package com.kmarinos.springrapidrest.domain.model;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.springframework.data.annotation.CreatedDate;

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@Immutable
public abstract class TrackedEntityHistory<R extends TrackedEntity>{
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name
            = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    protected String id;
    protected String datalineId;
    @Transient
    protected R entity;
    @CreatedDate
    LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    TrackedEntityHistoryChangeType changeType;

    @Transient
    private Map<String, Object> internalEntityValues = new HashMap<>();

    public <U> U getValue(String name) {
        try {
            return (U)PropertyUtils.getProperty(this,name);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            //Field does not exist. Skipping...
        }
        return null;
    }

    public void setValue(String name, Object value) {
        internalEntityValues.put(name ,value.toString());
        try {
            PropertyUtils.setProperty(this,name,value);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            //Field does not exist. Skipping...
        }
    }

    public Map<String, Object> getValuesMap() {
        return internalEntityValues;
    }
}
