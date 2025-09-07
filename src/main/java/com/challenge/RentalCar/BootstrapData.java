package com.challenge.RentalCar;

import com.challenge.RentalCar.customers.adapter.persistence.JpaCustomerEntity;
import com.challenge.RentalCar.customers.adapter.persistence.SpringDataCustomerRepository;
import com.challenge.RentalCar.inventory.adapter.persistence.JpaCarEntity;
import com.challenge.RentalCar.inventory.adapter.persistence.SpringDataCarRepository;
import com.challenge.RentalCar.inventory.domain.CarType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("dev-h2")
@Configuration
class BootstrapData {
    @Bean
    CommandLineRunner seed(
            SpringDataCustomerRepository customers,
            SpringDataCarRepository cars) {
        return args -> {
            if (customers.count() == 0) {
                var a = new JpaCustomerEntity();
                a.setName("Alice");
                a.setPoints(0);
                customers.save(a);
                var b = new JpaCustomerEntity();
                b.setName("Bob");
                b.setPoints(0);
                customers.save(b);
            }
            if (cars.count() == 0) {
                cars.save(car("BMW", "7", CarType.PREMIUM));
                cars.save(car("Kia", "Sorento", CarType.SUV));
                cars.save(car("Nissan", "Juke", CarType.SUV));
                cars.save(car("Seat", "Ibiza", CarType.SMALL));
            }
        };
    }

    private static JpaCarEntity car(String brand, String model, CarType type) {
        var e = new JpaCarEntity();
        e.setBrand(brand);
        e.setModel(model);
        e.setType(type);
        e.setAvailable(true);
        return e;
    }
}
