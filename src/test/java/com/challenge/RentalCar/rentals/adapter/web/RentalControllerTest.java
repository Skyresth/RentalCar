package com.challenge.RentalCar.rentals.adapter.web;

import com.challenge.RentalCar.rentals.port.in.RentCar;
import com.challenge.RentalCar.rentals.port.in.ReturnCar;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RentalController.class)
class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RentCar rentCar;

    @MockitoBean
    private ReturnCar returnCar;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("POST /rentals -> delegates to RentCar.handle and returns its result")
    void rent_delegatesAndReturns() throws Exception {
        // given
        var cmdJson = """
                  {"customerId":1,"carId":2,"days":9}
                """;

        var expected = new RentCar.Result(42L, 1290.0, 3);
        given(rentCar.handle(new RentCar.Command(1L, 2L, 9))).willReturn(expected);

        // when
        mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cmdJson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.rentalId").value(42))
                .andExpect(jsonPath("$.prepaidAmount").value(1290.0))
                .andExpect(jsonPath("$.loyaltyPointsAwarded").value(3));

        // then
        var captor = ArgumentCaptor.forClass(RentCar.Command.class);
        verify(rentCar).handle(captor.capture());
        var passed = captor.getValue();
        assertThat(passed.customerId()).isEqualTo(1L);
        assertThat(passed.carId()).isEqualTo(2L);
        assertThat(passed.days()).isEqualTo(9);
    }


    @Test
    @DisplayName("POST /rentals/{id}/return -> uses path id and ignores rentalId in body")
    void return_usesPathId_ignoresBodyRentalId() throws Exception {
        // given
        var pathId = 99L;
        var bodyWithDifferentId = """
                  {"rentalId":1234, "actualReturnDate":"2025-09-18"}
                """;

        var expected = new ReturnCar.Result(pathId, 130.0);
        given(returnCar.handle(new ReturnCar.Command(pathId, LocalDate.parse("2025-09-18"))))
                .willReturn(expected);

        // when
        mockMvc.perform(post("/rentals/{id}/return", pathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithDifferentId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.rentalId").value(99))
                .andExpect(jsonPath("$.surcharge").value(130.0));

        // then
        var captor = ArgumentCaptor.forClass(ReturnCar.Command.class);
        verify(returnCar).handle(captor.capture());
        var passed = captor.getValue();
        assertThat(passed.rentalId()).isEqualTo(99L);
        assertThat(passed.actualReturnDate()).isEqualTo(LocalDate.of(2025, 9, 18));
    }

    @Test
    @DisplayName("POST /rentals/{id}/return -> happy path with zero surcharge")
    void return_zeroSurcharge() throws Exception {
        // given
        var pathId = 1L;
        var date = LocalDate.of(2025, 9, 16);
        var body = objectMapper.writeValueAsString(new ReturnCar.Command(777L, date));

        given(returnCar.handle(new ReturnCar.Command(pathId, date)))
                .willReturn(new ReturnCar.Result(pathId, 0.0));

        // when / then
        mockMvc.perform(post("/rentals/{id}/return", pathId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalId").value(1))
                .andExpect(jsonPath("$.surcharge").value(0.0));
    }
}
