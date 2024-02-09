package com.zyh.utils;

import javax.swing.text.html.parser.AttributeList;
import java.util.HashMap;

public class Environment {

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public final Environment enclosing;

    private final HashMap<String,Object> map = new HashMap<>();

    void define(String name,Object value){
        map.put(name,value);
    }

    Object get(Token token){
        if(map.containsKey(token.lexeme)){
            return map.get(token.lexeme);
        }
        if(enclosing != null)   return enclosing.get(token);

        throw new RuntimeError("Undefined variable '" + token.lexeme + "'.", token);
    }

    public void assign(Token name, Object value) {
        if(map.containsKey(name.lexeme)){
            map.put(name.lexeme,value);
            return;
        } else if(enclosing != null){
            enclosing.assign(name,value);
            return;
        }

        throw new RuntimeError("Undefined variable '" + name.lexeme + "'.",name);
    }

    public Object getAt(Integer distance, String name) {
        return ancestor(distance).map.get(name);
    }

    private Environment ancestor(Integer distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    public void assignAt(Integer distance, Token name, Object value) {
        ancestor(distance).map.put(name.lexeme,value);
    }
}
