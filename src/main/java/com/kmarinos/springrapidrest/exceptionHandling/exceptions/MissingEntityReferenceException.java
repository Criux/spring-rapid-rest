package com.kmarinos.springrapidrest.exceptionHandling.exceptions;

import org.springframework.util.StringUtils;

public class MissingEntityReferenceException extends RuntimeException {

    public MissingEntityReferenceException(Class clazz, Class forEntity,String searchParam) {
        super(MissingEntityReferenceException.generateMessage(clazz.getSimpleName(), forEntity.getSimpleName(), searchParam));
    }

    private static String generateMessage(String entity, String forEntity,String searchParams) {
        return "The "+StringUtils.capitalize(forEntity) +
                "'s reference ("+searchParams+") must be set in Entity " +
                StringUtils.capitalize(entity);
    }

}