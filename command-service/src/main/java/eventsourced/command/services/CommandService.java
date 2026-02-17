package eventsourced.command.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import events.OrderConfirmed;
import events.OrderCreated;
import events.OrderEvent;
import events.Topics;
import eventsourced.command.dto.ConfirmOrderCommand;
import eventsourced.command.dto.CreateOrderCommand;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class CommandService {

    private final ObjectMapper mapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Counter ordersCreatedTotal;
    private final Counter ordersConfirmedTotal;

    public CommandService(ObjectMapper mapper, KafkaTemplate<String, String> kafkaTemplate, MeterRegistry registry) {
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
        this.ordersCreatedTotal = registry.counter("orders_created_total");
        this.ordersConfirmedTotal = registry.counter("orders_confirmed_total");
    }

    public String createOrder(CreateOrderCommand command) {
        String orderId = UUID.randomUUID().toString();
        OrderCreated orderCreated = new OrderCreated(
                orderId,
                1L,
                command.customerId(),
                command.amount(),
                Instant.now()
        );
        try {
            String json = mapper.writeValueAsString((OrderEvent) orderCreated);
            kafkaTemplate.send(Topics.ORDER_EVENTS, orderId, json);
            ordersCreatedTotal.increment();
            return orderId;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void confirmOrder(String orderId, ConfirmOrderCommand command) {
        OrderConfirmed confirmedOrder = new OrderConfirmed(
                orderId,
                2L,
                "",
                command.products(),
                Instant.now()
        );
        try {
            String json = mapper.writeValueAsString((OrderEvent) confirmedOrder);
            kafkaTemplate.send(Topics.ORDER_EVENTS, orderId, json);
            ordersConfirmedTotal.increment();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
