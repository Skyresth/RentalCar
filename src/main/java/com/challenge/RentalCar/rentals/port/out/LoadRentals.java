package com.challenge.RentalCar.rentals.port.out;

import com.challenge.RentalCar.rentals.domain.Rental;

import java.util.List;

public interface LoadRentals {
    List<Rental> findAll();

    List<Rental> findByStatus(Rental.Status status);

    List<Rental> findByCustomer(long customerId);

    List<Rental> findByCustomerAndStatus(long customerId, Rental.Status status);
}
