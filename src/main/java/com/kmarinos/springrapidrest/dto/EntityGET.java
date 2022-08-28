package com.kmarinos.springrapidrest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    }
}
