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

    private Expr assignment() {
        Expr expr = or();

        if (match(TokenType.COLON)) {
            Token colon = previous();
            Expr value = assignment();

            if (expr instanceof  Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
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
                    return new Expr.Assign(name, booleanBinary ? new Expr.BooleanBinary(expr, op, operand) :new Expr.Binary(expr, op, operand));
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

        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NOTHING)) return new Expr.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "expected: <)> after grouping expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "expected: expression");
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
        return expressionStatement();
    }

    private Stmt emptyStatement() {
        return new Stmt.Empty(0);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.EOS, "expected: newline");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.EOS, "expected: newline");
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
        consume(TokenType.LEFT_BRACE, "expected: block after condition in if statement");
        Stmt ifBlock = blockStatement();
        Stmt elseBlock = null;
        if (match(TokenType.ELSE)) {
            if (match(TokenType.IF)) {
                elseBlock = ifStatement();
            } else if (match( TokenType.LEFT_BRACE)){
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
            consume(TokenType.LEFT_BRACE, "exptected: block after loop head");
            Stmt body = blockStatement();
            return incl ? new Stmt.LoopRangeIncl(val, to, body) : new Stmt.LoopRange(val, to, body);
        }

        consume(TokenType.LEFT_BRACE, "exptected: block after loop head");
        Stmt body = blockStatement();
        return new Stmt.LoopCondition(val, body);
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }

        return statements;
    }

}
