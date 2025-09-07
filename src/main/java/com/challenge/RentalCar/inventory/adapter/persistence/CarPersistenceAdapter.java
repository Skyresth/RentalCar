package com.challenge.RentalCar.inventory.adapter.persistence;

import com.challenge.RentalCar.inventory.domain.Car;
import com.challenge.RentalCar.rentals.port.out.LoadCar;
import com.challenge.RentalCar.rentals.port.out.SaveCar;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CarPersistenceAdapter implements LoadCar, SaveCar {
    private final SpringDataCarRepository repo;

    @Override
    public Optional<Car> byId(long id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public Car save(Car car) {
        var saved = repo.save(toEntity(car));
        car.setId(saved.getId());
        return car;
    }

    private Car toDomain(JpaCarEntity carEntity) {
        return new Car(carEntity.getId(), carEntity.getBrand(), carEntity.getModel(), carEntity.getType(), carEntity.isAvailable());
    }

    private JpaCarEntity toEntity(Car car) {
        var jpaCarEntity = new JpaCarEntity();
        jpaCarEntity.setId(car.getId());
        jpaCarEntity.setBrand(car.getBrand());
        jpaCarEntity.setModel(car.getModel());
        jpaCarEntity.setType(car.getType());
        jpaCarEntity.setAvailable(car.isAvailable());
        return jpaCarEntity;
    }
}
