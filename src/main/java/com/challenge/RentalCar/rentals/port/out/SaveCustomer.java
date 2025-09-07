package com.challenge.RentalCar.rentals.port.out;

import com.challenge.RentalCar.customers.domain.Customer;

public interface SaveCustomer {
    Customer save(Customer customer);
}
