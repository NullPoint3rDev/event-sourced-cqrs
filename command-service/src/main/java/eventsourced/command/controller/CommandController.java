package eventsourced.command.controller;

import eventsourced.command.dto.ConfirmOrderCommand;
import eventsourced.command.services.CommandService;
import eventsourced.command.dto.CreateOrderCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CommandController {

    private final CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(@RequestBody CreateOrderCommand command) {
        String orderId = commandService.createOrder(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    @PostMapping("/orders/{id}/confirm")
    public ResponseEntity<Void> confirmOrder(@PathVariable("id") String orderId,
                                             @RequestBody ConfirmOrderCommand command) {
        commandService.confirmOrder(orderId, command);
        return ResponseEntity.noContent().build();
    }
}
