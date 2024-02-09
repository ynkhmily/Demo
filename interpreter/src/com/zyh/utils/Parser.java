package com.zyh.utils;


import com.zyh.JLox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
    解析规则

    expression -> assignment;
    assignment -> (call ".")? IDENTIFIER "=" assignment | logic_or;
    logic_or -> logic_and ("or" logic_and)*;
    logic_and -> equality ("and" logic_and)*;
    equality -> comparison ( ( "!=" | "==")  comparison )*;
    comparison -> term ( ( ">" | ">=" | "<" | "<=") term )*;
    term -> factor ( ( "-" | "+") factor )*;
    factor -> unary ( ( "/" | "*") unary )*;
    unary -> ( "!" | "-") unary | call;
    call -> primary ( "(" arguments? ")" | "." IDENTIFIER )*;
    arguments -> AnonymousFun | expression ("," expression)* ;
    primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | "this" | "super" "." IDENTIFIER;


    funcDecl -> "fun" function
    function -> IDENTIFIER "(" parameters? ")" block;
    parameters -> IDENTIFIER ( "," IDENTIFIER )*;

    AnonymousFun -> "fun" "(" parameters? ")" block;

    statement -> exprStmt
               | forStmt
               | ifStmt
               | printStmt
               | returnStmt
               | whileStmt
               | block ;

    returnStmt     → "return" expression? ";" ;

    classDecl      → "class" IDENTIFIER ( "<" IDENTIFIER)* "{" function* "}" ;

 */
public class Parser {
    private final List<Token> tokenList;

    private int current = 0;

    public Parser(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    public List<Stmt> parse(){
        ArrayList<Stmt> stmts = new ArrayList<>();

        while(!isAtEnd()){
            stmts.add(declaration());
        }
        return stmts;
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.VAR)) return varDeclaration();
            if (match(TokenType.FUN)) return function("function");
            if (match(TokenType.CLASS)) return classDeclaration();

