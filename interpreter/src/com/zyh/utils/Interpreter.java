package com.zyh.utils;

import com.zyh.JLox;

import java.util.*;


public class Interpreter implements Visitor<Object>{

    final Environment globals = new Environment();

    public Environment environment = globals;

    private final Map<Expr, Integer> locals = new HashMap<>();

    public Interpreter(){
        globals.define("clock", new JLoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    public void interpre(List<Stmt> statements){
        try{
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError e){
             JLox.runtimeError(e);
        }
    }

    private void execute(Stmt statement) {
        statement.accept(this);
    }

    private String stringify(Object value) {
        if(Objects.isNull(value))   return "nil";

        if(value instanceof Double){
            String text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return value.toString();
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        TokenType tokenType = expr.operator.tokenType;

        if(tokenType == TokenType.PLUS){
            if(left instanceof String && right instanceof String){
                return (String)left + (String) right;
            } else if(left instanceof Double && right instanceof Double) {
                checkNumberOperator(expr.operator,left,right);
                return (double)left + (double)right;
            }
        } else if(tokenType == TokenType.MINUS){
            checkNumberOperator(expr.operator,left,right);
            return (double)left - (double)right;
        } else if(tokenType == TokenType.STAR){
            checkNumberOperator(expr.operator,left,right);
            return (double)left * (double)right;
        } else if(tokenType == TokenType.SLASH){
            checkNumberOperator(expr.operator,left,right);
            if(((double)right) == 0)    throw new RuntimeError("divide zero error",expr.operator);
            return (double)left / (double)right;
        } else if(tokenType == TokenType.GREATER){
            checkCompareOperator(expr.operator,left,right);
            return ((Comparable)left).compareTo((Comparable)right) > 0;
        } else if(tokenType == TokenType.GREATER_EQUAL){
            checkCompareOperator(expr.operator,left,right);
            return ((Comparable)left).compareTo((Comparable)right) >= 0;
        } else if(tokenType == TokenType.LESS){
            checkCompareOperator(expr.operator,left,right);
            return ((Comparable)left).compareTo((Comparable)right) < 0;
        } else if(tokenType == TokenType.LESS_EQUAL){
            checkCompareOperator(expr.operator,left,right);
            return ((Comparable)left).compareTo((Comparable)right) <= 0;
        } else if(tokenType == TokenType.BANG_EQUAL){
            return !isEqual(left,right);
        } else if(tokenType == TokenType.EQUAL_EQUAL){
            return isEqual(left,right);
        }

        return null;
    }

    private void checkCompareOperator(Token operator,Object left, Object right) {
        if(left instanceof Comparable<?> && right instanceof Comparable<?>) return;
        throw new RuntimeError("Uncomparable element",operator);
    }


    private void checkNumberOperator(Token operator, Object left, Object right) {
        if(left instanceof Double && right instanceof Double)   return;
        throw new RuntimeError("Operator must be number",operator);
    }

    private boolean isEqual(Object left, Object right) {
        if(left == null && right == null)   return true;
        if(left == null || right == null)   return false;

        return left.equals(right);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        TokenType tokenType = expr.operator.tokenType;

        if(tokenType == TokenType.MINUS){
            checkNumberOperator(expr.operator,right);
            return -(double)right;
        } else if(tokenType == TokenType.BANG){
            return !isTruth(right);
        }

        return null;
    }

    @Override
    public Object visitVarExpr(Expr.Variable expr) {
        return lookUpVar(expr.name, expr);
    }

    private Object lookUpVar(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }


    @Override
    public void visitExpressionStmt(Stmt.Expression stmt) {
        Object value = evaluate(stmt.expr);
    }

    @Override
    public void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expr);
        System.out.println(stringify(value));
    }

    @Override
    public void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if(stmt.init != null){
            value = evaluate(stmt.init);
        }

        environment.define(stmt.name.lexeme,value);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }

