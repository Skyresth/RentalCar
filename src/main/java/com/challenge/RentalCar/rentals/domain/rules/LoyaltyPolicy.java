package com.challenge.RentalCar.rentals.domain.rules;


import com.challenge.RentalCar.inventory.domain.CarType;

public interface LoyaltyPolicy {
    int pointsFor(CarType type);
}
