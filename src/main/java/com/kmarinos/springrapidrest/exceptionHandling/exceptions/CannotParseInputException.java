package com.kmarinos.springrapidrest.exceptionHandling.exceptions;

import org.springframework.util.StringUtils;

public class CannotParseInputException extends RuntimeException{

  public CannotParseInputException(Class<?> clazz,String input,String parsePattern){
    super(String.format("'%s' cannot be parsed to %s with parser pattern '%s'",
        input,StringUtils.capitalize(clazz.getSimpleName()),parsePattern));
  }
}
