package com.zyh.utils;

import java.util.HashMap;
import java.util.Map;

public class JLoxInstance {
    final JLoxClass jLoxClass;

    final Map<String, Object> fields = new HashMap<>();

    public JLoxInstance(JLoxClass jLoxClass) {
        this.jLoxClass = jLoxClass;
    }

    @Override
    public String toString() {
        return jLoxClass + " instance";
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        JLoxFunction method = jLoxClass.findMethod(name.lexeme);
        if (method != null) return method.bind(this);

        throw new RuntimeError("Undefined property '" + name.lexeme + "'.",name);
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme,value);
    }
}
