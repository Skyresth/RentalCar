package com.challenge.RentalCar.rentals.port.in;

import java.util.List;
import java.util.Optional;

public interface ListRentals {
    record Filter(Optional<Long> customerId, Optional<String> status) {
    }

    record View(Long id, Long customerId, Long carId, String type,
                java.time.LocalDate startDate, int daysBooked,
                java.time.LocalDate plannedReturnDate,
                double prepaidAmount, String status) {
    }

    List<View> handle(Filter filter);
}
