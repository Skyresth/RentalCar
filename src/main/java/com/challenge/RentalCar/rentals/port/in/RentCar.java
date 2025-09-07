package com.challenge.RentalCar.rentals.port.in;

public interface RentCar {
    record Command(long customerId, long carId, int days) {
    }

    record Result(long rentalId, double prepaidAmount, int loyaltyPointsAwarded) {
    }

    Result handle(Command cmd);
}