        return value;
    }

    @Override
    public void visitBlockStmt(Stmt.Block block) {
        executeBlock(block.statements, new Environment(environment));
    }

    @Override
    public void visitIfStmt(Stmt.IF ifStmt) {
        Object condition = evaluate(ifStmt.condition);
        if(isTruth(condition)){
            execute(ifStmt.thenBranch);
        } else if(ifStmt.elseBranch != null){
            execute(ifStmt.elseBranch);
        }
    }

    @Override
    public Object visitLogicExpr(Expr.Logic logic) {
        Object left = evaluate(logic.left);

        if(logic.operator.tokenType == TokenType.OR){
            if(isTruth(left))   return left;
        } else {
            if(!isTruth(left))  return left;
        }
        return evaluate(logic.right);
    }

    @Override
    public void visitWhileStmt(Stmt.WHILE statement) {
        Object condition = evaluate(statement.condition);

        while(isTruth(condition)){
            try {
                execute(statement.body);
                if(statement.increment != null){
                    execute(statement.increment);
                }
            } catch(LoopContral e){
                if(e.type.tokenType == TokenType.BREAK) break;
                else if(e.type.tokenType == TokenType.CONTINUE){
                    if(statement.increment != null){
                        execute(statement.increment);
                    }
                }
            }
            condition = evaluate(statement.condition);
        }
    }

    @Override
    public void visitLoopContral(Stmt.LOOPCONTRAL stmt) {
        throw new LoopContral(stmt.type);
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof JLoxCallable)) {
            throw new RuntimeError("Can only call functions and classes.", expr.paren);
        }

        JLoxCallable function = (JLoxCallable) callee;

        if(arguments.size() != function.arity()){
            throw new RuntimeError("Expected " + function.arity() + " arguments but got " +
                    arguments.size() + ".",expr.paren);
        }
        return function.call(this, arguments);
    }

    @Override
    public void visitFunctionStmt(Stmt.Function function) {
        JLoxFunction fun = new JLoxFunction(function, environment,false);
        environment.define(function.name.lexeme, fun);
    }

    @Override
    public void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if(stmt.value != null)  value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Object visitAnonymousFunExpr(Expr.AnonymousFun anonymousFun) {
        Stmt.Function function = new Stmt.Function(null, anonymousFun.arguments, anonymousFun.body);
        JLoxFunction fun = new JLoxFunction(function, environment,false);
        return fun;
    }

    @Override
    public void visitClassStmt(Stmt.Class stmt) {
        Object superClass = null;
        if(stmt.superClass != null){
            superClass = evaluate(stmt.superClass);
            if(!(superClass  instanceof JLoxClass)){
                throw new RuntimeError("Superclass must be a class.",stmt.superClass.name);
            }
        }


        environment.define(stmt.name.lexeme, null);
        if(superClass != null){
            environment = new Environment(environment);
            environment.define("super",superClass);
        }

        Map<String, JLoxFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            JLoxFunction jLoxFunction = new JLoxFunction(method, environment,method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme,jLoxFunction);
        }

        JLoxClass klass = new JLoxClass(stmt.name.lexeme, methods, (JLoxClass) superClass);
        if(superClass != null){
            environment = environment.enclosing;
        }

        environment.assign(stmt.name, klass);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if(object instanceof JLoxInstance){
            return ((JLoxInstance) object).get(expr.name);
        }
        throw new RuntimeError("Only instances have properties.",expr.name);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);
        if(object instanceof JLoxInstance){
            Object value = evaluate(expr.value);
            JLoxInstance jLoxInstance = (JLoxInstance) object;
            jLoxInstance.set(expr.name,value);
            return value;
        }
        throw new RuntimeError("Only instances have fields.",expr.name);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVar(expr.keyword,expr);
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        JLoxClass superclass = (JLoxClass)environment.getAt(
                distance, "super");

        JLoxInstance object = (JLoxInstance)environment.getAt(distance - 1, "this");
        JLoxFunction method = superclass.findMethod(expr.method.lexeme);

        if (method == null) {
            throw new RuntimeError("Undefined property '" + expr.method.lexeme + "'.",expr.method);
        }
        return method.bind(object);
    }


    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;

        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private void checkNumberOperator(Token operator, Object right) {
        if(right instanceof Double) return;
        throw new RuntimeError("Operator must be number",operator);
    }

    private boolean isTruth(Object right) {
        if(Objects.isNull(right))   return false;
        if(right instanceof Boolean)    return (Boolean)right;
        return true;
    }

    public void resolve(Expr expr, int depth) {
        locals.put(expr,depth);
    }
}
