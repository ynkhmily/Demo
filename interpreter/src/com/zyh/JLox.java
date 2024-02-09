package com.zyh;

import com.zyh.utils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;


public class JLox {

    private static boolean hadError = false;

    private static boolean hadRuntimeError = false;

    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if(args.length > 1){
            System.out.println("Usage: JLox [script]");
            System.exit(64);
        } else if(args.length == 1){
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(inputStreamReader);

        for(;;){
            System.out.print("> ");
            String line = reader.readLine();
            if(Objects.isNull(line))    break;
            run(line);

            // reset flag
            hadError = false;
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if(hadError) System.exit(65);
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokensList = scanner.scanTokens();

        Parser parser = new Parser(tokensList);
        List<Stmt> statements = parser.parse();

        if(hadError || hadRuntimeError){
            return;
        }
        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if(hadError){
            return;
        }

        interpreter.interpre(statements);
//        System.out.println(new AstPrinter().print(expr));
    }

    public static void error(int line,String message){
        report(line, "", message);
    }

    public static void error(Token token,String message){
        if(token.tokenType == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
