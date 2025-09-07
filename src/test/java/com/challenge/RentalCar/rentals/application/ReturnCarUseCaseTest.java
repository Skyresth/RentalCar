package com.challenge.RentalCar.rentals.application;

import com.challenge.RentalCar.inventory.domain.Car;
import com.challenge.RentalCar.inventory.domain.CarType;
import com.challenge.RentalCar.rentals.domain.Rental;
import com.challenge.RentalCar.rentals.port.in.ReturnCar;
import com.challenge.RentalCar.rentals.port.out.LoadCar;
import com.challenge.RentalCar.rentals.port.out.LoadRental;
import com.challenge.RentalCar.rentals.port.out.SaveCar;
import com.challenge.RentalCar.rentals.port.out.SaveRental;
import com.challenge.RentalCar.rentals.domain.rules.PricingPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

class ReturnCarUseCaseTest {

    private final LoadRental loadRental = mock(LoadRental.class);
    private final SaveRental saveRental = mock(SaveRental.class);
    private final LoadCar loadCar = mock(LoadCar.class);
    private final SaveCar saveCar = mock(SaveCar.class);
    private final PricingPolicy pricingPolicy = mock(PricingPolicy.class);

    private final ReturnCarUseCase useCase =
            new ReturnCarUseCase(loadRental, saveRental, loadCar, saveCar, pricingPolicy);

    private static Rental rental(Long id, long customerId, long carId, CarType type,
                                 String startDate, int days, double prepaid, Rental.Status status) {
        return Rental.reconstitute(
                id, customerId, carId, type,
                LocalDate.parse(startDate), days, prepaid, status
        );
    }

    @Test
    @DisplayName("On-time return -> surcharge 0, rental marked RETURNED, car set available=true")
    void handle_onTimeReturn() {
        // given
        var rental = rental(1L, 1L, 4L, CarType.SMALL, "2025-09-07", 9, 410.0, Rental.Status.OPEN);
        var planned = LocalDate.of(2025, 9, 16); // 7th + 9 days

        given(pricingPolicy.latePerDay(CarType.SMALL)).willReturn(65.0);
        given(loadRental.byId(1L)).willReturn(Optional.of(rental));

        var car = new Car(4L, "Seat", "Ibiza", CarType.SMALL, false);
        given(loadCar.byId(4L)).willReturn(Optional.of(car));

        willAnswer(inv -> inv.getArgument(0)).given(saveRental).save(any(Rental.class));
        willAnswer(inv -> inv.getArgument(0)).given(saveCar).save(any(Car.class));

        // when
        ReturnCar.Result result =
                useCase.handle(new ReturnCar.Command(1L, planned));

        // then
        assertThat(result.rentalId()).isEqualTo(1L);
        assertThat(result.surcharge()).isEqualTo(0.0);

        var rentalCap = ArgumentCaptor.forClass(Rental.class);
        verify(saveRental).save(rentalCap.capture());
        assertThat(rentalCap.getValue().getStatus()).isEqualTo(Rental.Status.RETURNED);

        var carCap = ArgumentCaptor.forClass(Car.class);
        verify(saveCar).save(carCap.capture());
        assertThat(carCap.getValue().isAvailable()).isTrue();

        verify(pricingPolicy).latePerDay(CarType.SMALL);
    }

    @Test
    @DisplayName("Late return by 2 days (SMALL) -> surcharge = 2 * latePerDay")
    void handle_lateReturn_twoDays() {
        // given
        var rental = rental(2L, 1L, 4L, CarType.SMALL, "2025-09-07", 9, 410.0, Rental.Status.OPEN);
        var actual = LocalDate.of(2025, 9, 18);

        given(loadRental.byId(2L)).willReturn(Optional.of(rental));
        given(pricingPolicy.latePerDay(CarType.SMALL)).willReturn(65.0);

        var car = new Car(4L, "Seat", "Ibiza", CarType.SMALL, false);
        given(loadCar.byId(4L)).willReturn(Optional.of(car));

        willAnswer(inv -> inv.getArgument(0)).given(saveRental).save(any(Rental.class));
        willAnswer(inv -> inv.getArgument(0)).given(saveCar).save(any(Car.class));

        // when
        ReturnCar.Result result =
                useCase.handle(new ReturnCar.Command(2L, actual));

        // then
        assertThat(result.rentalId()).isEqualTo(2L);
        assertThat(result.surcharge()).isEqualTo(130.0);

        verify(saveRental).save(any(Rental.class));
        verify(saveCar).save(argThat(Car::isAvailable));
        verify(pricingPolicy).latePerDay(CarType.SMALL);
    }

    @Test
    @DisplayName("Idempotent: already RETURNED -> surcharge 0 and no further actions")
    void handle_alreadyReturned_isIdempotent() {
        // given
        var rental = rental(3L, 1L, 2L, CarType.SUV, "2025-09-01", 10, 1290.0, Rental.Status.RETURNED);
        given(loadRental.byId(3L)).willReturn(Optional.of(rental));

        // when
        ReturnCar.Result result =
                useCase.handle(new ReturnCar.Command(3L, LocalDate.of(2025, 9, 20)));

        // then
        assertThat(result.rentalId()).isEqualTo(3L);
        assertThat(result.surcharge()).isEqualTo(0.0);

        verify(saveRental, never()).save(any());
        verify(loadCar, never()).byId(anyLong());
        verify(saveCar, never()).save(any());
        verify(pricingPolicy, never()).latePerDay(any());
    }

    @Test
    @DisplayName("Rental not found -> throws NoSuchElementException")
    void handle_rentalNotFound() {
        // given
        given(loadRental.byId(999L)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() ->
                useCase.handle(new ReturnCar.Command(999L, LocalDate.of(2025, 9, 10))));

        // then
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);

        verify(saveRental, never()).save(any());
        verify(loadCar, never()).byId(anyLong());
        verify(saveCar, never()).save(any());
        verify(pricingPolicy, never()).latePerDay(any());
    }

    @Test
    @DisplayName("Car not found after saving rental as RETURNED -> throws NoSuchElementException (rental already saved)")
    void handle_carNotFound_afterSavingRental() {
        // given
        var rental = rental(5L, 1L, 999L, CarType.PREMIUM, "2025-09-01", 2, 600.0, Rental.Status.OPEN);
        given(loadRental.byId(5L)).willReturn(Optional.of(rental));
        given(pricingPolicy.latePerDay(CarType.PREMIUM)).willReturn(360.0);

        given(loadCar.byId(999L)).willReturn(Optional.empty());

        willAnswer(inv -> inv.getArgument(0)).given(saveRental).save(any(Rental.class));

        // when
        Throwable thrown = catchThrowable(() ->
                useCase.handle(new ReturnCar.Command(5L, LocalDate.of(2025, 9, 3))));

        // then
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);

        verify(saveRental).save(argThat(rn -> rn.getStatus() == Rental.Status.RETURNED));
        verify(loadCar).byId(999L);
        verify(saveCar, never()).save(any());
    }
}
