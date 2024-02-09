package com.zyh.utils;

public class Token {
    public final TokenType   tokenType;

    public final String lexeme;

    public final Object  literal;

    public final int line;

    public Token(TokenType tokenType, String lexeme, Object literal, int line) {
        this.tokenType = tokenType;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return "Token{" +
                "tokenType=" + tokenType +
                ", lexeme='" + lexeme + '\'' +
                ", literal=" + literal +
                ", line='" + line + '\'' +
                '}';
    }
}
