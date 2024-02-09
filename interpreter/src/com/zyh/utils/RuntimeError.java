package com.zyh.utils;

public class RuntimeError extends RuntimeException{

    public final Token token;

    public RuntimeError(String message, Token token) {
        super(message);
        this.token = token;
    }
}
