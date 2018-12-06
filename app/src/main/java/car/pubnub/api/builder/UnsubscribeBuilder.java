package car.pubnub.api.builder;

import car.pubnub.api.builder.dto.UnsubscribeOperation;
import car.pubnub.api.managers.SubscriptionManager;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnsubscribeBuilder extends PubSubBuilder {

    public UnsubscribeBuilder(SubscriptionManager subscriptionManager) {
        super(subscriptionManager);
    }

    public void execute() {

        UnsubscribeOperation unsubscribeOperation = UnsubscribeOperation.builder()
                .channels(this.getChannelSubscriptions())
                .channelGroups(this.getChannelGroupSubscriptions())
                .build();

        this.getSubscriptionManager().adaptUnsubscribeBuilder(unsubscribeOperation);
    }

}
