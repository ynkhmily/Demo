package com.zyh.utils;

import java.util.List;

public abstract class Stmt {

    abstract void accept(Visitor visitor);

    static class Expression extends Stmt{
        final Expr expr;

        public Expression(Expr expr) {
            this.expr = expr;
        }

        @Override
        void accept(Visitor visitor) {
            visitor.visitExpressionStmt(this);
        }
    }

    static class Print extends Stmt{
        final Expr expr;

        public Print(Expr expr) {
            this.expr = expr;
        }

        @Override
        void accept(Visitor visitor) {
            visitor.visitPrintStmt(this);
        }
    }

    static class Var extends Stmt{
        final Token name;

        final Expr init;

        public Var(Token name, Expr init) {
            this.name = name;
            this.init = init;
        }

        @Override
        void accept(Visitor visitor) {
            visitor.visitVarStmt(this);
        }
    }

    static class Block extends Stmt{
        final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        void accept(Visitor visitor) {
            visitor.visitBlockStmt(this);
        }
    }

    static class IF extends Stmt{
        final Expr condition;

        final Stmt thenBranch;

        final Stmt elseBranch;

        public IF(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        void accept(Visitor visitor) {
            visitor.visitIfStmt(this);
        }
    }

    static class WHILE extends Stmt{
        final Expr condition;

        final Stmt body;

        final Stmt increment;

        public WHILE(Expr condition, Stmt body, Stmt increment) {
            this.condition = condition;
            this.body = body;
            this.increment = increment;
        }

        @Override
        void accept(Visitor visitor) {
            visitor.visitWhileStmt(this);
        }
    }

    static class LOOPCONTRAL extends Stmt{
        final Token type;

        public LOOPCONTRAL(Token type) {
            this.type = type;
        }

        @Override
        void accept(Visitor visitor) {
            visitor.visitLoopContral(this);
        }
    }

    static class Function extends Stmt{
        final Token name;

        final List<Token>   params;

        final List<Stmt>    body;

        public Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        void accept(Visitor visitor) {
            visitor.visitFunctionStmt(this);
        }
    }

    static class Return extends Stmt{
        final Token keyword;

        final Expr value;

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        void accept(Visitor visitor) {
            visitor.visitReturnStmt(this);
        }
    }

    static class Class extends Stmt{
        final Token name;

        final List<Function>    methods;

        final Expr.Variable superClass;

        public Class(Token name, List<Function> methods, Expr.Variable superClass) {
            this.name = name;
            this.methods = methods;
            this.superClass = superClass;
        }

        @Override
        void accept(Visitor visitor) {
            visitor.visitClassStmt(this);
        }
    }
}
