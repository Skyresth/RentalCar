package com.challenge.RentalCar.rentals.application;

import com.challenge.RentalCar.rentals.domain.Rental;
import com.challenge.RentalCar.rentals.domain.rules.LoyaltyPolicy;
import com.challenge.RentalCar.rentals.domain.rules.PricingPolicy;
import com.challenge.RentalCar.rentals.port.in.RentCar;
import com.challenge.RentalCar.rentals.port.out.LoadCar;
import com.challenge.RentalCar.rentals.port.out.SaveCar;
import com.challenge.RentalCar.rentals.port.out.LoadCustomer;
import com.challenge.RentalCar.rentals.port.out.SaveCustomer;
import com.challenge.RentalCar.rentals.port.out.SaveRental;
import com.challenge.RentalCar.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RentCarUseCase implements RentCar {

    private final LoadCar loadCar;
    private final SaveCar saveCar;
    private final LoadCustomer loadCustomer;
    private final SaveCustomer saveCustomer;
    private final SaveRental saveRental;
    private final PricingPolicy pricingPolicy;
    private final LoyaltyPolicy loyaltyPolicy;

    @Override
    @Transactional
    public Result handle(Command cmd) {
        // 1) Validate
        if (cmd.days() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "days must be > 0");
        }

        // 2) Load aggregate roots (404 if missing)
        var car = loadCar.byId(cmd.carId())
                .orElseThrow(() -> new NotFoundException("Car", cmd.carId()));
        var customer = loadCustomer.byId(cmd.customerId())
                .orElseThrow(() -> new NotFoundException("Customer", cmd.customerId()));

        // 3) Business rules
        if (!car.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "car is not available");
        }

        double prepaid = pricingPolicy.basePrice(car.getType(), cmd.days());
        int points = loyaltyPolicy.pointsFor(car.getType());

        // 4) Persist changes
        var rental = Rental.open(customer.getId(), car.getId(), car.getType(), cmd.days(), prepaid);
        var persisted = saveRental.save(rental);

        car.setAvailable(false);
        saveCar.save(car);

        customer.addPoints(points);
        saveCustomer.save(customer);

        // 5) Return result DTO
        return new Result(persisted.getId(), prepaid, points);
    }
}
