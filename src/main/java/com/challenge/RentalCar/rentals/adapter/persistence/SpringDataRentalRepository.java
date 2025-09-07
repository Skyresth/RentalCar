package com.challenge.RentalCar.rentals.adapter.persistence;

import com.challenge.RentalCar.rentals.adapter.persistence.JpaRentalEntity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataRentalRepository extends JpaRepository<JpaRentalEntity, Long> {
    List<JpaRentalEntity> findByStatus(Status status);
    List<JpaRentalEntity> findByCustomerId(long customerId);
    List<JpaRentalEntity> findByCustomerIdAndStatus(long customerId, Status status);
}
