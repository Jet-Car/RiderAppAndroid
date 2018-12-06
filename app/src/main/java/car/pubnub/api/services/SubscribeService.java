package car.pubnub.api.services;

import car.pubnub.api.models.server.SubscribeEnvelope;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface SubscribeService {

    @GET("v2/subscribe/{subKey}/{channel}/0")
    Call<SubscribeEnvelope> subscribe(@Path("subKey") String subKey,
                                      @Path("channel") String channel,
                                      @QueryMap(encoded = true) Map<String, String> options);

}
