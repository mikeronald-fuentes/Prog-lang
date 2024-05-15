package interpreter;

enum TokenType {
    // Single-character tokens.
    CLASS, IF, ELSE, NULL, THIS, DISPLAY,
    TRUE, FALSE, WHILE, BEGIN, END, PRINT,
    SCAN, BEGIN_IF, END_IF, CODE, 

    // literals
    INT, FLOAT, BOOL, DOUBLE, CHAR, STRING, TYPESTRING,
    IDENTIFIER, NUMBER, VARIABLE, ASSIGN,
    COMMENT_SYMBOL, RESERVED,

    // one or two character tokens
    NOT, NOT_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER_THAN, GREATER_THAN_EQUAL,
    LESS_THAN, LESS_THAN_EQUAL,
    AND, OR, BIT_AND, BIT_OR,

    // single character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_ESCAPE,
    MULTIPLY, DIVISION, ADDITION, SUBTRACTION, MODULO,
    UNARY_MINUS, UNARY_PLUS, COMMA, SEMICOLON, DOT,
    CONCATENATOR, NEW_LINE, COMMENT, COLON, 

    EOF
  }
