package com.zyh.utils;

import com.zyh.JLox;

import java.util.List;
import java.util.Map;

// TODO 静态方法
public class JLoxClass implements JLoxCallable{
    final String name;

    public Map<String,JLoxFunction> methods = null;

    final JLoxClass superClass;

    public JLoxClass(String name, Map<String, JLoxFunction> methods, JLoxClass superClass) {
        this.name = name;
        this.methods = methods;
        this.superClass = superClass;
    }

    @Override
    public String toString() {
        return "<class " + name + ">";
    }

    @Override
    public int arity() {
        JLoxFunction init = findMethod("init");
        if (init == null) return 0;
        return init.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        JLoxInstance instance = new JLoxInstance(this);
        JLoxFunction init = findMethod("init");
        if (init != null) {
            init.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    public JLoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if(superClass != null)  return superClass.findMethod(name);
        return null;
    }
}
