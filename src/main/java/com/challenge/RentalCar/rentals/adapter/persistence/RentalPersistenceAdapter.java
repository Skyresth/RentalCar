package com.challenge.RentalCar.rentals.adapter.persistence;

import com.challenge.RentalCar.rentals.domain.Rental;
import com.challenge.RentalCar.rentals.port.out.LoadRental;
import com.challenge.RentalCar.rentals.port.out.SaveRental;
import com.challenge.RentalCar.rentals.port.out.LoadRentals;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RentalPersistenceAdapter implements LoadRental, SaveRental, LoadRentals {
    private final SpringDataRentalRepository repo;

    @Override
    public Optional<Rental> byId(long id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public Rental save(Rental rental) {
        var saved = repo.save(toEntity(rental));
        if (rental.getId() == null) rental.assignId(saved.getId());
        return rental;
    }

    @Override
    public List<Rental> findAll() {
        return repo.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Rental> findByStatus(Rental.Status status) {
        var statusCheck = status == Rental.Status.OPEN
                ? JpaRentalEntity.Status.OPEN : JpaRentalEntity.Status.RETURNED;
        return repo.findByStatus(statusCheck).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Rental> findByCustomer(long customerId) {
        return repo.findByCustomerId(customerId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Rental> findByCustomerAndStatus(long customerId, Rental.Status status) {
        var statusCheck = status == Rental.Status.OPEN
                ? JpaRentalEntity.Status.OPEN : JpaRentalEntity.Status.RETURNED;
        return repo.findByCustomerIdAndStatus(customerId, statusCheck).stream().map(this::toDomain).toList();
    }

    private Rental toDomain(JpaRentalEntity jpaRentalEntity) {
        return Rental.reconstitute(
                jpaRentalEntity.getId(), jpaRentalEntity.getCustomerId(), jpaRentalEntity.getCarId(), jpaRentalEntity.getType(),
                jpaRentalEntity.getStartDate(), jpaRentalEntity.getDaysBooked(), jpaRentalEntity.getPrepaidAmount(),
                jpaRentalEntity.getStatus() == JpaRentalEntity.Status.OPEN ? Rental.Status.OPEN : Rental.Status.RETURNED
        );
    }

    private JpaRentalEntity toEntity(Rental rental) {
        var jpaRentalEntity = new JpaRentalEntity();
        jpaRentalEntity.setId(rental.getId());
        jpaRentalEntity.setCustomerId(rental.getCustomerId());
        jpaRentalEntity.setCarId(rental.getCarId());
        jpaRentalEntity.setType(rental.getType());
        jpaRentalEntity.setStartDate(rental.getStartDate());
        jpaRentalEntity.setDaysBooked(rental.getDaysBooked());
        jpaRentalEntity.setPrepaidAmount(rental.getPrepaidAmount());
        jpaRentalEntity.setStatus(rental.getStatus() == Rental.Status.OPEN
                ? JpaRentalEntity.Status.OPEN : JpaRentalEntity.Status.RETURNED);
        return jpaRentalEntity;
    }
}
