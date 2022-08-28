package com.kmarinos.springrapidrest.controller;

import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistory;
import com.kmarinos.springrapidrest.domain.repository.ExpandedTrackedEntityHistoryRepository;
import com.kmarinos.springrapidrest.exceptionHandling.exceptions.EntityNotFoundException;
import java.util.List;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;

public class StandardEntityController<T extends TrackedEntity> extends AbstractEntityController<T> {
  @Autowired
  protected JpaRepository<T, String> repository;
  @Autowired
  private ApplicationContext applicationContext;
  protected ExpandedTrackedEntityHistoryRepository<T> expandedTrackedEntityHistoryRepository = null;

  @PostConstruct
  public void init() throws NoSuchMethodException {
    super.init();
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
