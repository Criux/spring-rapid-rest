package com.kmarinos.springrapidrest.domain.repository;

import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistory;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpandedTrackedEntityHistoryRepository<T extends TrackedEntity> {

    List<TrackedEntityHistory<T>> findAllByEntity(@Param("entity") TrackedEntity entity);
}
