package com.kmarinos.springrapidrest.domain;

import com.kmarinos.springrapidrest.converter.HistoryToStateConverter;
import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistory;

public abstract class HistoryChangeListener<T extends TrackedEntity> {
    protected final HistoryToStateConverter<T> converter;

    public HistoryChangeListener(HistoryToStateConverter<T> converter) {
        this.converter = converter;
    }
    public void updateEntityCallback(TrackedEntityHistory<T> history) {
        switch ((history.getChangeType())){
            case CREATE -> converter.prePersistBeforeCreate(history);
            case UPDATE -> converter.prePersistBeforeUpdate(history);
            case DELETE -> converter.prePersistBeforeDelete(history);
            default -> converter.handleUnknown(history);
        }
    }

}
