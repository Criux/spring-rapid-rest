package com.kmarinos.springrapidrest.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityHistoryGET<T extends TrackedEntity> {
    String id;
    String datalineId;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    EntityGET<T> entity;
    String forEntityClass;
    String type;
    LocalDateTime createdAt;
    @JsonAnyGetter
            @JsonIgnore
    Map<String,Object> values= new HashMap<>();
    @JsonAnySetter
    public void setValue(String name,Object value){
        values.put(name,value==null?null:String.valueOf(value));
    }
}
