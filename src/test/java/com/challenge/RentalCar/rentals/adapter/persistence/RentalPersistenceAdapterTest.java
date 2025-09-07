package com.challenge.RentalCar.rentals.adapter.persistence;

import com.challenge.RentalCar.inventory.domain.CarType;
import com.challenge.RentalCar.rentals.domain.Rental;
import com.challenge.RentalCar.rentals.domain.Rental.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class RentalPersistenceAdapterTest {

    private final SpringDataRentalRepository repo = mock(SpringDataRentalRepository.class);
    private final RentalPersistenceAdapter adapter = new RentalPersistenceAdapter(repo);


    @Test
    @DisplayName("save(): new rental -> maps to JPA, saves and assigns generated id back to domain")
    void save_newRental_assignsId() {
        // given
        var start = LocalDate.of(2025, 9, 7);
        var domain = Rental.reconstitute(
                null, 1L, 4L, CarType.SMALL, start, 9, 410.0, Status.OPEN);

        var savedEntity = jpa(100L, 1L, 4L, CarType.SMALL, start, 9, 410.0, JpaRentalEntity.Status.OPEN);
        given(repo.save(any(JpaRentalEntity.class))).willReturn(savedEntity);

        // when
        var result = adapter.save(domain);

        // then
        var captor = ArgumentCaptor.forClass(JpaRentalEntity.class);
        verify(repo).save(captor.capture());
        var passed = captor.getValue();

        assertThat(passed.getId()).isNull();
        assertThat(passed.getCustomerId()).isEqualTo(1L);
        assertThat(passed.getCarId()).isEqualTo(4L);
        assertThat(passed.getType()).isEqualTo(CarType.SMALL);
        assertThat(passed.getStatus()).isEqualTo(JpaRentalEntity.Status.OPEN);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(Status.OPEN);
    }

    @Test
    @DisplayName("save(): existing rental -> maps to JPA and keeps id after save")
    void save_existingRental_keepsId() {
        // given
        var start = LocalDate.of(2025, 9, 7);
        var domain = Rental.reconstitute(
                55L, 2L, 3L, CarType.SUV, start, 2, 300.0, Status.OPEN);

        var savedEntity = jpa(55L, 2L, 3L, CarType.SUV, start, 2, 300.0, JpaRentalEntity.Status.OPEN);
        given(repo.save(any(JpaRentalEntity.class))).willReturn(savedEntity);

        // when
        var result = adapter.save(domain);

        // then
        verify(repo).save(any(JpaRentalEntity.class));
        assertThat(result.getId()).isEqualTo(55L);
    }


    @Test
    @DisplayName("byId(): found -> maps JPA to domain OPEN")
    void byId_found_open() {
        // given
        var start = LocalDate.of(2025, 9, 7);
        var entity = jpa(7L, 1L, 2L, CarType.SUV, start, 9, 1290.0, JpaRentalEntity.Status.OPEN);
        given(repo.findById(7L)).willReturn(Optional.of(entity));

        // when
        var result = adapter.byId(7L);

        // then
        assertThat(result).isPresent();
        var rental = result.get();
        assertThat(rental.getId()).isEqualTo(7L);
        assertThat(rental.getCustomerId()).isEqualTo(1L);
        assertThat(rental.getCarId()).isEqualTo(2L);
        assertThat(rental.getType()).isEqualTo(CarType.SUV);
        assertThat(rental.getDaysBooked()).isEqualTo(9);
        assertThat(rental.getPrepaidAmount()).isEqualTo(1290.0);
        assertThat(rental.getStatus()).isEqualTo(Status.OPEN);
    }

    @Test
    @DisplayName("byId(): found -> maps JPA to domain RETURNED")
    void byId_found_returned() {
        // given
        var start = LocalDate.of(2025, 9, 1);
        var entity = jpa(8L, 2L, 4L, CarType.SMALL, start, 5, 250.0, JpaRentalEntity.Status.RETURNED);
        given(repo.findById(8L)).willReturn(Optional.of(entity));

        // when
        var result = adapter.byId(8L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(Status.RETURNED);
    }

    @Test
    @DisplayName("byId(): not found -> Optional.empty")
    void byId_notFound() {
        // given
        given(repo.findById(999L)).willReturn(Optional.empty());

        // when
        var result = adapter.byId(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll(): maps every JPA entity to domain")
    void findAll_mapsAll() {
        // given
        var rentalEntity = jpa(1L, 1L, 4L, CarType.SMALL, LocalDate.now(), 9, 410.0, JpaRentalEntity.Status.OPEN);
        var rentalEntity2 = jpa(2L, 1L, 2L, CarType.SUV, LocalDate.now(), 9, 1290.0, JpaRentalEntity.Status.RETURNED);
        given(repo.findAll()).willReturn(List.of(rentalEntity, rentalEntity2));

        // when
        var list = adapter.findAll();

        // then
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getId()).isEqualTo(1L);
        assertThat(list.get(1).getStatus()).isEqualTo(Status.RETURNED);
    }

    @Test
    @DisplayName("findByStatus(OPEN): delegates to repo and maps status correctly")
    void findByStatus_open() {
        // given
        var rentalEntity = jpa(3L, 2L, 1L, CarType.PREMIUM, LocalDate.now(), 10, 3000.0, JpaRentalEntity.Status.OPEN);
        given(repo.findByStatus(JpaRentalEntity.Status.OPEN)).willReturn(List.of(rentalEntity));

        // when
        var list = adapter.findByStatus(Status.OPEN);

        // then
        assertThat(list).extracting(Rental::getId).containsExactly(3L);
        assertThat(list.get(0).getStatus()).isEqualTo(Status.OPEN);
    }

    @Test
    @DisplayName("findByCustomer(): delegates to repo and maps list")
    void findByCustomer() {
        // given
        var rentalEntity = jpa(4L, 1L, 2L, CarType.SUV, LocalDate.now(), 2, 300.0, JpaRentalEntity.Status.OPEN);
        var rentalEntity2 = jpa(5L, 1L, 4L, CarType.SMALL, LocalDate.now(), 9, 410.0, JpaRentalEntity.Status.RETURNED);
        given(repo.findByCustomerId(1L)).willReturn(List.of(rentalEntity, rentalEntity2));

        // when
        var list = adapter.findByCustomer(1L);

        // then
        assertThat(list).hasSize(2);
        assertThat(list).extracting(Rental::getCustomerId).containsOnly(1L);
    }

    @Test
    @DisplayName("findByCustomerAndStatus(RETURNED): delegates and maps")
    void findByCustomerAndStatus_returned() {
        // given
        var rentalEntity = jpa(6L, 2L, 4L, CarType.SMALL, LocalDate.now(), 9, 410.0, JpaRentalEntity.Status.RETURNED);
        given(repo.findByCustomerIdAndStatus(2L, JpaRentalEntity.Status.RETURNED)).willReturn(List.of(rentalEntity));

        // when
        var list = adapter.findByCustomerAndStatus(2L, Status.RETURNED);

        // then
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getStatus()).isEqualTo(Status.RETURNED);
        assertThat(list.get(0).getCustomerId()).isEqualTo(2L);
    }

    private static JpaRentalEntity jpa(Long id, long customerId, long carId, CarType type,
                                       LocalDate start, int days, double prepaid,
                                       JpaRentalEntity.Status status) {
        var jpaRentalEntity = new JpaRentalEntity();
        jpaRentalEntity.setId(id);
        jpaRentalEntity.setCustomerId(customerId);
        jpaRentalEntity.setCarId(carId);
        jpaRentalEntity.setType(type);
        jpaRentalEntity.setStartDate(start);
        jpaRentalEntity.setDaysBooked(days);
        jpaRentalEntity.setPrepaidAmount(prepaid);
        jpaRentalEntity.setStatus(status);
        return jpaRentalEntity;
    }
}
