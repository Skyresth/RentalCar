package com.challenge.RentalCar.rentals.application;

import com.challenge.RentalCar.inventory.domain.Car;
import com.challenge.RentalCar.inventory.domain.CarType;
import com.challenge.RentalCar.rentals.domain.Rental;
import com.challenge.RentalCar.rentals.domain.rules.LoyaltyPolicy;
import com.challenge.RentalCar.rentals.domain.rules.PricingPolicy;
import com.challenge.RentalCar.rentals.port.in.RentCar;
import com.challenge.RentalCar.rentals.port.out.LoadCar;
import com.challenge.RentalCar.rentals.port.out.LoadCustomer;
import com.challenge.RentalCar.rentals.port.out.SaveCar;
import com.challenge.RentalCar.rentals.port.out.SaveCustomer;
import com.challenge.RentalCar.rentals.port.out.SaveRental;
import com.challenge.RentalCar.shared.error.NotFoundException;
import com.challenge.RentalCar.customers.domain.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

class RentCarUseCaseTest {

    private final LoadCar loadCar = mock(LoadCar.class);
    private final SaveCar saveCar = mock(SaveCar.class);
    private final LoadCustomer loadCustomer = mock(LoadCustomer.class);
    private final SaveCustomer saveCustomer = mock(SaveCustomer.class);
    private final SaveRental saveRental = mock(SaveRental.class);
    private final PricingPolicy pricingPolicy = mock(PricingPolicy.class);
    private final LoyaltyPolicy loyaltyPolicy = mock(LoyaltyPolicy.class);

    private final RentCarUseCase useCase = new RentCarUseCase(
            loadCar, saveCar, loadCustomer, saveCustomer, saveRental, pricingPolicy, loyaltyPolicy
    );


    @Test
    @DisplayName("handle(): rents available car, saves rental, flips availability, adds points and returns result")
    void handle_happyPath() {
        // given
        long customerId = 1L;
        long carId = 4L;
        int days = 9;

        var car = new Car(carId, "Seat", "Ibiza", CarType.SMALL, true);
        var customer = new Customer(customerId, "Alice", 0);

        given(loadCar.byId(carId)).willReturn(Optional.of(car));
        given(loadCustomer.byId(customerId)).willReturn(Optional.of(customer));
        given(pricingPolicy.basePrice(CarType.SMALL, days)).willReturn(410.0);
        given(loyaltyPolicy.pointsFor(CarType.SMALL)).willReturn(1);

        willAnswer(inv -> {
            Rental rental = inv.getArgument(0);
            return Rental.reconstitute(
                    100L, rental.getCustomerId(), rental.getCarId(), rental.getType(),
                    rental.getStartDate(), rental.getDaysBooked(), rental.getPrepaidAmount(), Rental.Status.OPEN
            );
        }).given(saveRental).save(any(Rental.class));

        // when
        RentCar.Result result = useCase.handle(new RentCar.Command(customerId, carId, days));

        // then
        // result
        assertThat(result.rentalId()).isEqualTo(100L);
        assertThat(result.prepaidAmount()).isEqualTo(410.0);
        assertThat(result.loyaltyPointsAwarded()).isEqualTo(1);

        var carCaptor = ArgumentCaptor.forClass(Car.class);
        verify(saveCar).save(carCaptor.capture());
        assertThat(carCaptor.getValue().isAvailable()).isFalse();

        var customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(saveCustomer).save(customerCaptor.capture());
        assertThat(customerCaptor.getValue().getPoints()).isEqualTo(1);

        var rentalCaptor = ArgumentCaptor.forClass(Rental.class);
        verify(saveRental).save(rentalCaptor.capture());
        var passedRental = rentalCaptor.getValue();
        assertThat(passedRental.getCustomerId()).isEqualTo(customerId);
        assertThat(passedRental.getCarId()).isEqualTo(carId);
        assertThat(passedRental.getType()).isEqualTo(CarType.SMALL);
        assertThat(passedRental.getDaysBooked()).isEqualTo(days);
        assertThat(passedRental.getPrepaidAmount()).isEqualTo(410.0);
    }

    @Test
    @DisplayName("handle(): days <= 0 -> BAD_REQUEST")
    void handle_invalidDays() {
        // given
        var cmd = new RentCar.Command(1L, 2L, 0);

        // when
        Throwable thrown = catchThrowable(() -> useCase.handle(cmd));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        var exception = (ResponseStatusException) thrown;
        assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(exception.getReason()).isEqualTo("days must be > 0");

        verifyNoInteractions(loadCar, loadCustomer, saveRental, saveCar, saveCustomer, pricingPolicy, loyaltyPolicy);
    }


    @Test
    @DisplayName("handle(): car not found -> NotFoundException")
    void handle_carNotFound() {
        // given
        given(loadCar.byId(10L)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> useCase.handle(new RentCar.Command(1L, 10L, 5)));

        // then
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Car not found: 10");

        verify(loadCustomer, never()).byId(anyLong());
        verifyNoInteractions(saveRental, saveCar, saveCustomer, pricingPolicy, loyaltyPolicy);
    }

    @Test
    @DisplayName("handle(): customer not found -> NotFoundException")
    void handle_customerNotFound() {
        // given
        var car = new Car(2L, "BMW", "7", CarType.PREMIUM, true);
        given(loadCar.byId(2L)).willReturn(Optional.of(car));
        given(loadCustomer.byId(999L)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> useCase.handle(new RentCar.Command(999L, 2L, 3)));

        // then
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer not found: 999");

        verifyNoInteractions(saveRental, saveCar, saveCustomer, pricingPolicy, loyaltyPolicy);
    }

    @Test
    @DisplayName("handle(): car unavailable -> CONFLICT")
    void handle_carUnavailable() {
        // given
        var car = new Car(3L, "Kia", "Sorento", CarType.SUV, false);
        var customer = new Customer(1L, "Alice", 0);

        given(loadCar.byId(3L)).willReturn(Optional.of(car));
        given(loadCustomer.byId(1L)).willReturn(Optional.of(customer));

        // when
        Throwable thrown = catchThrowable(() -> useCase.handle(new RentCar.Command(1L, 3L, 2)));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        var exception = (ResponseStatusException) thrown;
        assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(exception.getReason()).isEqualTo("car is not available");

        verifyNoInteractions(saveRental, saveCar, saveCustomer, pricingPolicy, loyaltyPolicy);
    }
}
