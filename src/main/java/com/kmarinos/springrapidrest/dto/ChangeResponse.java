package com.kmarinos.springrapidrest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ChangeResponse<T> {

    int status;
    T entity;
    String error;
    String message;

    public static <T> ChangeResponse<T> OK(T entity){
        return ChangeResponse.<T>builder()
                .status(200)
                .entity(entity)
                .build();
    }
}
