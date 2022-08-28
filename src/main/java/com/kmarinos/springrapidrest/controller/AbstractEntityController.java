package com.kmarinos.springrapidrest.controller;

import com.kmarinos.springrapidrest.config.EntityRootPath;
import com.kmarinos.springrapidrest.domain.EntityHistoryFactory;
import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistory;
import com.kmarinos.springrapidrest.domain.repository.TrackedEntityHistoryRepository;
import com.kmarinos.springrapidrest.dto.ChangeResponse;
import com.kmarinos.springrapidrest.dto.EntityDAO;
import com.kmarinos.springrapidrest.dto.EntityGET;
import com.kmarinos.springrapidrest.dto.EntityHistoryChangeRequest;
import com.kmarinos.springrapidrest.dto.EntityHistoryDAO;
import com.kmarinos.springrapidrest.dto.EntityHistoryGET;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

@Slf4j
public abstract class AbstractEntityController<T extends TrackedEntity> {
    protected final TrackedEntityHistoryRepository<T> trackedEntityHistoryRepository;
    protected final EntityDAO<T> entityDAO;
    protected final EntityHistoryDAO<T> entityHistoryDAO;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private RequestMappingInfo.BuilderConfiguration requestMappingOptions;
    protected Class<T> entityClass;

    protected AbstractEntityController(TrackedEntityHistoryRepository<T> trackedEntityHistoryRepository,
                                       EntityDAO<T> entityDAO, EntityHistoryDAO<T> entityHistoryDAO,
                                       RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.trackedEntityHistoryRepository = trackedEntityHistoryRepository;
        this.entityDAO = entityDAO;
        this.entityHistoryDAO = entityHistoryDAO;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        try{
            entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];

        }catch (ClassCastException ex){
            entityClass= (Class<T>) TrackedEntity.class;
        }

    }

    @PostConstruct
    public void init() throws NoSuchMethodException {
        requestMappingOptions = new RequestMappingInfo.BuilderConfiguration();
        requestMappingOptions.setPatternParser(new PathPatternParser());
        var rootPathService = this.findEntityPath("");

        registerMapping(rootPathService + "s", RequestMethod.GET, this.getClass().getMethod("getAllEntitiesWithOptionalSearch", String.class));
        registerMapping(rootPathService + "/{id}", RequestMethod.GET, this.getClass().getMethod("getEntityWithId", String.class));
        registerMapping(rootPathService + "/{id}/history", RequestMethod.GET, this.getClass().getMethod("getSingleEntityHistory", String.class));
        registerMapping(rootPathService + "", RequestMethod.POST, this.getClass().getMethod("createEntity", EntityHistoryChangeRequest.class));
        registerMapping(rootPathService + "/{id}", RequestMethod.PUT, this.getClass().getMethod("updateEntity", EntityHistoryChangeRequest.class, String.class));
        registerMapping(rootPathService + "/{id}", RequestMethod.DELETE, this.getClass().getMethod("deleteEntity", String.class));
    }

    public abstract Function<String, List<T>> handleFetchAllEntities();

    public abstract Function<String, T> handleFetchEntityWithId();

    public abstract Function<T,List<TrackedEntityHistory<T>>> handleFetchHistoryForEntity();

    public List<? extends EntityGET<T>> getAllEntitiesWithOptionalSearch(@RequestParam(name = "q", required = false) String searchTerm) {
        return entityDAO.GET(handleFetchAllEntities().apply(searchTerm));
    }
    public EntityGET<T> getEntityWithId(@PathVariable("id") String id){
        return entityDAO.GET(handleFetchEntityWithId().apply(id));
    }
    public List<? extends EntityHistoryGET<T>> getSingleEntityHistory(@PathVariable("id") String id) {
        var entity = handleFetchEntityWithId().apply(id);
        return entityHistoryDAO.GET(handleFetchHistoryForEntity().apply(entity));
    }

    public EntityGET<T> createEntity(@RequestBody EntityHistoryChangeRequest<T> changeHistory) {
        return processChangeRequest(changeHistory, entityHistoryDAO::POST);
    }

    public EntityGET<T> updateEntity(@RequestBody EntityHistoryChangeRequest<T> changeHistory, @PathVariable("id") String id) {
        var entity = handleFetchEntityWithId().apply(id);
        return processChangeRequest(changeHistory, entity, entityHistoryDAO::PUT);
    }

    public EntityGET<T> deleteEntity(@PathVariable("id") String id) {
        var entity = handleFetchEntityWithId().apply(id);
        return processChangeRequest(EntityHistoryChangeRequest.<T>builder()
                .build(), entity, entityHistoryDAO::DELETE);
    }

    protected EntityGET<T> processChangeRequest(EntityHistoryChangeRequest<T> changeHistory,
                                                Function<EntityHistoryChangeRequest<T>, TrackedEntityHistory<T>> converter) {
        return this.processChangeRequest(changeHistory, null, converter);
    }

    protected EntityGET<T> processChangeRequest(EntityHistoryChangeRequest<T> changeHistory, T forEntity,
                                                Function<EntityHistoryChangeRequest<T>, TrackedEntityHistory<T>> converter) {
        if (forEntity == null) {
            changeHistory.setHistory(EntityHistoryFactory.forEntity(entityClass));
        } else {
            changeHistory.setHistory(EntityHistoryFactory.forEntity(forEntity));
        }
        var history = converter.apply(changeHistory);
        var entity = trackedEntityHistoryRepository.save(history).getEntity();
        return entityDAO.GET(entity);
    }

    private void registerMapping(String path, RequestMethod httpMethod, Method method) {
        RequestMappingInfo mappingInfo = RequestMappingInfo.paths(path).methods(httpMethod).options(requestMappingOptions).build();
        requestMappingHandlerMapping.registerMapping(mappingInfo, this, method);
    }

    private String findEntityPath(String controllerType) {
        String entityRootPath = "";
        var requestMappingAnnotation = this.getClass().getAnnotation(RequestMapping.class);
        if (requestMappingAnnotation != null) {
            entityRootPath += requestMappingAnnotation.value()[0];
            if (!entityRootPath.endsWith("/")) {
                entityRootPath += "/";
            }
        }
        if(controllerType!=null&&!controllerType.isEmpty()){
            entityRootPath+=controllerType+"/";
        }
        var pathFromEntity = this.getClass().getAnnotation(EntityRootPath.class);
        if (pathFromEntity != null) {
            entityRootPath += pathFromEntity.value();
        } else {
            Class<T> entityClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                    .getActualTypeArguments()[0];
            entityRootPath += entityClass.getSimpleName().toLowerCase(Locale.ROOT);
        }
        return entityRootPath;
    }

}
