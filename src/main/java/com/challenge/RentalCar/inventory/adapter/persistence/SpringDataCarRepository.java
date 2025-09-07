package com.challenge.RentalCar.inventory.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCarRepository extends JpaRepository<JpaCarEntity, Long> {
}
