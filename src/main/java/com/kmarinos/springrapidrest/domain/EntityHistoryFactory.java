package com.kmarinos.springrapidrest.domain;


import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class EntityHistoryFactory {
    public static final Map<Class<?>,String> classMap = new HashMap();
    public static Map<Class<?>,String>getMap(){
        return EntityHistoryFactory.classMap;
    }


    public static <T extends TrackedEntity> TrackedEntityHistory<T> forEntity(T entity) {
        TrackedEntityHistory<T> entityHistory = (TrackedEntityHistory<T>) EntityHistoryFactory.forEntity(entity.getClass());
        if (entityHistory != null) {
            entityHistory.setEntity(entity);
        }
        return entityHistory;
    }

    public static <T extends TrackedEntity> TrackedEntityHistory<T> forEntity(Class<T> entityClass) {
        String historyClassName = getMap().get(entityClass);
        if (historyClassName == null) {
            throw new RuntimeException("The entity " + entityClass + " does not have a corresponding history class");
        }
        Class<?> clazz = null;
        try {
            clazz = Class.forName(historyClassName);
            Constructor<?> ctor = clazz.getConstructor();
            TrackedEntityHistory<T> entityHistory = (TrackedEntityHistory<T>) ctor.newInstance();
            entityHistory.setEntity(entityClass.getConstructor().newInstance());
            return entityHistory;
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            return null;
        }
    }

}
