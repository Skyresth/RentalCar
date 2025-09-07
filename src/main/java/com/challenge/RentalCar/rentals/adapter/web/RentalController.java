package com.challenge.RentalCar.rentals.adapter.web;

import com.challenge.RentalCar.rentals.port.in.RentCar;
import com.challenge.RentalCar.rentals.port.in.ReturnCar;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentCar rentCar;
    private final ReturnCar returnCar;

    @PostMapping
    public RentCar.Result rent(@RequestBody RentCar.Command cmd) {
        return rentCar.handle(cmd);
    }

    @PostMapping("/{id}/return")
    public ReturnCar.Result doReturn(@PathVariable("id") long id,
                                     @RequestBody ReturnCar.Command body) {
        var cmd = new ReturnCar.Command(id, body.actualReturnDate());
        return returnCar.handle(cmd);
    }
}

