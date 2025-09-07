package com.challenge.RentalCar.rentals.port.out;

import com.challenge.RentalCar.inventory.domain.Car;

import java.util.Optional;

public interface LoadCar {
    Optional<Car> byId(long id);
}
