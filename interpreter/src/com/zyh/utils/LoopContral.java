package com.zyh.utils;

public class LoopContral extends RuntimeException{
    final Token type;

    public LoopContral(Token type) {
        this.type = type;
    }

}
