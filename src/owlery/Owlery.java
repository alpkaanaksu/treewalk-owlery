package owlery;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Owlery {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("You can run a file by \"owlery <file>\"");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPromt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPromt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print("→ owlery • ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
            hadRuntimeError = false;
        }
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError) return;

        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        hadError = true;
        report(line, "", message);
    }
    static void error(Token token, String message) {
        hadError = true;
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void warning(int line, String message) {
        System.out.println("\u001B[33m" + "warning!\n[line "+ line +"] " + message + "\u001B[0m");
    }

    static void runtimeError(RuntimeError e) {
        if (e.token != null) {
            System.err.println("\nruntime error\n[line " + e.token.line + "] " + e.getMessage());
        } else {
            System.err.println("\nruntime error\n" + e.getMessage());
        }
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.out.println("[line " + line + "] " + message);
    }
}
