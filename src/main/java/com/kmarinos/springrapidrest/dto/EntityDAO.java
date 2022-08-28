package com.kmarinos.springrapidrest.dto;

import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;
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
            Map<String,Object> values = new HashMap<>();
            for (Field declaredField : entity.getClass().getDeclaredFields()) {
                try {
                    var value = PropertyUtils.getProperty(entity,declaredField.getName());
                    if(value!=null){
                        values.put(declaredField.getName(),value);
                    }
                } catch (IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
            return EntityGET.SimpleEntityGET.<T>builder().id(entity.getId()).values(values).build();
        }

        @Override
        public List<EntityGET<T>> GET(List<T> entities) {
            if (entities == null) {
                return null;
            }
            return entities.stream().map(this::GET).collect(Collectors.toList());
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
