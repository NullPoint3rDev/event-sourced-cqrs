package eventsourced.command.controller;

import eventsourced.command.dto.ConfirmOrderCommand;
import eventsourced.command.dto.CreateOrderCommand;
import eventsourced.command.services.CommandService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommandController.class)
class CommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommandService commandService;

    @Test
    void createOrder_returns201WithOrderId() throws Exception {
        CreateOrderCommand command = new CreateOrderCommand("customer-1", "100");
        String orderId = "order-uuid-123";
        when(commandService.createOrder(any(CreateOrderCommand.class))).thenReturn(orderId);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(content().string(orderId));

        verify(commandService).createOrder(any(CreateOrderCommand.class));
    }

    @Test
    void confirmOrder_returns204() throws Exception {
        String orderId = "order-456";
        ConfirmOrderCommand command = new ConfirmOrderCommand(orderId, List.of("item-1"));

        mockMvc.perform(post("/api/orders/" + orderId + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNoContent());

        verify(commandService).confirmOrder(eq(orderId), any(ConfirmOrderCommand.class));
    }
}
