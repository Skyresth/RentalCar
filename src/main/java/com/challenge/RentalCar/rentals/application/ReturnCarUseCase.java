package com.challenge.RentalCar.rentals.application;

import com.challenge.RentalCar.rentals.domain.Rental;
import com.challenge.RentalCar.rentals.domain.rules.PricingPolicy;
import com.challenge.RentalCar.rentals.port.in.ReturnCar;
import com.challenge.RentalCar.rentals.port.out.LoadCar;
import com.challenge.RentalCar.rentals.port.out.LoadRental;
import com.challenge.RentalCar.rentals.port.out.SaveCar;
import com.challenge.RentalCar.rentals.port.out.SaveRental;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ReturnCarUseCase implements ReturnCar {
    private final LoadRental loadRental;
    private final SaveRental saveRental;
    private final LoadCar loadCar;
    private final SaveCar saveCar;
    private final PricingPolicy pricingPolicy;

    @Override
    @Transactional
    public Result handle(Command cmd) {
        var rental = loadRental.byId(cmd.rentalId()).orElseThrow();
        if (rental.getStatus() == Rental.Status.RETURNED) return new Result(rental.getId(), 0.0);

        var planned = rental.getStartDate().plusDays(rental.getDaysBooked());
        long extra = Math.max(0, ChronoUnit.DAYS.between(planned, cmd.actualReturnDate()));
        double surcharge = extra * pricingPolicy.latePerDay(rental.getType());

        rental.markReturned();
        saveRental.save(rental);

        var car = loadCar.byId(rental.getCarId()).orElseThrow();
        car.setAvailable(true);
        saveCar.save(car);

        return new Result(rental.getId(), surcharge);
    }
}
