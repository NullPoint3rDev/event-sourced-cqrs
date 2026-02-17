package events;

import java.time.Instant;

public record OrderCreated(
        String orderId,
        long sequenceNumber,
        String customerId,
        String amount,
        Instant createdAt
) implements OrderEvent {}
