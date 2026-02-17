package eventsourced.query.readmodel;

import eventsourced.query.dto.OrderView;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderReadModel {

    private final Map<String, OrderView> store = new ConcurrentHashMap<>();

    public void put(OrderView view) {
        store.put(view.orderId(), view);
    }

    public Optional<OrderView> get(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }
}
