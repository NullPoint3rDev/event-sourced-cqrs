package events;

import java.time.Instant;
import java.util.List;

public record OrderConfirmed(
        String orderId,
        long sequenceNumber,
        String customerId,
        List<String> products,
        Instant confirmedAt
) implements OrderEvent {}
