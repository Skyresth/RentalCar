package com.challenge.RentalCar.rentals.application;

import com.challenge.RentalCar.inventory.domain.CarType;
import com.challenge.RentalCar.rentals.domain.Rental;
import com.challenge.RentalCar.rentals.port.in.ListRentals;
import com.challenge.RentalCar.rentals.port.out.LoadRentals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ListRentalsUseCaseTest {

    private final LoadRentals loadRentals = mock(LoadRentals.class);
    private final ListRentalsUseCase useCase = new ListRentalsUseCase(loadRentals);

    private static Rental rental(Long id, long customerId, long carId, CarType type,
                                 String startDate, int days, double prepaid, Rental.Status status) {
        return Rental.reconstitute(
                id, customerId, carId, type,
                LocalDate.parse(startDate), days, prepaid, status
        );
    }

    @Test
    @DisplayName("No filters -> calls findAll and maps to views")
    void handle_noFilters_callsFindAll() {
        // given
        var rental = rental(1L, 1L, 4L, CarType.SMALL, "2025-09-07", 9, 410.0, Rental.Status.OPEN);
        var rental2 = rental(2L, 1L, 2L, CarType.SUV, "2025-09-07", 9, 1290.0, Rental.Status.RETURNED);
        given(loadRentals.findAll()).willReturn(List.of(rental, rental2));

        var filter = new ListRentals.Filter(Optional.empty(), Optional.empty());

        // when
        var views = useCase.handle(filter);

        // then
        verify(loadRentals).findAll();
        verifyNoMoreInteractions(loadRentals);

        assertThat(views).hasSize(2);
        assertThat(views.get(0).id()).isEqualTo(1L);
        assertThat(views.get(0).type()).isEqualTo("SMALL");
        assertThat(views.get(0).plannedReturnDate()).isEqualTo(LocalDate.parse("2025-09-16"));
        assertThat(views.get(1).status()).isEqualTo("RETURNED");
    }

    @Test
    @DisplayName("Status=OPEN only -> calls findByStatus(OPEN)")
    void handle_statusOnly_open() {
        // given
        var rental = rental(3L, 2L, 1L, CarType.PREMIUM, "2025-09-01", 10, 3000.0, Rental.Status.OPEN);
        given(loadRentals.findByStatus(Rental.Status.OPEN)).willReturn(List.of(rental));

        var filter = new ListRentals.Filter(Optional.empty(), Optional.of("OPEN"));

        // when
        var views = useCase.handle(filter);

        // then
        verify(loadRentals).findByStatus(Rental.Status.OPEN);
        verifyNoMoreInteractions(loadRentals);

        assertThat(views).hasSize(1);
        assertThat(views.get(0).status()).isEqualTo("OPEN");
        assertThat(views.get(0).prepaidAmount()).isEqualTo(3000.0);
    }

    @Test
    @DisplayName("CustomerId only -> calls findByCustomer(customerId)")
    void handle_customerOnly() {
        // given
        var rental = rental(4L, 7L, 3L, CarType.SUV, "2025-09-02", 2, 300.0, Rental.Status.OPEN);
        given(loadRentals.findByCustomer(7L)).willReturn(List.of(rental));

        var filter = new ListRentals.Filter(Optional.of(7L), Optional.empty());

        // when
        var views = useCase.handle(filter);

        // then
        verify(loadRentals).findByCustomer(7L);
        verifyNoMoreInteractions(loadRentals);

        assertThat(views).singleElement()
                .satisfies(v -> {
                    assertThat(v.customerId()).isEqualTo(7L);
                    assertThat(v.carId()).isEqualTo(3L);
                });
    }

    @Test
    @DisplayName("CustomerId + Status=returned (case-insensitive) -> calls findByCustomerAndStatus(RETURNED)")
    void handle_customerAndStatus_returned() {
        // given
        var rental = rental(5L, 1L, 4L, CarType.SMALL, "2025-08-01", 5, 250.0, Rental.Status.RETURNED);
        given(loadRentals.findByCustomerAndStatus(1L, Rental.Status.RETURNED)).willReturn(List.of(rental));

        var filter = new ListRentals.Filter(Optional.of(1L), Optional.of("returned"));

        // when
        var views = useCase.handle(filter);

        // then
        verify(loadRentals).findByCustomerAndStatus(1L, Rental.Status.RETURNED);
        verifyNoMoreInteractions(loadRentals);

        assertThat(views).hasSize(1);
        assertThat(views.get(0).status()).isEqualTo("RETURNED");
        assertThat(views.get(0).plannedReturnDate()).isEqualTo(LocalDate.parse("2025-08-06"));
    }

    @Test
    @DisplayName("Unknown status string -> treated as no status (uses findAll or findByCustomer)")
    void handle_unknownStatus_ignored() {
        // given
        var rental = rental(6L, 9L, 2L, CarType.SUV, "2025-09-03", 1, 150.0, Rental.Status.OPEN);
        given(loadRentals.findAll()).willReturn(List.of(rental));

        var filter = new ListRentals.Filter(Optional.empty(), Optional.of("something-else"));

        // when
        var views = useCase.handle(filter);

        // then
        verify(loadRentals).findAll();
        verifyNoMoreInteractions(loadRentals);

        assertThat(views).hasSize(1);
        assertThat(views.get(0).type()).isEqualTo("SUV");
    }
}
