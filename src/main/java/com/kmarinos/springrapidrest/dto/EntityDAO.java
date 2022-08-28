package com.kmarinos.springrapidrest.dto;

import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

public abstract class EntityDAO<T> {

    public abstract EntityGET<T> GET(T entity);

    public abstract List<? extends EntityGET<T>> GET(List<T> entities);

    public abstract EntityGET<T> getAsReference(T entity);

    public abstract EntityGET<T> getOnlyId(T entity);

    @Component
    public static class SimpleEntityDAO<T extends TrackedEntity> extends EntityDAO<T> {

        @Override
        public EntityGET<T> GET(T entity) {
            return getOnlyId(entity);
        }

        @Override
        public List<EntityGET<T>> GET(List<T> entities) {
            if (entities == null) {
                return null;
            }
            return entities.stream().map(this::getOnlyId).collect(Collectors.toList());
        }

        @Override
        public EntityGET<T> getAsReference(T entity) {
            return getOnlyId(entity);
        }

        @Override
        public EntityGET<T> getOnlyId(T entity) {
            return EntityGET.SimpleEntityGET.<T>builder().id(entity.getId()).build();
        }



    }
}
