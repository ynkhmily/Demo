package com.zyh.utils;

import java.util.List;

public class JLoxFunction implements JLoxCallable{
    final Stmt.Function funDeclara;

    final Environment closure;

    final boolean isInit;

    public JLoxFunction(Stmt.Function funDeclara, Environment closure, boolean isInit) {
        this.funDeclara = funDeclara;
        this.closure = closure;
        this.isInit = isInit;
    }

    @Override
    public int arity() {
        return funDeclara.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);

        for(int i = 0;i < funDeclara.params.size();i ++){
            environment.define(funDeclara.params.get(i).lexeme,arguments.get(i));
        }

        try {
            interpreter.executeBlock(funDeclara.body, environment);
        } catch (Return v){
            if (isInit) return closure.getAt(0, "this");
            return v.value;
        }

        if (isInit) return closure.getAt(0, "this");
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + funDeclara.name.lexeme + '>';
    }

    public JLoxFunction bind(JLoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new JLoxFunction(funDeclara, environment,isInit);
    }
}
