package com.zyh.utils;

import java.util.List;

public abstract class Expr {

    abstract <R> R accept(Visitor<R> visitor);

    static class Binary extends Expr{
        final Expr left;

        final Token operator;

        final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    static class Grouping extends Expr{
        final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    static class Literal extends Expr{
        final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    static class Unary extends Expr{
        final Expr right;

        final Token operator;

        public Unary(Expr right, Token operator) {
            this.right = right;
            this.operator = operator;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    static class Variable extends Expr{
        final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarExpr(this);
        }
    }

    static class Assign extends Expr{
        final Token name;

        final Expr value;

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    static class Logic extends Expr{
        final Expr left;

        final Expr right;

        final Token operator;

        public Logic(Expr left, Expr right, Token operator) {
            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicExpr(this);
        }
    }

    static class Call extends Expr{
        final Expr callee;

        final Token paren;

        final List<Expr> arguments;

        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }

    static class AnonymousFun extends Expr{
        final List<Token>  arguments;

        final List<Stmt> body;

        public AnonymousFun(List<Token> arguments, List<Stmt> body) {
            this.arguments = arguments;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAnonymousFunExpr(this);
        }
    }

    static class Get extends  Expr{
        final Expr object;

        final Token name;

        public Get(Expr object, Token name) {
            this.object = object;
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    static class Set extends Expr{
        final Expr object;

        final Token name;

        final Expr value;

        public Set(Expr object, Token name, Expr value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    static class This extends Expr{
        final Token keyword;

        public This(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpr(this);
        }
    }

    static class Super extends Expr{
        final Token keyword;

        final Token method;

        public Super(Token keyword, Token method) {
            this.keyword = keyword;
            this.method = method;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSuperExpr(this);
        }
    }
}
