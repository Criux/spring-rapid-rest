package com.kmarinos.springrapidrest.example;

import com.kmarinos.springrapidrest.domain.EntityHistoryFactory;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistory;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistoryChangeType;
import com.kmarinos.springrapidrest.domain.repository.TrackedEntityHistoryRepository;
import com.kmarinos.springrapidrest.example.car.Car;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitExample {
  private final TrackedEntityHistoryRepository trackedEntityHistoryRepository;

  @Bean
  public CommandLineRunner init(){
    return args -> {
      var start =System.currentTimeMillis();

      var cars = createCars();
      updateCars(cars);
      deleteCars(cars);

      log.info("Initialized examples in {} ms",(System.currentTimeMillis()-start));
    };
  }

  private List<Car> createCars() {
    var car1 =createCar(Car.builder()
          .manufacturer("Mercedes Benz")
          .model("A-Class Sedan")
          .year(2019)
          .weight(1260.0)
          .color("red")
          .build());
    var car2 =createCar(Car.builder()
        .manufacturer("Volkswagen")
        .model("Golf 8")
        .year(2020)
        .weight(1164.0)
        .color("white")
        .build());
    var car3 =createCar(Car.builder()
        .manufacturer("Skoda")
        .model("Fabia 4")
        .year(2021)
        .weight(1142.0)
        .color("orange")
        .build());


    return List.of(car1,car2,car3);
  }
  private void updateCars(List<Car> cars){
    var carUPDATE = EntityHistoryFactory.forEntity(cars.get(0));
    carUPDATE.setChangeType(TrackedEntityHistoryChangeType.UPDATE);
    carUPDATE.setValue("year",2020);
    trackedEntityHistoryRepository.save(carUPDATE);
  }
  private void deleteCars(List<Car> cars){
    var carDELETE = EntityHistoryFactory.forEntity(cars.get(cars.size()-1));
    carDELETE.setChangeType(TrackedEntityHistoryChangeType.DELETE);
    trackedEntityHistoryRepository.save(carDELETE);
  }
  private Car createCar(Car car) {
    var carCREATE = EntityHistoryFactory.forEntity(Car.class);
    carCREATE.setChangeType(TrackedEntityHistoryChangeType.CREATE);
    for (Field declaredField : car.getClass().getDeclaredFields()) {
      try {
        declaredField.setAccessible(true);
        if(declaredField.get(car) !=null){
          carCREATE.setValue(declaredField.getName(), PropertyUtils.getProperty(car,
              declaredField.getName()));
        }
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }
    return ((TrackedEntityHistory<Car>)trackedEntityHistoryRepository.save(carCREATE)).getEntity();
  }
}
