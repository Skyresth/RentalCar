package com.challenge.RentalCar.rentals.domain;

import com.challenge.RentalCar.inventory.domain.CarType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RentalTest {

    @Test
    @DisplayName("open(): creates a new OPEN rental with today's startDate and null id")
    void open_createsOpenRental() {
        // given
        long customerId = 1L;
        long carId = 4L;
        int days = 9;
        double prepaid = 410.0;
        LocalDate today = LocalDate.now();

        // when
        Rental rental = Rental.open(customerId, carId, CarType.SMALL, days, prepaid);

        // then
        assertThat(rental.getId()).isNull();
        assertThat(rental.getCustomerId()).isEqualTo(customerId);
        assertThat(rental.getCarId()).isEqualTo(carId);
        assertThat(rental.getType()).isEqualTo(CarType.SMALL);
        assertThat(rental.getDaysBooked()).isEqualTo(days);
        assertThat(rental.getPrepaidAmount()).isEqualTo(prepaid);
        assertThat(rental.getStatus()).isEqualTo(Rental.Status.OPEN);
        assertThat(rental.getStartDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("assignId(): sets the generated identifier")
    void assignId_setsId() {
        // given
        Rental rental = Rental.open(1L, 2L, CarType.SUV, 2, 300.0);

        // when
        rental.assignId(123L);

        // then
        assertThat(rental.getId()).isEqualTo(123L);
    }

    @Test
    @DisplayName("markReturned(): sets status to RETURNED")
    void markReturned_setsReturned() {
        // given
        Rental rental = Rental.open(1L, 2L, CarType.PREMIUM, 3, 900.0);

        // when
        rental.markReturned();

        // then
        assertThat(rental.getStatus()).isEqualTo(Rental.Status.RETURNED);
    }

    @Test
    @DisplayName("reconstitute(): restores all fields and status from persistence")
    void reconstitute_restoresFields() {
        // given
        LocalDate start = LocalDate.of(2025, 9, 7);

        // when
        Rental rental = Rental.reconstitute(
                77L, 10L, 20L, CarType.SUV, start, 9, 1290.0, Rental.Status.RETURNED
        );

        // then
        assertThat(rental.getId()).isEqualTo(77L);
        assertThat(rental.getCustomerId()).isEqualTo(10L);
        assertThat(rental.getCarId()).isEqualTo(20L);
        assertThat(rental.getType()).isEqualTo(CarType.SUV);
        assertThat(rental.getStartDate()).isEqualTo(start);
        assertThat(rental.getDaysBooked()).isEqualTo(9);
        assertThat(rental.getPrepaidAmount()).isEqualTo(1290.0);
        assertThat(rental.getStatus()).isEqualTo(Rental.Status.RETURNED);
    }
}
