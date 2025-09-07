package com.challenge.RentalCar.rentals.adapter.persistence;

import com.challenge.RentalCar.inventory.domain.CarType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "rentals")
@Getter
@Setter
public class JpaRentalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    long customerId;
    long carId;
    @Enumerated(EnumType.STRING)
    CarType type;
    LocalDate startDate;
    int daysBooked;
    double prepaidAmount;
    @Enumerated(EnumType.STRING)
    Status status;

    public enum Status {OPEN, RETURNED}
}

