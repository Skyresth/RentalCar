package com.challenge.RentalCar.rentals.domain.rules;

import com.challenge.RentalCar.inventory.domain.CarType;

public class DefaultPricingPolicy implements PricingPolicy {
    private final double premium, suv, small;
    public DefaultPricingPolicy(double premium, double suv, double small) {
        this.premium = premium; this.suv = suv; this.small = small;
    }
    public double basePrice(CarType type, int days) {
        return switch (type) {
            case PREMIUM -> premium * days;
            case SUV -> {
                double total = 0;
                for (int d = 1; d <= days; d++) {
                    if (d <= 7) total += suv;
                    else if (d <= 30) total += suv * 0.80;
                    else total += suv * 0.50;
                }
                yield total;
            }
            case SMALL -> {
                if (days <= 7) yield small * days;
                yield small * 7 + (small * 0.60) * (days - 7);
            }
        };
    }
    public double latePerDay(CarType type) {
        return switch (type) {
            case PREMIUM -> premium * 1.20;
            case SUV -> suv + 0.60 * small;
            case SMALL -> small * 1.30;
        };
    }
}