            return statement();
        } catch (ParserError e){
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect class name.");

        Expr.Variable superClass = null;
        if(match(TokenType.LESS)){
            Token token = consume(TokenType.IDENTIFIER, "Expect class name.");
            superClass = new Expr.Variable(token);
        }

        consume(TokenType.LEFT_BRACE,"Expect '{' before class body.");
        ArrayList<Stmt.Function> methods = new ArrayList<>();

        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()){
            methods.add(function("methods"));
        }

        consume(TokenType.RIGHT_BRACE,"Expect '}' after class body.");
        return new Stmt.Class(name,methods,superClass);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(TokenType.IDENTIFIER, "Expect " + kind + " name.");
        consume(TokenType.LEFT_PAREN,"Expect '(' after " + kind + " name.");
        ArrayList<Token> params = new ArrayList<>();

        if(!check(TokenType.RIGHT_PAREN)){
            do{
                if(params.size() > Constant.MAXIMUM_ARGUMENTS){
                    error(peek(), "Can't have more than 255 parameters.");
                }

                params.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while(match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN,"Expect ')' after parameters.");

        consume(TokenType.LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();

        return new Stmt.Function(name,params,body);
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER,"Expect variable name.");

        Expr init = null;
        if(match(TokenType.EQUAL)){
            init = expression();
        }

        consume(TokenType.SEMICOLON,"Expect ';' after value.");
        return new Stmt.Var(name,init);
    }

    // TODO 语法检查
    private void synchronize() {

    }

    private Stmt statement() {
        if(match(TokenType.PRINT))  return PrintStatement();
        if(match(TokenType.LEFT_BRACE)){
            return new Stmt.Block(block());
        }
        if(match(TokenType.IF)) return ifStatement();
        if(match(TokenType.WHILE))  return whileStatement();
        if(match(TokenType.FOR))    return forStatement();
        if(match(TokenType.RETURN)) return returnStatement();

        if(match(TokenType.BREAK) || match(TokenType.CONTINUE)) return loopContralStatement();

        return expressionStatement();
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;

        if(!check(TokenType.SEMICOLON)){
            value = expression();
        }

        consume(TokenType.SEMICOLON,"Expect ';' after return value.");
        return new Stmt.Return(keyword,value);
    }

    private Stmt loopContralStatement() {
        Token operator = previous();
        consume(TokenType.SEMICOLON,"Expect ';' after expression.");

        return new Stmt.LOOPCONTRAL(operator);
    }

    private Stmt forStatement() {
        consume(TokenType.LEFT_PAREN,"Expect '(' after 'for'.");
        Stmt varDeclara;

        if(match(TokenType.SEMICOLON)){
            varDeclara = null;
        } else if(match(TokenType.VAR)){
            varDeclara = varDeclaration();
        } else {
            varDeclara = expressionStatement();
        }

        Expr condition = null;
        if(!check(TokenType.SEMICOLON)){
            condition = expression();
        }
        consume(TokenType.SEMICOLON,"Expect ';' after loop condition");

        Expr operator = null;
        if(!check(TokenType.RIGHT_PAREN)){
            operator = expression();
        }
        consume(TokenType.RIGHT_PAREN,"Expect ')' after clauses.");
        Stmt body = statement();

        if(condition == null)  condition = new Expr.Literal(true);
        body = new Stmt.WHILE(condition,body,new Stmt.Expression(operator));

        if(varDeclara != null){
            body = new Stmt.Block(Arrays.asList(varDeclara,body));
        }

        return body;
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN,"Expect '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN,"Expect ')' after 'while'.");

        Stmt body = statement();

        return new Stmt.WHILE(condition,body,null);
    }

    private Stmt.IF ifStatement() {
        consume(TokenType.LEFT_PAREN,"Expect '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN,"Expect ')' after 'if'.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if(match(TokenType.ELSE)){
            elseBranch = statement();
        }

        return new Stmt.IF(condition,thenBranch,elseBranch);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE,"Expect '}' after block.");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON,"Expect ';' after value.");
        return new Stmt.Expression(expr);
    }

    private Stmt PrintStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON,"Expect ';' after value.");
        return new Stmt.Print(expr);
    }

    private Expr expression(){
        return assignment();
    }

    private Expr assignment() {
        Expr expr = logicOr();

        if(match(TokenType.EQUAL)){
            Token equal = previous();
            Expr value = assignment();
            if(expr instanceof Expr.Variable){
                return new Expr.Assign(((Expr.Variable) expr).name,value);
            } else if(expr instanceof Expr.Get){
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object,get.name,value);
            }
            error(equal,"Invalid assignment target.");
        }
        return expr;
    }

    private Expr logicOr() {
        Expr expr = logicAnd();

        while(match(TokenType.OR)){
            Token operator = previous();
            Expr right = logicAnd();
            expr = new Expr.Logic(expr,right,operator);
        }

        return expr;
    }

    private Expr logicAnd() {
        Expr expr = equality();

        while(match(TokenType.AND)){
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logic(expr,right,operator);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while(match(TokenType.BANG_EQUAL,TokenType.EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while(match(TokenType.GREATER,TokenType.GREATER_EQUAL,TokenType.LESS,TokenType.LESS_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr,operator,right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while(match(TokenType.MINUS,TokenType.PLUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr,operator,right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while(match(TokenType.SLASH,TokenType.STAR)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr,operator,right);
        }

        return expr;
    }

    private Expr unary() {

        if(match(TokenType.BANG,TokenType.MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(right,operator);
        }
        return call();
    }

    private Expr call() {
        Expr expr = primary();

        // 可能参数为函数
        while(true){
            if(match(TokenType.LEFT_PAREN)){
                expr = finishCall(expr);
            } else if(match(TokenType.DOT)){
                Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Get(expr,name);
            } else {
                break;
            }

        }
        return expr;
    }

    private Expr finishCall(Expr expr) {
        ArrayList<Expr> arguments = new ArrayList<>();

        if(!check(TokenType.RIGHT_PAREN)){
            do{
                if(arguments.size() >= Constant.MAXIMUM_ARGUMENTS){
                    error(peek(),"Can't have more than 255 arguments.");
                }
                Expr argument = null;
                if(check(TokenType.FUN)) {
                    advance();
                    argument = anonymousFun();
                } else {
                    argument = expression();
                }
                arguments.add(argument);
            } while(match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");
        return new Expr.Call(expr,paren,arguments);
    }

    private Expr anonymousFun() {
        consume(TokenType.LEFT_PAREN,"Expect '(' after function name.");
        ArrayList<Token> params = new ArrayList<>();

        if(!check(TokenType.RIGHT_PAREN)){
            do{
                if(params.size() > Constant.MAXIMUM_ARGUMENTS){
                    error(peek(), "Can't have more than 255 parameters.");
                }

                params.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while(match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN,"Expect ')' after parameters.");

        consume(TokenType.LEFT_BRACE, "Expect '{' before  function body.");
        List<Stmt> body = block();

        return new Expr.AnonymousFun(params,body);
    }

    private Expr primary() {
        if(match(TokenType.FALSE))  return new Expr.Literal(false);
        if(match(TokenType.TRUE))   return new Expr.Literal(true);
        if(match(TokenType.NIL))    return new Expr.Literal(null);


        if(match(TokenType.NUMBER,TokenType.STRING)){
            return new Expr.Literal(previous().literal);
        }

        if(match(TokenType.LEFT_PAREN)){
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN,"Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        if(match(TokenType.THIS)){
            return new Expr.This(previous());
        }

        if(match(TokenType.SUPER)){
            Token keyword = previous();
            consume(TokenType.DOT,"Expect '.' after 'super'.");
            Token method = consume(TokenType.IDENTIFIER, "Expect superclass method name.");
            return new Expr.Super(keyword, method);
        }

        if(match(TokenType.IDENTIFIER)){
            return new Expr.Variable(previous());
        }

        throw error(peek(),"Parser Error. Unexpected Token Type");
    }

    private Token consume(TokenType type, String message) {
        if(check(type)) return advance();

        throw error(peek(),message);
    }

    private ParserError error(Token token, String message) {
        JLox.error(token, message);
        throw new ParserError();
    }

    private boolean match(TokenType ... tokenTypes) {
        for (TokenType tokenType : tokenTypes) {
            if(check(tokenType)){
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType tokenType) {
        return peek().tokenType == tokenType;
    }

    private Token peek() {
        return tokenList.get(current);
    }

    private Token advance() {
        if(!isAtEnd())  current ++;
        return previous();
    }

    private Token previous() {
        return tokenList.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().tokenType == TokenType.EOF;
    }

    private static class ParserError extends RuntimeException{

    }
}
