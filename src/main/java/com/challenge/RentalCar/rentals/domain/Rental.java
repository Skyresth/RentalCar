package com.challenge.RentalCar.rentals.domain;

import com.challenge.RentalCar.inventory.domain.CarType;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Rental {
    public enum Status {OPEN, RETURNED}

    private Long id;
    private final long customerId;
    private final long carId;
    private final CarType type;
    private final LocalDate startDate;
    private final int daysBooked;
    private final double prepaidAmount;
    private Status status = Status.OPEN;

    private Rental(Long id, long customerId, long carId, CarType type,
                   LocalDate startDate, int daysBooked, double prepaidAmount) {
        this.id = id;
        this.customerId = customerId;
        this.carId = carId;
        this.type = type;
        this.startDate = startDate;
        this.daysBooked = daysBooked;
        this.prepaidAmount = prepaidAmount;
    }

    public static Rental reconstitute(Long id, long customerId, long carId, CarType type,
                                      LocalDate startDate, int daysBooked, double prepaidAmount,
                                      Status status) {
        var rental = new Rental(id, customerId, carId, type, startDate, daysBooked, prepaidAmount);
        rental.status = status;
        return rental;
    }

    public static Rental open(long customerId, long carId, CarType type, int days, double prepaid) {
        return new Rental(null, customerId, carId, type, LocalDate.now(), days, prepaid);
    }

    public void assignId(long id) {
        this.id = id;
    }

    public void markReturned() {
        this.status = Status.RETURNED;
    }
}
