package eventsourced.query.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import events.OrderConfirmed;
import events.OrderCreated;
import events.OrderEvent;
import events.Topics;
import eventsourced.query.dto.OrderView;
import eventsourced.query.readmodel.OrderReadModel;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class QueryConsumerService {

    private final ObjectMapper mapper;
    private final OrderReadModel model;
    private final Counter eventsProcessedTotal;

    public QueryConsumerService(ObjectMapper mapper, OrderReadModel model, MeterRegistry registry) {
        this.mapper = mapper;
        this.model = model;
        this.eventsProcessedTotal = registry.counter("events_processed_total", "source", "order-events");
    }

    @KafkaListener(topics = Topics.ORDER_EVENTS, groupId = "query-service-order-read-model")
    public void handleMessage(String payload) {
        try {
            OrderEvent event = mapper.readValue(payload, OrderEvent.class);
            if(event instanceof OrderCreated created) {
                eventsProcessedTotal.increment();
                OrderView view = OrderView.fromCreated(
                        created.orderId(),
                        created.customerId(),
                        created.amount(),
                        created.createdAt()
                );
                model.put(view);
            } else if (event instanceof OrderConfirmed confirmed) {
                eventsProcessedTotal.increment();
                model.get(confirmed.orderId()).ifPresent(existing -> {
                    OrderView updated = new OrderView(
                            existing.orderId(),
                            existing.customerId(),
                            existing.amount(),
                            existing.createdAt(),
                            true,
                            confirmed.products(),
                            confirmed.confirmedAt()
                    );
                    model.put(updated);
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process event", e);
        }
    }
}
