package com.kmarinos.springrapidrest.example.car;

import com.kmarinos.springrapidrest.converter.StandardHistoryToStateConverter;
import org.springframework.stereotype.Component;

@Component
public class CarHistoryToStateConverter extends StandardHistoryToStateConverter<Car> {

}
