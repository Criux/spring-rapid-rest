package com.kmarinos.springrapidrest.domain.repository;

import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackedEntityHistoryRepository<T extends TrackedEntity> extends JpaRepository<TrackedEntityHistory<T>,String> {

    List<TrackedEntityHistory<T>> findAllByDatalineId(String datalineId);

}
