package com.challenge.RentalCar.rentals;

import com.challenge.RentalCar.rentals.domain.rules.DefaultLoyaltyPolicy;
import com.challenge.RentalCar.rentals.domain.rules.DefaultPricingPolicy;
import com.challenge.RentalCar.rentals.domain.rules.LoyaltyPolicy;
import com.challenge.RentalCar.rentals.domain.rules.PricingPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RentalsConfig {

    @Bean
    PricingPolicy pricingPolicy(
            @Value("${pricing.premium:300}") double premium,
            @Value("${pricing.suv:150}") double suv,
            @Value("${pricing.small:50}") double small) {
        return new DefaultPricingPolicy(premium, suv, small);
    }

    @Bean
    LoyaltyPolicy loyaltyPolicy() {
        return new DefaultLoyaltyPolicy();
    }
}
