package com.challenge.RentalCar.rentals.application;

import com.challenge.RentalCar.rentals.domain.Rental;
import com.challenge.RentalCar.rentals.port.in.ListRentals;
import com.challenge.RentalCar.rentals.port.out.LoadRentals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListRentalsUseCase implements ListRentals {
    private final LoadRentals loadRentals;

    @Override
    public List<View> handle(Filter filter) {
        var rentals = switch (statusFrom(filter)) {
            case null -> filter.customerId().isPresent()
                    ? loadRentals.findByCustomer(filter.customerId().get())
                    : loadRentals.findAll();
            case Rental.Status s -> filter.customerId().isPresent()
                    ? loadRentals.findByCustomerAndStatus(filter.customerId().get(), s)
                    : loadRentals.findByStatus(s);
        };
        return rentals.stream().map(this::toView).toList();
    }

    private Rental.Status statusFrom(Filter filter) {
        return filter.status().map(String::toUpperCase).map(s -> switch (s) {
            case "OPEN" -> Rental.Status.OPEN;
            case "RETURNED" -> Rental.Status.RETURNED;
            default -> null;
        }).orElse(null);
    }

    private View toView(Rental rental) {
        var planned = rental.getStartDate().plusDays(rental.getDaysBooked());
        return new View(
                rental.getId(), rental.getCustomerId(), rental.getCarId(),
                rental.getType().name(),
                rental.getStartDate(), rental.getDaysBooked(),
                planned, rental.getPrepaidAmount(),
                rental.getStatus().name()
        );
    }
}
