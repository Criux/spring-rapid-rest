package com.kmarinos.springrapidrest.example.car;

import com.kmarinos.springrapidrest.config.EntityRootPath;
import com.kmarinos.springrapidrest.controller.StandardEntityController;
import com.kmarinos.springrapidrest.domain.repository.TrackedEntityHistoryRepository;
import com.kmarinos.springrapidrest.dto.EntityDAO;
import com.kmarinos.springrapidrest.dto.EntityHistoryDAO;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@RestController
@RequestMapping("api/v1")
@EntityRootPath("car")
public class CarRestController extends StandardEntityController<Car> {

  protected CarRestController(
      TrackedEntityHistoryRepository<Car> trackedEntityHistoryRepository,
      EntityDAO<Car> entityDAO,
      EntityHistoryDAO<Car> entityHistoryDAO,
      RequestMappingHandlerMapping requestMappingHandlerMapping,
      JpaRepository<Car, String> repository,
      ApplicationContext applicationContext) {
    super(trackedEntityHistoryRepository, entityDAO, entityHistoryDAO, requestMappingHandlerMapping,
        repository, applicationContext);
  }
}
