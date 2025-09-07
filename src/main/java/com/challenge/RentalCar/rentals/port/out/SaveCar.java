package com.challenge.RentalCar.rentals.port.out;

import com.challenge.RentalCar.inventory.domain.Car;

public interface SaveCar {
    Car save(Car car);
}
