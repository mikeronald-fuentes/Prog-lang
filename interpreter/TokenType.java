package interpreter;

enum TokenType {
    // Single-character tokens.
    CLASS, IF, ELSE, NULL, THIS, DISPLAY,
    TRUE, FALSE, WHILE, BEGIN, END, PRINT,
    SCAN, BEGIN_IF, END_IF, CODE,

    // literals
    INT, FLOAT, BOOL, DOUBLE, CHAR, STRING,
    IDENTIFIER, NUMBER, VARIABLE,
    COMMENT_SYMBOL, RESERVED,

    // one or two character tokens
    NOT, NOT_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    AND, OR, BIT_AND, BIT_OR,

    // single character tokens
    L_PAREN, R_PAREN, L_BRACE, R_BRACE,
    L_BRACKET, R_BRACKET, MULTIPLY, SLASH,
    MODULO, PLUS, MINUS, UNARY_MINUS,
    UNARY_PLUS, COMMA, SEMICOLON, DOT,
    CONCATENATOR, COMMENT, COLON,

    EOF
  }
