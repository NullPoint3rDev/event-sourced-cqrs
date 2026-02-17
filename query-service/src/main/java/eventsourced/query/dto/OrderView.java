package eventsourced.query.dto;

import java.time.Instant;
import java.util.List;

public record OrderView (
        String orderId,
        String customerId,
        String amount,
        Instant createdAt,
        boolean confirmed,
        List<String> products,
        Instant confirmedAt
) {
    public static OrderView fromCreated(String orderId, String customerId, String amount, Instant createdAt) {
        return new OrderView(orderId, customerId, amount, createdAt, false, null, null);
    }
}
