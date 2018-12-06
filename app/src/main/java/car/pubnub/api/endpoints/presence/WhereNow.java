package car.pubnub.api.endpoints.presence;

import car.pubnub.api.PubNub;
import car.pubnub.api.PubNubException;
import car.pubnub.api.builder.PubNubErrorBuilder;
import car.pubnub.api.endpoints.Endpoint;
import car.pubnub.api.enums.PNOperationType;
import car.pubnub.api.managers.RetrofitManager;
import car.pubnub.api.managers.TelemetryManager;
import car.pubnub.api.models.consumer.presence.PNWhereNowResult;
import car.pubnub.api.models.server.Envelope;
import car.pubnub.api.models.server.presence.WhereNowPayload;

import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

@Accessors(chain = true, fluent = true)
public class WhereNow extends Endpoint<Envelope<WhereNowPayload>, PNWhereNowResult> {

    @Setter
    private String uuid;

    public WhereNow(PubNub pubnub, TelemetryManager telemetryManager, RetrofitManager retrofit) {
        super(pubnub, telemetryManager, retrofit);
    }

    @Override
    protected List<String> getAffectedChannels() {
        return null;
    }

    @Override
    protected List<String> getAffectedChannelGroups() {
        return null;
    }

    @Override
    protected void validateParams() throws PubNubException {
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
    }

    @Override
    protected Call<Envelope<WhereNowPayload>> doWork(Map<String, String> params) {
        return this.getRetrofit().getPresenceService().whereNow(this.getPubnub().getConfiguration().getSubscribeKey(),
                this.uuid != null ? this.uuid : this.getPubnub().getConfiguration().getUuid(), params);
    }

    @Override
    protected PNWhereNowResult createResponse(Response<Envelope<WhereNowPayload>> input) throws PubNubException {
        if (input.body() == null || input.body().getPayload() == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_PARSING_ERROR).build();
        }

        PNWhereNowResult pnPresenceWhereNowResult = PNWhereNowResult.builder()
                .channels(input.body().getPayload().getChannels())
                .build();

        return pnPresenceWhereNowResult;
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNWhereNowOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

}
