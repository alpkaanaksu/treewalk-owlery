package owlery;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr expression() {
        return assignment();
    }

    private Expr function() {
        List<Token> params = params();
        consume(TokenType.ARROW, "expected: '->' in function expression");
        consume(TokenType.LEFT_BRACE, "expected: method body (block) after '->'");
        List<Stmt> body = block();
        return new Expr.Function(params, body);
    }

    private Expr assignment() {
        Expr expr = or();


        if (match(TokenType.COLON)) {
            Token colon = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            if (expr instanceof Expr.TypeBinary conv && conv.op.type == TokenType.HASHTAG &&conv.expression instanceof Expr.Variable var) {
                Token name = var.name;
                return new Expr.Define(name, value, conv.type);

            }
            error(colon, "invalid assignment target");
        } else if (match(TokenType.COLON_COLON)) {
            // desugar <::> syntax
            Token colon = previous();
            if (match(TokenType.MINUS, TokenType.PLUS, TokenType.VERTICAL_BAR, TokenType.DOUBLE_VERTICAL_BAR,
                    TokenType.SLASH, TokenType.STAR, TokenType.PERCENT, TokenType.AND, TokenType.OR, TokenType.XOR)) {
                Token op = previous();
                Expr operand = expression();

                boolean booleanBinary = op.type == TokenType.AND || op.type == TokenType.OR || op.type == TokenType.XOR;

                if (expr instanceof Expr.Variable) {
                    Token name = ((Expr.Variable) expr).name;
                    return new Expr.Assign(name, booleanBinary ? new Expr.BooleanBinary(expr, op, operand) : new Expr.Binary(expr, op, operand));
                }
                error(colon, "invalid assignment target");
            }
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(TokenType.OR)) {
            Token op = previous();
            Expr r = and();
            expr = new Expr.BooleanBinary(expr, op, r);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(TokenType.AND)) {
            Token op = previous();
            Expr r = equality();
            expr = new Expr.BooleanBinary(expr, op, r);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL)) {
            Token op = previous();
            Expr r = comparison();
            expr = new Expr.Binary(expr, op, r);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while(match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token op = previous();
            Expr r = term();
            expr = new Expr.Binary(expr, op, r);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while(match(TokenType.MINUS, TokenType.PLUS, TokenType.VERTICAL_BAR, TokenType.DOUBLE_VERTICAL_BAR)) {
            Token op = previous();
            Expr r = factor();
            expr = new Expr.Binary(expr, op, r);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while(match(TokenType.SLASH, TokenType.STAR, TokenType.PERCENT)) {
            Token op = previous();
            Expr r = unary();
            expr = new Expr.Binary(expr, op, r);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            Token op = previous();
            Expr r = unary();
            return new Expr.Unary(op, r);
        }

        return call(); // primary();
    }



    private Expr call() {
        Expr expr = type();

        if (match(TokenType.BANG)) {
            Token bang = previous();
            List<Expr> args = arguments();
            return new Expr.Call(expr, bang, args);
        }

        return expr;
    }

    private OType typeFromToken(Token token) {
        return switch (token.type) {
            case T_INTEGER -> OType.Integer;
            case T_STRING -> OType.String;
            case T_DOUBLE -> OType.Double;
            case T_BOOLEAN -> OType.Boolean;
            case T_LIST -> OType.List;
            case T_CALLABLE -> OType.Callable;
            default -> OType.Flexible;
        };
    }

    private Expr type() {
        Expr expr = primary();

        if (match(TokenType.IS)) {
            Token is = previous();
            Token typeToken = consume("expected: type after 'is'", TokenType.T_INTEGER, TokenType.T_STRING, TokenType.T_DOUBLE, TokenType.T_BOOLEAN, TokenType.T_LIST, TokenType.T_CALLABLE);
            OType type = typeFromToken(typeToken);
            expr = new Expr.TypeBinary(expr, is, type);
        }

        if (match(TokenType.HASHTAG)) {
            Token hashtag = previous();
            Token typeToken = consume("expected: type after conversion operator '#'", TokenType.T_INTEGER, TokenType.T_STRING, TokenType.T_DOUBLE, TokenType.T_BOOLEAN, TokenType.T_LIST, TokenType.T_CALLABLE);
            OType type = typeFromToken(typeToken);
            expr = new Expr.TypeBinary(expr, hashtag, type);
        }

        return expr;
    }

    private boolean funcAhead() {
        int freeze = current;
        skip(TokenType.IDENTIFIER);
        if (match(TokenType.ARROW)) {
            current = freeze;
            return true;
        }
        current = freeze;
        return false;
    }

    private Expr primary() {
        if (funcAhead()) return function();

        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NOTHING)) return new Expr.Literal(null);

        if (match(TokenType.DOUBLE, TokenType.INTEGER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.IDENTIFIER)) {
            Token name = previous();
            if (match(TokenType.LEFT_BRACKET)) {
                Expr index = expression();
                consume(TokenType.RIGHT_BRACKET, "expected: ']' after index notation");
                return new Expr.Index(name, index);
            }
            return new Expr.Variable(name);
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "expected: <)> after grouping expression");
            return new Expr.Grouping(expr);
        }
        System.out.println(peek());
        throw error(peek(), "expected: expression");
    }

    private List<Expr> expressionList() {
        List<Expr> exprList = new ArrayList<>();

        if (!check(TokenType.EOS) && !check(TokenType.RIGHT_PAREN))
        do {
            exprList.add(expression());
        } while (match(TokenType.COMMA));
        return exprList;
    }

    private List<Expr> arguments() {
        List<Expr> arguments = new ArrayList<>();
        while (!check(TokenType.EOS) && !check(TokenType.RIGHT_PAREN) && !isAtEnd()) {
            arguments.add(expression());
        }
        return arguments;
    }

    private List<Token> params() {
        List<Token> params = new ArrayList<>();
        while (!check(TokenType.ARROW)) {
            params.add(advance());
        }
        return params;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if(check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {return false;}
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if(check(type)) return advance();
        throw error(peek(), message);
    }

    private Token consume(String message, TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) return advance();
        }
        throw error(peek(), message);
    }

    private boolean endStatement() {
        if (check(TokenType.EOS)) {
            advance();
            return true;
        } else if (check(TokenType.RIGHT_BRACE)) {
            return true;
        } else if (isAtEnd()) {
            return true;
        }

        error(previous(), "exptected: newline or <}> at the end of the statement");
        return false;
    }

    private void skip(TokenType type) {
        while (check(type)) advance();
    }


    private static class ParseError extends RuntimeException {}
    private ParseError error(Token token, String message) {
        Owlery.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == TokenType.EOS) return;

            switch (peek().type) {
                case CLASS, LOOP, IF, RETURN, PRINT -> { return; }
            }
        }

        advance();
    }

    private Stmt statement() {
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.EOS)) return emptyStatement();
        if (match(TokenType.LEFT_BRACE)) return blockStatement();
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.LOOP)) return loopStatement();
        if (match(TokenType.AT)) return returnStatement();
        return expressionStatement();
    }

    private Stmt emptyStatement() {
        return new Stmt.Empty(0);
    }

    private Stmt printStatement() {
        Expr value = expression();
        endStatement();
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        endStatement();
        return new Stmt.Expression(expr);
    }

    private Stmt blockStatement() {
        return new Stmt.Block(block());
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(statement());
        }

        consume(TokenType.RIGHT_BRACE, "exptected: <}> after block");
        return statements;
    }

    private Stmt ifStatement() {
        Expr cond = expression();
        skip(TokenType.EOS);
        consume(TokenType.LEFT_BRACE, "exptected: block after if condition");
        Stmt ifBlock = blockStatement();
        Stmt elseBlock = null;
        skip(TokenType.EOS);
        if (match(TokenType.ELSE)) {
            skip(TokenType.EOS);
            if (match(TokenType.IF)) {
                elseBlock = ifStatement();
            } else if (match(TokenType.LEFT_BRACE)){
                elseBlock = blockStatement();
            } else {
                throw error(peek(), "expected: if statement or block after <else>");
            }
        }
        return new Stmt.If(cond, ifBlock, elseBlock);
    }

    private Stmt loopStatement() {
        Expr val = expression();

        if (match(TokenType.TO)) {
            boolean incl = match(TokenType.INCL);
            Expr to = expression();
            skip(TokenType.EOS);
            consume(TokenType.LEFT_BRACE, "exptected: block after loop head");
            Stmt body = blockStatement();
            return new Stmt.LoopRange(val, to, body, incl);
        }

        skip(TokenType.EOS);
        consume(TokenType.LEFT_BRACE, "exptected: block after loop head");
        Stmt body = blockStatement();
        return new Stmt.LoopCondition(val, body);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(TokenType.EOS) && !check(TokenType.RIGHT_BRACE)) {
            value = expression();
        }
        endStatement();
        return new Stmt.Return(keyword, value);
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }
        return statements;
    }

}
