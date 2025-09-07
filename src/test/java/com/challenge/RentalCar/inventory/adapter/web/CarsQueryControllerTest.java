package com.challenge.RentalCar.inventory.adapter.web;

import com.challenge.RentalCar.inventory.adapter.persistence.JpaCarEntity;
import com.challenge.RentalCar.inventory.adapter.persistence.SpringDataCarRepository;
import com.challenge.RentalCar.inventory.domain.CarType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarsQueryController.class)
class CarsQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpringDataCarRepository repo;

    @Test
    @DisplayName("GET /cars returns list of cars as JSON")
    void getCars_returnsList() throws Exception {
        // given
        var bmw = new JpaCarEntity();
        bmw.setId(1L);
        bmw.setBrand("BMW");
        bmw.setModel("7");
        bmw.setType(CarType.PREMIUM);
        bmw.setAvailable(true);

        var kia = new JpaCarEntity();
        kia.setId(2L);
        kia.setBrand("Kia");
        kia.setModel("Sorento");
        kia.setType(CarType.SUV);
        kia.setAvailable(false);

        given(repo.findAll()).willReturn(List.of(bmw, kia));

        // when / then
        mockMvc.perform(get("/cars").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].brand").value("BMW"))
                .andExpect(jsonPath("$[0].model").value("7"))
                .andExpect(jsonPath("$[0].type").value("PREMIUM"))
                .andExpect(jsonPath("$[0].available").value(true))

                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].brand").value("Kia"))
                .andExpect(jsonPath("$[1].model").value("Sorento"))
                .andExpect(jsonPath("$[1].type").value("SUV"))
                .andExpect(jsonPath("$[1].available").value(false));
    }

    @Test
    @DisplayName("GET /cars returns empty array when no cars exist")
    void getCars_returnsEmptyList() throws Exception {
        // given
        given(repo.findAll()).willReturn(List.of());

        // when / then
        mockMvc.perform(get("/cars").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }
}
