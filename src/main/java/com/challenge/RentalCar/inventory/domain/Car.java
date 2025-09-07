package com.challenge.RentalCar.inventory.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Car {
    private Long id;
    private String brand;
    private String model;
    private CarType type;
    private boolean available = true;

    public Car(Long id, String brand, String model, CarType type, boolean available) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.type = type;
        this.available = available;
    }
}
