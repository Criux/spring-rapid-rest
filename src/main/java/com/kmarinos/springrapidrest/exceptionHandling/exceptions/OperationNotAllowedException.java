package com.kmarinos.springrapidrest.exceptionHandling.exceptions;

public class OperationNotAllowedException extends RuntimeException{
    public OperationNotAllowedException(String message){
        super(message);
    }
}
