package com.kmarinos.springrapidrest.converter;

import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistory;
import com.kmarinos.springrapidrest.exceptionHandling.exceptions.OperationNotAllowedException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.CrudRepository;

@Slf4j
public abstract class HistoryToStateConverter<T extends TrackedEntity> {

    @Autowired
    protected ApplicationContext applicationContext;


    public abstract String[] getMandatoryFields();
    public abstract Function<TrackedEntityHistory<T>,TrackedEntityHistory<T>> handleCreate();
    public abstract Function<TrackedEntityHistory<T>,TrackedEntityHistory<T>> handleUpdate();
    public abstract Function<TrackedEntityHistory<T>,TrackedEntityHistory<T>> handleDelete();

    public void prePersistBeforeCreate(TrackedEntityHistory<T> history) {
        history=handleCreate().apply(history);
        saveEntity(history.getEntity(), history::setEntity);
    }

    public void prePersistBeforeUpdate(TrackedEntityHistory<T> history) {
        history=handleUpdate().apply(history);
        saveEntity(history.getEntity());
    }

    public void prePersistBeforeDelete(TrackedEntityHistory<T> history) {
        history=handleDelete().apply(history);
        T entity = history.getEntity();
        entity.setActive(false);
        saveEntity(entity);
    }

    public void handleUnknown(TrackedEntityHistory<T> history) {
        System.err.println("** Handle Unknown **");
    }

    public void validateMandatoryFields(TrackedEntityHistory<T> history){
        List.of(getMandatoryFields()).stream().forEach(field -> {
            validate(history, e -> {
                        try {
                            return PropertyUtils.getProperty(e, field) != null;
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                            //Field does not exist in history. Skipping...
                        }
                        return true;
                    },
                    (Function<T, String>) e -> "Field " + field + " cannot be empty in entity " + e.getClass().getSimpleName());
        });
    }

    void validate(TrackedEntityHistory<T> history, Predicate<T> condition) {
        validate(history, condition, (Function<T, String>) e -> "Entity did not pass validation");
    }

    void validate(TrackedEntityHistory<T> history, Predicate<T> c1, Predicate<T> c2) {
        validate(history, c1, (Function<T, String>) e -> "Entity did not pass validation");
        validate(history, c2, (Function<T, String>) e -> "Entity did not pass validation");
    }

    void validate(TrackedEntityHistory<T> history, Predicate<T> c1, Predicate<T> c2, Predicate<T> c3) {
        validate(history, c1, (Function<T, String>) e -> "Entity did not pass validation");
        validate(history, c2, (Function<T, String>) e -> "Entity did not pass validation");
        validate(history, c3, (Function<T, String>) e -> "Entity did not pass validation");
    }

    void validate(TrackedEntityHistory<T> history, Predicate<T> c1, Predicate<T> c2, Predicate<T> c3, Predicate<T> c4) {
        validate(history, c1, (Function<T, String>) e -> "Entity did not pass validation");
        validate(history, c2, (Function<T, String>) e -> "Entity did not pass validation");
        validate(history, c3, (Function<T, String>) e -> "Entity did not pass validation");
        validate(history, c4, (Function<T, String>) e -> "Entity did not pass validation");
    }

    void validate(TrackedEntityHistory<T> history, Predicate<T> c1, Predicate<T> c2, Predicate<T> c3, Predicate<T> c4, Predicate<T> c5) {
        validate(history, c1, (Function<T, String>) e -> "Entity did not pass validation");
        validate(history, c2, (Function<T, String>) e -> "Entity did not pass validation");
        validate(history, c3, (Function<T, String>) e -> "Entity did not pass validation");
        validate(history, c4, (Function<T, String>) e -> "Entity did not pass validation");
        validate(history, c5, (Function<T, String>) e -> "Entity did not pass validation");
    }

    public void validate(TrackedEntityHistory<T> history, Predicate<T> condition, Function<T, String> errorMessageProvider) {
        if (history.getEntity() == null || !condition.test(history.getEntity())) {
            throw new OperationNotAllowedException(errorMessageProvider.apply(history.getEntity()));
        }
    }

    void validate(TrackedEntityHistory<T> history, Predicate<T> c1, Function<T, String> e1, Predicate<T> c2, Function<T, String> e2) {
        validate(history, c1, e1);
        validate(history, c2, e2);
    }

    void validate(TrackedEntityHistory<T> history, Predicate<T> c1, Function<T, String> e1, Predicate<T> c2, Function<T, String> e2, Predicate<T> c3, Function<T, String> e3) {
        validate(history, c1, e1);
        validate(history, c2, e2);
        validate(history, c3, e3);
    }

    void validate(TrackedEntityHistory<T> history, Predicate<T> c1, Function<T, String> e1, Predicate<T> c2, Function<T, String> e2, Predicate<T> c3, Function<T, String> e3, Predicate<T> c4, Function<T, String> e4) {
        validate(history, c1, e1);
        validate(history, c2, e2);
        validate(history, c3, e3);
        validate(history, c4, e4);
    }

    void validate(TrackedEntityHistory<T> history, Predicate<T> c1, Function<T, String> e1, Predicate<T> c2, Function<T, String> e2, Predicate<T> c3, Function<T, String> e3, Predicate<T> c4, Function<T, String> e4, Predicate<T> c5, Function<T, String> e5) {
        validate(history, c1, e1);
        validate(history, c2, e2);
        validate(history, c3, e3);
        validate(history, c4, e4);
        validate(history, c5, e5);
    }

    public void setValueOnCondition(TrackedEntityHistory<T> history, String valueName, Predicate<TrackedEntityHistory<T>> condition) {
        if (condition.test(history)) {
            try {
                PropertyUtils.setProperty(history.getEntity(), valueName, history.getValue(valueName));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.warn("Cannot set {} for {}", valueName, history.getEntity().getClass().getSimpleName());
            }
        }
    }
    public void copyValueToHistory(TrackedEntityHistory<T> history,String valueName){
        try {
            var value=PropertyUtils.getProperty(history.getEntity(),valueName);
            PropertyUtils.setProperty(history, valueName, value);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.warn("Cannot set {} for {}", valueName, history.getEntity().getClass().getSimpleName());
        }
    }
    protected void saveEntity(T entity) {
        this.saveEntity(entity, null);
    }

    private void saveEntity(T entity, Consumer<T> callback) {
        applicationContext.getBeanProvider(CrudRepository.class).stream().findFirst()
                .map(c -> ((CrudRepository<T, String>) c).save(entity))
                .map(e -> {
                    if (callback != null) {
                        callback.accept(e);
                    }
                    return e;
                });
    }
}
