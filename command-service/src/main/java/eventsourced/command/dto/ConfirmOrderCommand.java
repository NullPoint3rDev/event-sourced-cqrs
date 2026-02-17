package eventsourced.command.dto;

import java.time.Instant;
import java.util.List;

public record ConfirmOrderCommand (
        String orderId,
        List<String> products
) {}
