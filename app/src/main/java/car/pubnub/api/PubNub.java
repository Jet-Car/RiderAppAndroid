package car.pubnub.api;

import car.pubnub.api.builder.PresenceBuilder;
import car.pubnub.api.builder.PubNubErrorBuilder;
import car.pubnub.api.builder.SubscribeBuilder;
import car.pubnub.api.builder.UnsubscribeBuilder;
import car.pubnub.api.callbacks.SubscribeCallback;
import car.pubnub.api.endpoints.DeleteMessages;
import car.pubnub.api.endpoints.FetchMessages;
import car.pubnub.api.endpoints.History;
import car.pubnub.api.endpoints.Time;
import car.pubnub.api.endpoints.access.Audit;
import car.pubnub.api.endpoints.access.Grant;
import car.pubnub.api.endpoints.channel_groups.AddChannelChannelGroup;
import car.pubnub.api.endpoints.channel_groups.AllChannelsChannelGroup;
import car.pubnub.api.endpoints.channel_groups.DeleteChannelGroup;
import car.pubnub.api.endpoints.channel_groups.ListAllChannelGroup;
import car.pubnub.api.endpoints.channel_groups.RemoveChannelChannelGroup;
import car.pubnub.api.endpoints.presence.GetState;
import car.pubnub.api.endpoints.presence.HereNow;
import car.pubnub.api.endpoints.presence.SetState;
import car.pubnub.api.endpoints.presence.WhereNow;
import car.pubnub.api.endpoints.pubsub.Publish;
import car.pubnub.api.endpoints.push.AddChannelsToPush;
import car.pubnub.api.endpoints.push.ListPushProvisions;
import car.pubnub.api.endpoints.push.RemoveAllPushChannelsForDevice;
import car.pubnub.api.endpoints.push.RemoveChannelsFromPush;
import car.pubnub.api.managers.BasePathManager;
import car.pubnub.api.managers.MapperManager;
import car.pubnub.api.managers.PublishSequenceManager;
import car.pubnub.api.managers.RetrofitManager;
import car.pubnub.api.managers.SubscriptionManager;
import car.pubnub.api.managers.TelemetryManager;
import car.pubnub.api.vendor.Crypto;

import java.util.Date;
import java.util.List;
import java.util.UUID;


public class PubNub {

    private static final int TIMESTAMP_DIVIDER = 1000;
    private static final int MAX_SEQUENCE = 65535;
    private static final String SDK_VERSION = "4.17.0";
    private PNConfiguration configuration;
    private MapperManager mapper;
    private String instanceId;
    private SubscriptionManager subscriptionManager;
    private BasePathManager basePathManager;
    private PublishSequenceManager publishSequenceManager;
    private TelemetryManager telemetryManager;
    private RetrofitManager retrofitManager;

    public PubNub(PNConfiguration initialConfig) {
        this.configuration = initialConfig;
        this.mapper = new MapperManager();
        this.telemetryManager = new TelemetryManager();
        this.basePathManager = new BasePathManager(initialConfig);
        this.retrofitManager = new RetrofitManager(this);
        this.subscriptionManager = new SubscriptionManager(this, retrofitManager, this.telemetryManager);
        this.publishSequenceManager = new PublishSequenceManager(MAX_SEQUENCE);
        instanceId = UUID.randomUUID().toString();
    }

    public String getBaseUrl() {
        return this.basePathManager.getBasePath();
    }


    //
    public void addListener(SubscribeCallback listener) {
        subscriptionManager.addListener(listener);
    }

    public void removeListener(SubscribeCallback listener) {
        subscriptionManager.removeListener(listener);
    }

    public SubscribeBuilder subscribe() {
        return new SubscribeBuilder(this.subscriptionManager);
    }

    public UnsubscribeBuilder unsubscribe() {
        return new UnsubscribeBuilder(this.subscriptionManager);
    }

    public PresenceBuilder presence() {
        return new PresenceBuilder(this.subscriptionManager);
    }

    // start push

    public AddChannelsToPush addPushNotificationsOnChannels() {
        return new AddChannelsToPush(this, this.telemetryManager, this.retrofitManager);
    }

    public RemoveChannelsFromPush removePushNotificationsFromChannels() {
        return new RemoveChannelsFromPush(this, this.telemetryManager, this.retrofitManager);
    }

    public RemoveAllPushChannelsForDevice removeAllPushNotificationsFromDeviceWithPushToken() {
        return new RemoveAllPushChannelsForDevice(this, this.telemetryManager, this.retrofitManager);
    }

    public ListPushProvisions auditPushChannelProvisions() {
        return new ListPushProvisions(this, this.telemetryManager, this.retrofitManager);
    }

    // end push

    public WhereNow whereNow() {
        return new WhereNow(this, this.telemetryManager, this.retrofitManager);
    }

    public HereNow hereNow() {
        return new HereNow(this, this.telemetryManager, this.retrofitManager);
    }

    public Time time() {
        return new Time(this, this.telemetryManager, this.retrofitManager);
    }

    public History history() {
        return new History(this, this.telemetryManager, this.retrofitManager);
    }

    public FetchMessages fetchMessages() {
        return new FetchMessages(this, this.telemetryManager, this.retrofitManager);
    }

