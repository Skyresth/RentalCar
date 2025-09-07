package com.challenge.RentalCar.inventory.adapter.persistence;

import com.challenge.RentalCar.inventory.domain.CarType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cars")
@Getter
@Setter
public class JpaCarEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String brand;
    private String model;
    @Enumerated(EnumType.STRING)
    private CarType type;
    private boolean available;
}
