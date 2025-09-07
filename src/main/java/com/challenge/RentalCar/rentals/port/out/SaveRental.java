package com.challenge.RentalCar.rentals.port.out;

import com.challenge.RentalCar.rentals.domain.Rental;

public interface SaveRental {
    Rental save(Rental rental);
}