    public DeleteMessages deleteMessages() {
        return new DeleteMessages(this, this.telemetryManager, this.retrofitManager);
    }

    public Audit audit() {
        return new Audit(this, this.telemetryManager, this.retrofitManager);
    }

    public Grant grant() {
        return new Grant(this, this.telemetryManager, this.retrofitManager);
    }

    public GetState getPresenceState() {
        return new GetState(this, this.telemetryManager, this.retrofitManager);
    }

    public SetState setPresenceState() {
        return new SetState(this, subscriptionManager, this.telemetryManager, this.retrofitManager);
    }

    public Publish publish() {
        return new Publish(this, publishSequenceManager, this.telemetryManager, this.retrofitManager);
    }

    public ListAllChannelGroup listAllChannelGroups() {
        return new ListAllChannelGroup(this, this.telemetryManager, this.retrofitManager);
    }

    public AllChannelsChannelGroup listChannelsForChannelGroup() {
        return new AllChannelsChannelGroup(this, this.telemetryManager, this.retrofitManager);
    }

    public AddChannelChannelGroup addChannelsToChannelGroup() {
        return new AddChannelChannelGroup(this, this.telemetryManager, this.retrofitManager);
    }

    public RemoveChannelChannelGroup removeChannelsFromChannelGroup() {
        return new RemoveChannelChannelGroup(this, this.telemetryManager, this.retrofitManager);
    }

    public DeleteChannelGroup deleteChannelGroup() {
        return new DeleteChannelGroup(this, this.telemetryManager, this.retrofitManager);
    }

    // public methods

    /**
     * Perform Cryptographic decryption of an input string using cipher key provided by PNConfiguration
     *
     * @param inputString String to be encrypted
     * @return String containing the encryption of inputString using cipherKey
     */
    public String decrypt(String inputString) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS).build();
        }

        return decrypt(inputString, this.getConfiguration().getCipherKey());
    }

    /**
     * Perform Cryptographic decryption of an input string using the cipher key
     *
     * @param inputString String to be encrypted
     * @param cipherKey   cipher key to be used for encryption
     * @return String containing the encryption of inputString using cipherKey
     * @throws PubNubException throws exception in case of failed encryption
     */
    public String decrypt(String inputString, String cipherKey) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS).build();
        }

        return new Crypto(cipherKey).decrypt(inputString);
    }

    /**
     * Perform Cryptographic encryption of an input string and the cipher key provided by PNConfiguration
     *
     * @param inputString String to be encrypted
     * @return String containing the encryption of inputString using cipherKey
     */
    public String encrypt(String inputString) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS).build();
        }

        return encrypt(inputString, this.getConfiguration().getCipherKey());
    }

    /**
     * Perform Cryptographic encryption of an input string and the cipher key.
     *
     * @param inputString String to be encrypted
     * @param cipherKey   cipher key to be used for encryption
     * @return String containing the encryption of inputString using cipherKey
     * @throws PubNubException throws exception in case of failed encryption
     */
    public String encrypt(String inputString, String cipherKey) throws PubNubException {
        if (inputString == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS).build();
        }

        return new Crypto(cipherKey).encrypt(inputString);
    }

    public int getTimestamp() {
        return (int) ((new Date().getTime()) / TIMESTAMP_DIVIDER);
    }

    /**
     * @return instance uuid.
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * @return request uuid.
     */
    public String getRequestId() {
        return UUID.randomUUID().toString();
    }

    /**
     * @return version of the SDK.
     */
    public String getVersion() {
        return SDK_VERSION;
    }

    /**
     * Stop the SDK and terminate all listeners.
     */
    @Deprecated
    public void stop() {
        subscriptionManager.stop();
    }

    /**
     * Destroy the SDK to cancel all ongoing requests and stop heartbeat timer.
     */
    public void destroy() {
        try {
            subscriptionManager.destroy(false);
            retrofitManager.destroy(false);
        } catch (Exception error) {
            //
        }
    }

    /**
     * Force destroy the SDK to evict the connection pools and close executors.
     */
    public void forceDestroy() {
        try {
            subscriptionManager.destroy(true);
            retrofitManager.destroy(true);
            telemetryManager.stopCleanUpTimer();
        } catch (Exception error) {
            //
        }
    }

    /**
     * Perform a Reconnect to the network
     */
    public void reconnect() {
        subscriptionManager.reconnect();
    }

    /**
     * Perform a disconnect from the listeners
     */
    public void disconnect() {
        subscriptionManager.disconnect();
    }

    public Publish fire() {
        return publish().shouldStore(false).replicate(false);
    }

    public List<String> getSubscribedChannels() {
        return subscriptionManager.getSubscribedChannels();
    }

    public List<String> getSubscribedChannelGroups() {
        return subscriptionManager.getSubscribedChannelGroups();
    }

    public void unsubscribeAll() {
        subscriptionManager.unsubscribeAll();
    }


    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    public PNConfiguration getConfiguration() {
        return this.configuration;
    }

    @java.lang.SuppressWarnings("all")
    @javax.annotation.Generated("lombok")
    public MapperManager getMapper() {
        return this.mapper;
    }
}
