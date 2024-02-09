package com.zyh.utils;

import com.zyh.JLox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Visitor{

    private final Interpreter interpreter;

    private final Stack<Map<String,Boolean>> scopes = new Stack<Map<String, Boolean>>();

    private FunctionType currentFunction = FunctionType.NONE;

    private ClassType currentClass = ClassType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private void endScope() {
        scopes.pop();
    }

    public void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    public void resolve(Expr expr) {
        expr.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<String,Boolean>());
    }

    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Object visitVarExpr(Expr.Variable expr) {
        if(!scopes.isEmpty() && scopes.peek().containsKey(expr.name.lexeme) && scopes.peek().get(expr.name.lexeme) == false){
            JLox.error(expr.name,"Can't read local variable in its own initializer.");
        }
        resolveLocal(expr, expr.name);
        return null;
    }

    private void resolveLocal(Expr expr, Token name) {
        for(int i = scopes.size() - 1;i >= 0;i --){
            if(scopes.get(i).containsKey(name.lexeme)){
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }

    }

    @Override
    public void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expr);
    }

    @Override
    public void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expr);
    }

    @Override
    public void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if(stmt.init != null){
            resolve(stmt.init);
        }
        define(stmt.name);
    }

    private void define(Token name) {
        if(scopes.isEmpty())  return;

        Map<String, Boolean> peek = scopes.peek();
        peek.put(name.lexeme,true);
    }

    private void declare(Token name) {
        if(scopes.isEmpty())  return;

        Map<String, Boolean> peek = scopes.peek();
        if(peek.containsKey(name.lexeme)){
            JLox.error(name,
                    "Already a variable with this name in this scope.");
        }
        peek.put(name.lexeme,false);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr,expr.name);
        return null;
    }

    @Override
    public void visitBlockStmt(Stmt.Block block) {
        beginScope();
        resolve(block.statements);
        endScope();
    }

    @Override
    public void visitIfStmt(Stmt.IF stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if(stmt.elseBranch != null) resolve(stmt.elseBranch);
    }

    @Override
    public Object visitLogicExpr(Expr.Logic logic) {
        resolve(logic.left);
        resolve(logic.right);
        return null;
    }

    @Override
    public void visitWhileStmt(Stmt.WHILE stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        if(stmt.increment != null)  resolve(stmt.increment);
    }

    @Override
    public void visitLoopContral(Stmt.LOOPCONTRAL loopcontral) {

    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);
        for (Expr argument : expr.arguments) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public void visitFunctionStmt(Stmt.Function function) {
        declare(function.name);
        define(function.name);

        resolveFunction(function,FunctionType.FUNCTION);
    }

    private void resolveFunction(Stmt.Function function,FunctionType functionType) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = functionType;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();

        currentFunction = enclosingFunction;
    }

    @Override
    public void visitReturnStmt(Stmt.Return stmt) {
        if(currentFunction == FunctionType.NONE){
            JLox.error(stmt.keyword, "Can't return from top-level code.");
        }

        if(stmt.value != null){
            if(currentFunction == FunctionType.INIT){
                JLox.error(stmt.keyword, "Can't return from init method.");
            }

            resolve(stmt.value);
        }
    }

    @Override
    public Object visitAnonymousFunExpr(Expr.AnonymousFun anonymousFun) {
        beginScope();
        for (Token argument : anonymousFun.arguments) {
            declare(argument);
            define(argument);
        }
        resolve(anonymousFun.body);
        endScope();
        return null;
    }

    @Override
    public void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);

        if(stmt.superClass != null ){
            if(stmt.superClass.name.lexeme.equals(stmt.name.lexeme)){
                JLox.error(stmt.superClass.name,
                        "A class can't inherit from itself.");
            }
            beginScope();
            scopes.peek().put("super",true);
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.superClass);
        }

        beginScope();
        scopes.peek().put("this",true);

        for (Stmt.Function method : stmt.methods) {
            FunctionType type = FunctionType.METHOD;
            if(method.name.lexeme.equals("init")){
                type = FunctionType.INIT;
            }
            resolveFunction(method,type);
        }

        endScope();
        if(stmt.superClass != null) endScope();
        currentClass = enclosingClass;
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
         resolve(expr.object);
         return null;
    }

    @Override
    public Object visitSetExpr(Expr.Set stmt) {
        resolve(stmt.object);
        resolve(stmt.value);
        return null;
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            JLox.error(expr.keyword,
                    "Can't use 'this' outside of a class.");
            return null;
        }

        resolveLocal(expr,expr.keyword);
        return null;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        if (currentClass == ClassType.NONE) {
            JLox.error(expr.keyword,
                    "Can't use 'super' outside of a class.");
        } else if (currentClass != ClassType.SUBCLASS) {
            JLox.error(expr.keyword,
                    "Can't use 'super' in a class with no superclass.");
        }
        resolveLocal(expr,expr.keyword);
        return null;
    }

    private enum FunctionType {
        NONE,
        FUNCTION,
        METHOD,
        INIT
    }

    private enum ClassType {
        NONE,
        CLASS,
        SUBCLASS
    }
}
