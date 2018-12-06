package car.pubnub.api.callbacks;

import car.pubnub.api.PubNub;
import car.pubnub.api.models.consumer.PNStatus;
import car.pubnub.api.models.consumer.pubsub.PNMessageResult;
import car.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

public abstract class SubscribeCallback {
    public abstract void status(PubNub pubnub, PNStatus status);

    public abstract void message(PubNub pubnub, PNMessageResult message);

    public abstract void presence(PubNub pubnub, PNPresenceEventResult presence);
}
