package com.challenge.RentalCar.customers.adapter.web;

import com.challenge.RentalCar.customers.adapter.persistence.SpringDataCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
class CustomersQueryController {
    private final SpringDataCustomerRepository repo;

    @GetMapping
    public List<Map<String,Object>> all() {
        return repo.findAll().stream().map(e -> Map.<String,Object>of(
                "id", e.getId(),
                "name", e.getName(),
                "points", e.getPoints()
        )).toList();
    }

}
