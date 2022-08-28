package com.kmarinos.springrapidrest.converter;

import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistory;
import java.lang.reflect.Field;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StandardHistoryToStateConverter<T extends TrackedEntity> extends HistoryToStateConverter<T> {

    @Override
    public String[] getMandatoryFields() {
        return new String[0];
    }

    @Override
    public Function<TrackedEntityHistory<T>,TrackedEntityHistory<T>> handleCreate() {
        return history->{
            setAllValuesNotNull(history);
            validateMandatoryFields(history);
            return history;
        };
    }

    @Override
    public Function<TrackedEntityHistory<T>,TrackedEntityHistory<T>> handleUpdate() {
        return history->{
            validateUpdate(history);
            setAllValuesNotNull(history);
            return history;
        };
    }

    @Override
    public Function<TrackedEntityHistory<T>,TrackedEntityHistory<T>> handleDelete() {
        return history->{
            validateDelete(history);
            copyAllValuesToHistory(history);
            return history;
        };
    }

    void validateUpdate(TrackedEntityHistory<T> history) {
        validate(history,
            entity -> entity != null && entity.getId() != null,
                (Function<T, String>) e -> "Cannot update unknown entity");
    }
    void validateDelete(TrackedEntityHistory<T> history) {
        validate(history,
            entity -> entity != null && entity.getId() != null,
                (Function<T, String>) e -> "Cannot delete unknown entity");
        validate(history,
            TrackedEntity::isActive, (Function<T, String>) e -> "Entity " + e.getClass() + "(" + e.getId() + ") is already deleted");
    }
    protected void setValueOnNotNull(TrackedEntityHistory<T> history, String valueName) {
        setValueOnCondition(history, valueName, h -> h.getValue(valueName) != null);
    }

    protected void setAllValuesNotNull(TrackedEntityHistory<T> history) {
        for (Field field : history.getEntity().getClass().getDeclaredFields()) {
            setValueOnNotNull(history, field.getName());
        }
    }
    protected void copyAllValuesToHistory(TrackedEntityHistory<T> history) {
        for (Field field : history.getEntity().getClass().getDeclaredFields()) {
            copyValueToHistory(history, field.getName());
        }
    }


}
