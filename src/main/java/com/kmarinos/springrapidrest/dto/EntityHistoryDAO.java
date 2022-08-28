package com.kmarinos.springrapidrest.dto;

import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistory;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistoryChangeType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class EntityHistoryDAO<T extends TrackedEntity> {

    @Autowired
    ApplicationContext applicationContext;

    private Map<Class<? extends TrackedEntity>,Class<? extends EntityDAO>> classMap= new HashMap<>();
    private final Class<? extends EntityDAO> defaultDAO;

    public EntityHistoryDAO(){
        defaultDAO=EntityDAO.SimpleEntityDAO.class;
    }

    public <T extends TrackedEntity>EntityHistoryGET<T> GET(TrackedEntityHistory<T> history){
        var entityDAOClass = classMap.get(history.getEntity().getClass());
        EntityDAO<T> entityDAO = applicationContext.getBean(entityDAOClass);
        return EntityHistoryGET.<T>builder()
                .id(history.getId())
                .entity(entityDAO.GET((T) history.getEntity()))
                .createdAt(history.getCreatedAt())
                .datalineId(history.getDatalineId())
                .values(history.getValuesMap())
                .type(history.getChangeType().name())
                .forEntityClass(history.getEntity().getClass().getName())
                .build();

    }
    public List<? extends EntityHistoryGET<T>> GET(List<TrackedEntityHistory<T>> historyList){
        return historyList.stream().map(this::getAsReference).collect(Collectors.toList());
    }
    public <T extends TrackedEntity>TrackedEntityHistory<T> POST(EntityHistoryChangeRequest<T> changeHistory){
        return prepareChange(changeHistory, TrackedEntityHistoryChangeType.CREATE).getHistory();
    }
    public <T extends TrackedEntity>TrackedEntityHistory<T> PUT(EntityHistoryChangeRequest<T> changeHistory) {
        return prepareChange(changeHistory,TrackedEntityHistoryChangeType.UPDATE).getHistory();
    }
    public <T extends TrackedEntity>TrackedEntityHistory<T> DELETE(EntityHistoryChangeRequest<T> changeHistory) {
        return prepareChange(changeHistory,TrackedEntityHistoryChangeType.DELETE).getHistory();
    }
    private <T extends TrackedEntity> EntityHistoryChangeRequest<T> prepareChange(EntityHistoryChangeRequest<T> changeHistory,TrackedEntityHistoryChangeType type){
        var history = changeHistory.getHistory();
        history.setChangeType(type);
        history.setDatalineId(changeHistory.getDatalineId());
        changeHistory.getValues().forEach(history::setValue);
        return changeHistory;
    }
    public EntityHistoryGET<T> getAsReference(TrackedEntityHistory<T> history){
        var entityDAOClass = classMap.getOrDefault(history.getEntity().getClass(),defaultDAO);
        EntityDAO<T> entityDAO = applicationContext.getBean(entityDAOClass);

        return EntityHistoryGET.<T>builder()
                .id(history.getId())
                .type(history.getChangeType().name())
                .entity(entityDAO.getOnlyId(history.getEntity()))
                .createdAt(history.getCreatedAt())
                .forEntityClass(history.getEntity().getClass().getName())
                .build();

    }


}
