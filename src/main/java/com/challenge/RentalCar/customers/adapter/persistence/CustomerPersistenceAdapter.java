package com.challenge.RentalCar.customers.adapter.persistence;

import com.challenge.RentalCar.customers.domain.Customer;
import com.challenge.RentalCar.rentals.port.out.LoadCustomer;
import com.challenge.RentalCar.rentals.port.out.SaveCustomer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements LoadCustomer, SaveCustomer {
    private final SpringDataCustomerRepository repo;

    @Override
    public Optional<Customer> byId(long id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public Customer save(Customer c) {
        var saved = repo.save(toEntity(c));
        c.setId(saved.getId());
        return c;
    }

    private Customer toDomain(JpaCustomerEntity e) {
        return new Customer(e.getId(), e.getName(), e.getPoints());
    }

    private JpaCustomerEntity toEntity(Customer c) {
        var e = new JpaCustomerEntity();
        e.setId(c.getId());
        e.setName(c.getName());
        e.setPoints(c.getPoints());
        return e;
    }
}
