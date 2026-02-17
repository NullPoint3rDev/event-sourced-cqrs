package eventsourced.query.controller;

import eventsourced.query.dto.OrderView;
import eventsourced.query.readmodel.OrderReadModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QueryController {

    private final OrderReadModel readModel;

    public QueryController(OrderReadModel readModel) {
        this.readModel = readModel;
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderView> getOrder(@PathVariable String id) {
        return readModel.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
