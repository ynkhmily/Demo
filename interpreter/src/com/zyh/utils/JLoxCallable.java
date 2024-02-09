package com.zyh.utils;

import java.util.List;

public interface JLoxCallable {

    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
