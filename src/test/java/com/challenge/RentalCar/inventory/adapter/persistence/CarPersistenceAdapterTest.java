package com.challenge.RentalCar.inventory.adapter.persistence;

import com.challenge.RentalCar.inventory.domain.Car;
import com.challenge.RentalCar.inventory.domain.CarType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class CarPersistenceAdapterTest {

    private final SpringDataCarRepository repo = mock(SpringDataCarRepository.class);
    private final CarPersistenceAdapter adapter = new CarPersistenceAdapter(repo);

    @Test
    @DisplayName("save() should map domain Car to JpaCarEntity, save it, and update id in domain")
    void saveCar_updatesIdAndCallsRepo() {
        // given
        Car domainCar = new Car(null, "BMW", "7", CarType.PREMIUM, true);

        JpaCarEntity savedEntity = new JpaCarEntity();
        savedEntity.setId(42L);
        savedEntity.setBrand("BMW");
        savedEntity.setModel("7");
        savedEntity.setType(CarType.PREMIUM);
        savedEntity.setAvailable(true);

        given(repo.save(any(JpaCarEntity.class))).willReturn(savedEntity);

        // when
        Car result = adapter.save(domainCar);

        // then
        ArgumentCaptor<JpaCarEntity> captor = ArgumentCaptor.forClass(JpaCarEntity.class);
        verify(repo).save(captor.capture());
        JpaCarEntity passedToRepo = captor.getValue();

        assertThat(passedToRepo.getId()).isNull();
        assertThat(passedToRepo.getBrand()).isEqualTo("BMW");
        assertThat(passedToRepo.getType()).isEqualTo(CarType.PREMIUM);

        assertThat(result.getId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("byId() should map JpaCarEntity to domain Car when found")
    void byId_returnsMappedDomainCar() {
        // given
        JpaCarEntity entity = new JpaCarEntity();
        entity.setId(100L);
        entity.setBrand("Kia");
        entity.setModel("Sorento");
        entity.setType(CarType.SUV);
        entity.setAvailable(false);

        given(repo.findById(100L)).willReturn(Optional.of(entity));

        // when
        Optional<Car> result = adapter.byId(100L);

        // then
        assertThat(result).isPresent();
        Car car = result.get();
        assertThat(car.getId()).isEqualTo(100L);
        assertThat(car.getBrand()).isEqualTo("Kia");
        assertThat(car.getType()).isEqualTo(CarType.SUV);
        assertThat(car.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("byId() should return Optional.empty when repo has no entity")
    void byId_returnsEmptyWhenNotFound() {
        // given
        given(repo.findById(999L)).willReturn(Optional.empty());

        // when
        Optional<Car> result = adapter.byId(999L);

        // then
        assertThat(result).isEmpty();
    }
}
