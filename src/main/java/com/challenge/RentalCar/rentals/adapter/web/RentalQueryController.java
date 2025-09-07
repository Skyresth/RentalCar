package com.challenge.RentalCar.rentals.adapter.web;

import com.challenge.RentalCar.rentals.port.in.ListRentals;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalQueryController {
    private final ListRentals listRentals;

    /**
     * GET /rentals?status=OPEN|RETURNED&customerId=1
     */
    @GetMapping
    public List<ListRentals.View> history(@RequestParam Optional<String> status,
                                          @RequestParam Optional<Long> customerId) {
        var filter = new ListRentals.Filter(customerId, status);
        return listRentals.handle(filter);
    }
}
