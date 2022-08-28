package com.kmarinos.springrapidrest.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistory;
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
public class EntityHistoryChangeRequest<T extends TrackedEntity> {
    @JsonIgnore
    TrackedEntityHistory<T> history;
    String datalineId;
    @JsonIgnore
    String forEntityClass;
    @JsonIgnore
    String type;
    LocalDateTime createdAt;
    @JsonAnyGetter
    @JsonIgnore
            @Builder.Default
    Map<String,String> values= new HashMap<>();
    @JsonAnySetter
    public void setValue(String name,Object value){
        values.put(name,value==null?null:String.valueOf(value));
    }

}
