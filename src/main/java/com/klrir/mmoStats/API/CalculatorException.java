package com.klrir.mmoStats.API;

public class CalculatorException extends RuntimeException{
    public CalculatorException(){
        super();
    }
    public CalculatorException(String str){
        super(str);
    }
}