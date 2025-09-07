package com.challenge.RentalCar.rentals.port.out;

import com.challenge.RentalCar.customers.domain.Customer;

import java.util.Optional;

public interface LoadCustomer {
    Optional<Customer> byId(long id);
}
