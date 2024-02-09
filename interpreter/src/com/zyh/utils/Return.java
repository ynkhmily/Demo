package com.zyh.utils;

public class Return extends RuntimeException{
    final Object value;

    public Return(Object value) {
        this.value = value;
    }
}
