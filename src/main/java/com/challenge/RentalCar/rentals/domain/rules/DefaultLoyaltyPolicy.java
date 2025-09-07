package com.challenge.RentalCar.rentals.domain.rules;

import com.challenge.RentalCar.inventory.domain.CarType;

public class DefaultLoyaltyPolicy implements LoyaltyPolicy {
    public int pointsFor(CarType type) {
        return switch (type) {
            case PREMIUM -> 5;
            case SUV -> 3;
            case SMALL -> 1;
        };
    }
}

