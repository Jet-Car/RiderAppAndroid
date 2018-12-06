package car.pubnub.api.callbacks;


import car.pubnub.api.models.consumer.PNStatus;

public abstract class PNCallback<X> {
    public abstract void onResponse(X result, PNStatus status);
}

