package com.kmarinos.springrapidrest.example.car;

import com.kmarinos.springrapidrest.controller.StandardEntityController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class CarRestController extends StandardEntityController<Car> {

}
