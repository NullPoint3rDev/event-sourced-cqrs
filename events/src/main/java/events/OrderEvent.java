package events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "OrderCreated", value = OrderCreated.class),
        @JsonSubTypes.Type(name = "OrderConfirmed", value = OrderConfirmed.class)
})
public interface OrderEvent {
    String orderId();
}
