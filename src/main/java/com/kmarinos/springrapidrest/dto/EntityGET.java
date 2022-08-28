package com.kmarinos.springrapidrest.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class EntityGET<T> {
    public EntityGET() {
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder
    public static class SimpleEntityGET<T> extends EntityGET<T> {
        String id;
        @JsonAnyGetter
        @JsonInclude(Include.NON_EMPTY)
            @JsonIgnore
        Map<String,Object>values= new HashMap<>();
    }
}
