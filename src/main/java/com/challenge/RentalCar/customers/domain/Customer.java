package com.challenge.RentalCar.customers.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private Long id;
    private String name;
    private int points;

    public void addPoints(int p) {
        this.points += p;
    }
}
