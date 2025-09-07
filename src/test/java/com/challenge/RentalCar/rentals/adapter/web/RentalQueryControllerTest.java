package com.challenge.RentalCar.rentals.adapter.web;

import com.challenge.RentalCar.rentals.port.in.ListRentals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RentalQueryController.class)
class RentalQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListRentals listRentals;

    private static ListRentals.View view(long id, long customerId, long carId, String type,
                                         String startDate, int days, String planned, double prepaid, String status) {
        return new ListRentals.View(
                id, customerId, carId, type,
                LocalDate.parse(startDate), days,
                LocalDate.parse(planned), prepaid, status
        );
    }

    @Test
    @DisplayName("GET /rentals with no filters -> delegates with empty filter and returns list")
    void history_noFilters() throws Exception {
        // given
        var view = view(1, 1, 4, "SMALL", "2025-09-07", 9, "2025-09-16", 410.0, "OPEN");
        var view1 = view(2, 1, 2, "SUV", "2025-09-07", 9, "2025-09-16", 1290.0, "RETURNED");
        given(listRentals.handle(any())).willReturn(List.of(view, view1));

        // when
        mockMvc.perform(get("/rentals").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[1].status").value("RETURNED"));

        // then
        var captor = ArgumentCaptor.forClass(ListRentals.Filter.class);
        verify(listRentals).handle(captor.capture());
        var filter = captor.getValue();
        assertThat(filter.customerId()).isEmpty();
        assertThat(filter.status()).isEmpty();
    }

    @Test
    @DisplayName("GET /rentals?status=OPEN -> delegates with status filter only")
    void history_statusOnly() throws Exception {
        // given
        var view = view(3, 2, 1, "PREMIUM", "2025-09-01", 10, "2025-09-11", 3000.0, "OPEN");
        given(listRentals.handle(any())).willReturn(List.of(view));

        // when
        mockMvc.perform(get("/rentals")
                        .param("status", "OPEN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("OPEN"));

        // then
        var captor = ArgumentCaptor.forClass(ListRentals.Filter.class);
        verify(listRentals).handle(captor.capture());
        var filter = captor.getValue();
        assertThat(filter.customerId()).isEmpty();
        assertThat(filter.status()).contains("OPEN");
    }

    @Test
    @DisplayName("GET /rentals?customerId=1&status=returned -> delegates with both filters")
    void history_bothFilters() throws Exception {
        // given
        var view = view(10, 1, 4, "SMALL", "2025-08-01", 5, "2025-08-06", 250.0, "RETURNED");
        given(listRentals.handle(any())).willReturn(List.of(view));

        // when
        mockMvc.perform(get("/rentals")
                        .param("customerId", "1")
                        .param("status", "returned")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].status").value("RETURNED"));

        // then
        var captor = ArgumentCaptor.forClass(ListRentals.Filter.class);
        verify(listRentals).handle(captor.capture());
        var filter = captor.getValue();
        assertThat(filter.customerId()).contains(1L);
        assertThat(filter.status()).contains("returned");
    }
}
