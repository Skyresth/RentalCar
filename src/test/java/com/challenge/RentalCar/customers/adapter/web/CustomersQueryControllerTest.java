package com.challenge.RentalCar.customers.adapter.web;

import com.challenge.RentalCar.customers.adapter.persistence.JpaCustomerEntity;
import com.challenge.RentalCar.customers.adapter.persistence.SpringDataCustomerRepository;
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

@WebMvcTest(CustomersQueryController.class)
class CustomersQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpringDataCustomerRepository repo;

    @Test
    @DisplayName("GET /customers returns list of customers as JSON")
    void getCustomers_returnsList() throws Exception {
        // given
        var alice = new JpaCustomerEntity();
        alice.setId(1L);
        alice.setName("Alice");
        alice.setPoints(5);

        var bob = new JpaCustomerEntity();
        bob.setId(2L);
        bob.setName("Bob");
        bob.setPoints(0);

        given(repo.findAll()).willReturn(List.of(alice, bob));

        // when / then
        mockMvc.perform(get("/customers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[0].points").value(5))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Bob"))
                .andExpect(jsonPath("$[1].points").value(0));
    }

    @Test
    @DisplayName("GET /customers returns empty array when no customers")
    void getCustomers_returnsEmptyList() throws Exception {
        // given
        given(repo.findAll()).willReturn(List.of());

        // when / then
        mockMvc.perform(get("/customers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }
}
