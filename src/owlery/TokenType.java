package owlery;

public enum TokenType {
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET, BANG,
    COMMA, MINUS, PLUS, SLASH, STAR, PERCENT, VERTICAL_BAR, DOUBLE_VERTICAL_BAR, HASHTAG,
    T_STRING, T_INTEGER, T_DOUBLE, T_BOOLEAN, T_LIST, T_CALLABLE,

    EOS,

    COLON, COLON_COLON,
    BANG_EQUAL, EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    AT, AT_AT,
    ARROW,
    DOT,

    IDENTIFIER, STRING, NUMBER, INTEGER, DOUBLE, BOOLEAN,

    CLASS,
    AND, OR, XOR, NOT,
    BIT_AND, BIT_OR, BIT_XOR, BIT_NOT,
    RETURN,
    IF, ELSE,
    LOOP, TO, INCL, IN, IS,
    NOTHING, TRUE, FALSE,
    PRINT,

    EOF
}
