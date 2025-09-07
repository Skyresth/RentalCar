package com.challenge.RentalCar.rentals.port.out;

import com.challenge.RentalCar.rentals.domain.Rental;

import java.util.Optional;

public interface LoadRental {
    Optional<Rental> byId(long id);
}
