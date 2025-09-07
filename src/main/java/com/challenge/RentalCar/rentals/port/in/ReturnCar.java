package com.challenge.RentalCar.rentals.port.in;

import java.time.LocalDate;

public interface ReturnCar {
    record Command(long rentalId, LocalDate actualReturnDate) {
    }

    record Result(long rentalId, double surcharge) {
    }

    Result handle(Command cmd);
}
