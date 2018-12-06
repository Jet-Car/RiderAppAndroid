package car.pubnub.api.workers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import car.pubnub.api.PubNub;
import car.pubnub.api.PubNubException;
import car.pubnub.api.PubNubUtil;
import car.pubnub.api.enums.PNOperationType;
import car.pubnub.api.enums.PNStatusCategory;
import car.pubnub.api.managers.DuplicationManager;
import car.pubnub.api.managers.ListenerManager;
import car.pubnub.api.managers.MapperManager;
import car.pubnub.api.models.consumer.PNErrorData;
import car.pubnub.api.models.consumer.PNStatus;
import car.pubnub.api.models.consumer.pubsub.PNMessageResult;
import car.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import car.pubnub.api.models.server.PresenceEnvelope;
import car.pubnub.api.models.server.PublishMetaData;
import car.pubnub.api.models.server.SubscribeMessage;
import car.pubnub.api.vendor.Crypto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;


public class SubscribeMessageWorker implements Runnable {

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SubscribeMessageWorker.class);

    private PubNub pubnub;
    private ListenerManager listenerManager;
    private LinkedBlockingQueue<SubscribeMessage> queue;
    private DuplicationManager duplicationManager;

    private boolean isRunning;

    public SubscribeMessageWorker(PubNub pubnubInstance,
                                  ListenerManager listenerManagerInstance,
                                  LinkedBlockingQueue<SubscribeMessage> queueInstance,
                                  DuplicationManager dupManager) {
        this.pubnub = pubnubInstance;
        this.listenerManager = listenerManagerInstance;
        this.queue = queueInstance;
        this.duplicationManager = dupManager;
    }

    @Override
    public void run() {
        takeMessage();
    }


    private void takeMessage() {
        this.isRunning = true;

        while (this.isRunning) {
            try {
                this.processIncomingPayload(this.queue.take());
            } catch (InterruptedException e) {
                this.isRunning = false;
                log.trace("take message interrupted", e);
            }
        }
    }

    private JsonElement processMessage(JsonElement input) {
        // if we do not have a crypto key, there is no way to process the node; let's return.
        if (pubnub.getConfiguration().getCipherKey() == null) {
            return input;
        }

        Crypto crypto = new Crypto(pubnub.getConfiguration().getCipherKey());
        MapperManager mapper = this.pubnub.getMapper();
        String inputText;
        String outputText;
        JsonElement outputObject;

        if (mapper.isJsonObject(input) && mapper.hasField(input, "pn_other")) {
            inputText = mapper.elementToString(input, "pn_other");
        } else {
            inputText = mapper.elementToString(input);
        }

        try {
            outputText = crypto.decrypt(inputText);
        } catch (PubNubException e) {
            PNStatus pnStatus = PNStatus.builder().error(true)
                    .errorData(new PNErrorData(e.getMessage(), e))
                    .operation(PNOperationType.PNSubscribeOperation)
                    .category(PNStatusCategory.PNDecryptionErrorCategory)
                    .build();

            listenerManager.announce(pnStatus);
            return null;
        }

        try {
            outputObject = mapper.fromJson(outputText, JsonElement.class);
        } catch (PubNubException e) {
            PNStatus pnStatus = PNStatus.builder().error(true)
                    .errorData(new PNErrorData(e.getMessage(), e))
                    .operation(PNOperationType.PNSubscribeOperation)
                    .category(PNStatusCategory.PNMalformedResponseCategory)
                    .build();

            listenerManager.announce(pnStatus);
            return null;
        }

        // inject the decoded response into the payload
        if (mapper.isJsonObject(input) && mapper.hasField(input, "pn_other")) {
            JsonObject objectNode = mapper.getAsObject(input);
            mapper.putOnObject(objectNode, "pn_other", outputObject);
            outputObject = objectNode;
        }

        return outputObject;
    }

    private void processIncomingPayload(SubscribeMessage message) {
        MapperManager mapper = this.pubnub.getMapper();

        String channel = message.getChannel();
        String subscriptionMatch = message.getSubscriptionMatch();
        PublishMetaData publishMetaData = message.getPublishMetaData();

        if (channel != null && channel.equals(subscriptionMatch)) {
            subscriptionMatch = null;
        }

        if (this.pubnub.getConfiguration().isDedupOnSubscribe()) {
            if (this.duplicationManager.isDuplicate(message)) {
                return;
            } else {
                this.duplicationManager.addEntry(message);
            }
        }

        if (message.getChannel().endsWith("-pnpres")) {
            PresenceEnvelope presencePayload = mapper.convertValue(message.getPayload(), PresenceEnvelope.class);

            String strippedPresenceChannel = null;
            String strippedPresenceSubscription = null;

            if (channel != null) {
                strippedPresenceChannel = PubNubUtil.replaceLast(channel, "-pnpres", "");
            }
            if (subscriptionMatch != null) {
                strippedPresenceSubscription = PubNubUtil.replaceLast(subscriptionMatch, "-pnpres", "");
            }

            JsonElement isHereNowRefresh = message.getPayload().getAsJsonObject().get("here_now_refresh");

            PNPresenceEventResult pnPresenceEventResult = PNPresenceEventResult.builder()
                    .event(presencePayload.getAction())
                    // deprecated
                    .actualChannel((subscriptionMatch != null) ? channel : null)
                    .subscribedChannel(subscriptionMatch != null ? subscriptionMatch : channel)
                    // deprecated
                    .channel(strippedPresenceChannel)
                    .subscription(strippedPresenceSubscription)
                    .state(presencePayload.getData())
                    .timetoken(publishMetaData.getPublishTimetoken())
                    .occupancy(presencePayload.getOccupancy())
                    .uuid(presencePayload.getUuid())
                    .timestamp(presencePayload.getTimestamp())
                    .join(getDelta(message.getPayload().getAsJsonObject().get("join")))
                    .leave(getDelta(message.getPayload().getAsJsonObject().get("leave")))
                    .timeout(getDelta(message.getPayload().getAsJsonObject().get("timeout")))
                    .hereNowRefresh(isHereNowRefresh != null && isHereNowRefresh.getAsBoolean())
                    .build();

            listenerManager.announce(pnPresenceEventResult);
        } else {
            JsonElement extractedMessage = processMessage(message.getPayload());

            if (extractedMessage == null) {
                log.debug("unable to parse payload on #processIncomingMessages");
            }

            PNMessageResult pnMessageResult = PNMessageResult.builder()
                    .message(extractedMessage)
                    // deprecated
                    .actualChannel((subscriptionMatch != null) ? channel : null)
                    .subscribedChannel(subscriptionMatch != null ? subscriptionMatch : channel)
                    // deprecated
                    .channel(channel)
                    .subscription(subscriptionMatch)
                    .timetoken(publishMetaData.getPublishTimetoken())
                    .publisher(message.getIssuingClientId())
                    .userMetadata(message.getUserMetadata())
                    .build();


            listenerManager.announce(pnMessageResult);
        }
    }

    private List<String> getDelta(JsonElement delta) {
        List<String> list = new ArrayList<>();
        if (delta != null) {
            JsonArray jsonArray = delta.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                list.add(jsonArray.get(i).getAsString());
            }
        }

        return list;

    }
}
