package eventsourced.command.dto;

public record CreateOrderCommand (
        String customerId,
        String amount
) {}
