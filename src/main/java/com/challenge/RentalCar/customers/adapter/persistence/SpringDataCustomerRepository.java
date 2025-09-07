package com.challenge.RentalCar.customers.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCustomerRepository extends JpaRepository<JpaCustomerEntity, Long> {}

