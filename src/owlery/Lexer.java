package owlery;

import java.util.*;

public class Lexer {
    private final String source;
    private final List<Token> tokens;

    private int start, current, line;

    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("or", TokenType.OR);
        keywords.put("xor", TokenType.XOR);
        keywords.put("not", TokenType.NOT);
        keywords.put("AND", TokenType.BIT_AND);
        keywords.put("OR", TokenType.BIT_OR);
        keywords.put("XOR", TokenType.BIT_XOR);
        keywords.put("NOT", TokenType.BIT_NOT);
        keywords.put("class", TokenType.CLASS);
        keywords.put("AND", TokenType.BIT_AND);
        keywords.put("loop", TokenType.LOOP);
        keywords.put("to", TokenType.TO);
        keywords.put("in", TokenType.IN);
        keywords.put("return", TokenType.RETURN);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("nothing", TokenType.NOTHING);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("hoot", TokenType.PRINT);
        keywords.put("incl", TokenType.INCL);
        keywords.put("integer", TokenType.T_INTEGER);
        keywords.put("double", TokenType.T_DOUBLE);
        keywords.put("string", TokenType.T_STRING);
        keywords.put("boolean", TokenType.T_BOOLEAN);
        keywords.put("list", TokenType.T_LIST);
        keywords.put("callable", TokenType.T_CALLABLE);





    }

    Lexer(String source) {
        this.source = source;
        tokens = new ArrayList<>();

        start = 0;
        current = 0;
        line = 1;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case '[' -> addToken(TokenType.LEFT_BRACKET);
            case ']' -> addToken(TokenType.RIGHT_BRACKET);
            case ',' -> addToken(TokenType.COMMA);
            case '+' -> addToken(TokenType.PLUS);
            case '/' -> addToken(TokenType.SLASH);
            case '*' -> addToken(TokenType.STAR);
            case '%' -> addToken(TokenType.PERCENT);
            case '=' -> addToken(TokenType.EQUAL);
            case '#' -> addToken(TokenType.HASHTAG);

            case '\n' -> {
                addToken(TokenType.EOS);
                line++;
            }

            case '|' -> addToken(match('|') ? TokenType.DOUBLE_VERTICAL_BAR : TokenType.VERTICAL_BAR);
            case '@' -> addToken(match('@') ? TokenType.AT_AT : TokenType.AT);
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case ':' -> addToken(match(':') ? TokenType.COLON_COLON : TokenType.COLON);

            case '.' -> {
                if (match('.')) {
                    if (!match('\n')) {
                        Owlery.warning(line, "unnecessary use of <..>\nexpected: newline character after <..>");
                    }
                } else {
                    addToken(TokenType.DOT);
                }
            }

            case '!' -> {
                if (match('=')) {
                    addToken(TokenType.BANG_EQUAL);
                } else {
                    addToken(TokenType.BANG);
                }
            }

            case '-' -> {
                if (match('-')) {
                    comment();
                } else if (match('>')) {
                    addToken(TokenType.ARROW);
                } else {
                    addToken(TokenType.MINUS);
                }
            }

            case ' ', '\r', '\t' -> {break;}

            case '"' -> {string();}

            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Owlery.error(line, "unexpected character <" + c + ">");
                }
            }

        }
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private char peek (int lookahead) {
        if (current + lookahead >= source.length()) return '\0';
        return source.charAt(current + lookahead);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void comment() {
        while (peek(0) != '\n' && !isAtEnd())
            advance();
    }

    private void string() {
        while (peek(0) != '"' && (!isAtEnd() || peek(0) == '\n')) {
            advance();
        }

        if (isAtEnd()) {
            Owlery.error(line, "unterminated string, reached end of file");
            return;
        }

        if (peek(0) == '\n') {
            Owlery.error(line, "unterminated string, reached end of statement");
            return;
        }

        // last "
        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void number() {
        while (isDigit(peek(0))) advance();

        if (peek(0) == '.' && isDigit(peek(1))) {
            advance();
            while (isDigit(peek(0))) advance();
            addToken(TokenType.DOUBLE, Double.parseDouble(source.substring(start, current)));
            return;
        }
        addToken(TokenType.INTEGER, Integer.parseInt(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek(0))) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        addToken(type == null ? TokenType.IDENTIFIER : type);
    }
}
