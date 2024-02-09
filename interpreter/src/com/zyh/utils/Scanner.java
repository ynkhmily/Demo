package com.zyh.utils;

import com.zyh.JLox;

import java.util.*;

import static com.zyh.utils.TokenType.*;

public class Scanner {

    private final String source;

    private final List<Token> tokenList = new ArrayList<>();

    private static final Map<String,TokenType> keywords = new HashMap<>();

    private int start = 0;

    private int current = 0;

    private int line = 1;

    static {
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
        // 新增
        keywords.put("break", BREAK);
        keywords.put("continue",CONTINUE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while(!isAtEnd()){
            start = current;
            scanToken();
        }

        tokenList.add(new Token(TokenType.EOF,"",null,line));
        return tokenList;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if(match('/')){
                    while(peek() != '\n' && !isAtEnd()) advance();
                } else if(match('*')) {
                    skipBlockComments();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"': scanString(); break;
            default:
                if(isDigit(c)){
                    scanNumber();
                } else if(isAlpha(c)){  // 最大匹配原则
                    identifier();
                }else {
                    JLox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void skipBlockComments() {
        while(!isAtEnd() && peek() != '*' && peekNext() != '/'){
            char c = advance();
            if(c == '\n')   line ++;
        }
        // 跳过 "*/"
        advance();
        advance();
    }

    private void identifier() {
        while(isAlpha(peek()) || isDigit(peek()))   advance();

        String text = source.substring(start,current);

        TokenType tokenType = keywords.get(text);
        if(Objects.isNull(tokenType))   tokenType = IDENTIFIER;

        addToken(tokenType);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private void scanNumber() {
        while(isDigit(peek()))  advance();

        if(peek() == '.' && isDigit(peekNext())){
            // skip '.'
            current ++;
            while(isDigit(peek()))  advance();
        }

        addToken(NUMBER,Double.parseDouble(source.substring(start,current)));
    }

    private char peekNext() {
        if(current + 1 >= source.length())  return '\0';

        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void scanString() {
        while(peek() != '"' && !isAtEnd()){
            advance();
        }

        if(isAtEnd()){
            JLox.error(line,"Unterminated string.");
            return;
        }

        current ++;
        String text = source.substring(start + 1,current - 1);
        addToken(STRING,text);
    }

    private char peek() {
        if(isAtEnd())   return '\0';

        return source.charAt(current);
    }

    private boolean match(char expected) {
        if(isAtEnd())   return false;
        if(source.charAt(current) != expected)  return false;

        current ++;
        return true;
    }

    private void addToken(TokenType tokenType) {
        addToken(tokenType,null);
    }

    private void addToken(TokenType tokenType, Object literal) {
        String text = source.substring(start,current);
        tokenList.add(new Token(tokenType,text,literal,line));
    }

    private char advance() {
        return source.charAt(current ++);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
