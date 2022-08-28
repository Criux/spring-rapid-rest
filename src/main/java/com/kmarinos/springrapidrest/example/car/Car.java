package com.kmarinos.springrapidrest.example.car;

import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import javax.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Car extends TrackedEntity {

  String color;
  String model;
  String manufacturer;
  Double weight;
  Integer year;

}
