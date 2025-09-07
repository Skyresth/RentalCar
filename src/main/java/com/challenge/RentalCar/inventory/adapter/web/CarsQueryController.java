package com.challenge.RentalCar.inventory.adapter.web;

import com.challenge.RentalCar.inventory.adapter.persistence.SpringDataCarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
class CarsQueryController {
    private final SpringDataCarRepository repo;

    @GetMapping
    public List<Map<String, Object>> all() {
        return repo.findAll().stream().map(e -> Map.<String, Object>of(
                "id", e.getId(),
                "brand", e.getBrand(),
                "model", e.getModel(),
                "type", e.getType(),
                "available", e.isAvailable()
        )).toList();
    }

}

