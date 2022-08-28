package com.kmarinos.springrapidrest.controller;

import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistory;
import com.kmarinos.springrapidrest.domain.repository.ExpandedTrackedEntityHistoryRepository;
import com.kmarinos.springrapidrest.domain.repository.TrackedEntityHistoryRepository;
import com.kmarinos.springrapidrest.dto.EntityDAO;
import com.kmarinos.springrapidrest.dto.EntityHistoryDAO;
import com.kmarinos.springrapidrest.exceptionHandling.exceptions.EntityNotFoundException;
import java.util.List;
import java.util.function.Function;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class StandardEntityController<T extends TrackedEntity> extends AbstractEntityController<T> {

  private final JpaRepository<T, String> repository;
  private ExpandedTrackedEntityHistoryRepository<T> expandedTrackedEntityHistoryRepository = null;

  protected StandardEntityController(
      TrackedEntityHistoryRepository<T> trackedEntityHistoryRepository,
      EntityDAO<T> entityDAO,
      EntityHistoryDAO<T> entityHistoryDAO,
      RequestMappingHandlerMapping requestMappingHandlerMapping,
      JpaRepository<T, String> repository, ApplicationContext applicationContext) {
    super(trackedEntityHistoryRepository, entityDAO, entityHistoryDAO,
        requestMappingHandlerMapping);
    this.repository = repository;
    String repositoryName = Character.toLowerCase(this.entityClass.getSimpleName().charAt(0)) +
        this.entityClass.getSimpleName().substring(1) +
        "HistoryRepository";
    try {
      this.expandedTrackedEntityHistoryRepository = (ExpandedTrackedEntityHistoryRepository<T>) applicationContext.getBean(
          repositoryName, ExpandedTrackedEntityHistoryRepository.class);
    } catch (BeansException e) {
      //Ignored...
    }
  }

  @Override
  public Function<String, List<T>> handleFetchAllEntities() {
    return search -> repository.findAll().stream().filter(TrackedEntity::isActive).toList();
  }

  @Override
  public Function<String, T> handleFetchEntityWithId() {
    return id -> repository.findById(id).filter(TrackedEntity::isActive)
        .orElseThrow(() -> new EntityNotFoundException(this.entityClass, "id", id));
  }

  @Override
  public Function<T, List<TrackedEntityHistory<T>>> handleFetchHistoryForEntity() {
    return expandedTrackedEntityHistoryRepository::findAllByEntity;
  }
}
