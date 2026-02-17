package eventsourced.query.controller;

import eventsourced.query.dto.OrderView;
import eventsourced.query.readmodel.OrderReadModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QueryController.class)
class QueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderReadModel readModel;

    @Test
    void getOrder_whenFound_returns200AndOrderView() throws Exception {
        String orderId = "order-123";
        OrderView view = OrderView.fromCreated(
                orderId,
                "customer-1",
                "99.99",
                Instant.parse("2025-02-16T12:00:00Z")
        );
        when(readModel.get(orderId)).thenReturn(Optional.of(view));

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.amount").value("99.99"))
                .andExpect(jsonPath("$.confirmed").value(false));
    }

    @Test
    void getOrder_whenNotFound_returns404() throws Exception {
        when(readModel.get("missing-id")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/missing-id"))
                .andExpect(status().isNotFound());
    }
}
