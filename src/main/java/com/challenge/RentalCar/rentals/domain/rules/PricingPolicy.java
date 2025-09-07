package com.challenge.RentalCar.rentals.domain.rules;


import com.challenge.RentalCar.inventory.domain.CarType;

public interface PricingPolicy {
    double basePrice(CarType type, int days);
    double latePerDay(CarType type);
}
