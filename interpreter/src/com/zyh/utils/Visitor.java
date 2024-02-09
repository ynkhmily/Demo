package com.zyh.utils;

public interface Visitor<R> {
    R visitBinaryExpr(Expr.Binary expr);

    R visitGroupingExpr(Expr.Grouping expr);

    R visitLiteralExpr(Expr.Literal expr);

    R visitUnaryExpr(Expr.Unary expr);

    R visitVarExpr(Expr.Variable expr);

    void visitExpressionStmt(Stmt.Expression stmt);

    void visitPrintStmt(Stmt.Print stmt);

    void visitVarStmt(Stmt.Var stmt);

    R visitAssignExpr(Expr.Assign expr);

    void visitBlockStmt(Stmt.Block block);

    void visitIfStmt(Stmt.IF anIf);

    R visitLogicExpr(Expr.Logic logic);

    void visitWhileStmt(Stmt.WHILE aWhile);

    void visitLoopContral(Stmt.LOOPCONTRAL loopcontral);

    R visitCallExpr(Expr.Call expr);

    void visitFunctionStmt(Stmt.Function function);

    void visitReturnStmt(Stmt.Return aReturn);

    R visitAnonymousFunExpr(Expr.AnonymousFun anonymousFun);

    void visitClassStmt(Stmt.Class aClass);

    R visitGetExpr(Expr.Get get);

    R visitSetExpr(Expr.Set set);

    R visitThisExpr(Expr.This aThis);

    R visitSuperExpr(Expr.Super aSuper);
}
