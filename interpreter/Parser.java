// package interpreter;

// import java.util.List;

// import static interpreter.TokenType.*;

// class Parser {
//   private static class ParseError extends RuntimeException {
//   }
//   private final List<Token> tokens;
//   private int current = 0;

//   Parser(List<Token> tokens) {
//     this.tokens = tokens;
//   }

//   private Expr expression() {
//     return equality();
//   }

//   private Expr equality() {
//     Expr expr = comparison();

//     while (match(NOT_EQUAL, EQUAL_EQUAL)) {
//       Token operator = previous();
//       Expr right = comparison();
//       expr = new Expr.Binary(expr, operator, right);
//     }

//     return expr;
//   }

//   private boolean match(TokenType... types) {
//     for (TokenType type : types) {
//       if (check(type)) {
//         advance();
//         return true;
//       }
//     }

//     return false;
//   }

//   private boolean check(TokenType type) {
//     if (isAtEnd()) return false;
//     return peek().type == type;
//   }

//   private Token advance() {
//     if (!isAtEnd()) current++;
//     return previous();
//   }

//   private boolean isAtEnd() {
//     return peek().type == EOF;
//   }

//   private Token peek() {
//     return tokens.get(current);
//   }

//   private Token previous() {
//     return tokens.get(current - 1);
//   }

//   private Expr comparison() {
//     Expr expr = term();

//     while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
//       Token operator = previous();
//       Expr right = term();
//       expr = new Expr.Binary(expr, operator, right);
//     }

//     return expr;
//   }

//   private Expr term() {
//     Expr expr = factor();

//     while (match(MINUS, PLUS)) {
//       Token operator = previous();
//       Expr right = factor();
//       expr = new Expr.Binary(expr, operator, right);
//     }

//     return expr;
//   }

//   private Expr factor() {
//     Expr expr = unary();

//     while (match(SLASH, MULTIPLY)) {
//       Token operator = previous();
//       Expr right = unary();
//       expr = new Expr.Binary(expr, operator, right);
//     }

//     return expr;
//   }

//   private Expr unary() {
//     if (match(NOT, MINUS)) {
//       Token operator = previous();
//       Expr right = unary();
//       return new Expr.Unary(operator, right);
//     }

//     return primary();
//   }

//   private Expr primary() {
//     if (match(FALSE)) return new Expr.Literal(false);
//     if (match(TRUE)) return new Expr.Literal(true);
//     if (match(NULL)) return new Expr.Literal(null);

//     if (match(NUMBER, STRING)) {
//       return new Expr.Literal(previous().literal);
//     }

//     if (match(L_PAREN)) {
//       Expr expr = expression();
//       consume(R_PAREN, "Expect ')' after expression.");
//       return new Expr.Grouping(expr);
//     }
//   }

//   private Token consume(TokenType type, String message) {
//     if (check(type)) return advance();
    
    
//   }
//   // private ParseError error(Token token, String message) {
//   //   Code.error(token, message);
//   //   return new ParseError();
//   // }

//   // static void error(Token token, String message) {
//   //   if (token.type == TokenType.EOF) {
//   //     report(token.line, " at end", message);
//   //   } else {
//   //     report(token.line, " at '" + token.lexeme + "'", message);
//   //   }
//   // }

//   private void synchronize() {
//     advance();

//     while (!isAtEnd()) {
//         if (previous().type == CODE)
//             return;

//         switch (peek().type) {
//             case INT:
//             // case TYPECHAR:
//             case BOOL:
//             case IF:
//             case WHILE:
//             case SCAN:
//             case DISPLAY:
//             case END:
//                 return;
//         }

//         advance();
//     }
// }
// }
package interpreter;

import java.util.List;

import static interpreter.TokenType.*;


class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    // to point to the next token
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // first rule
    private Expr expression() {
        return equality();
    }

    // second rule
    private Expr equality() {
        Expr expr = comparison();
    
        while (match(NOT_EQUAL, EQUAL_EQUAL)) {
          Token operator = previous();
          Expr right = comparison();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    // ... maps to a while loop, match method indicates when the loop will stop
    // consumes token with has any of the given type and returns true; otherwise false
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
    
        return false;
    }

    // looks at the token and returns true if token is of given type
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    // consumes current token and returns it
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    // to check if there are no tokens left to parse
    private boolean isAtEnd() {
        return peek().type == EOF;
    }
    
    // returns current token that is yet to be consumed
    private Token peek() {
        return tokens.get(current);
    }
    
    // returns the most recent consumed token
    private Token previous() {
        return tokens.get(current - 1);
    }

    // same with equality but matches different token
    // same with comparison but it is now term()
    private Expr comparison() {
        Expr expr = term();
    
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
          Token operator = previous();
          Expr right = term();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    // order of precedence, addition - substraction
    private Expr term() {
        Expr expr = factor();
    
        while (match(MINUS, PLUS)) {
          Token operator = previous();
          Expr right = factor();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    // same with term, multiplication - division
    private Expr factor() {
        Expr expr = unary();
    
        while (match(SLASH, MULTIPLY)) {
          Token operator = previous();
          Expr right = unary();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    // for unary operators
    private Expr unary() {
        if (match(NOT, MINUS)) {
          Token operator = previous();
          Expr right = unary();
          return new Expr.Unary(operator, right);
        }
    
        return primary();
    }

    // highest level of precedence, primary expressions
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NULL)) return new Expr.Literal(null);
    
        if (match(NUMBER, STRING)) {
          return new Expr.Literal(previous().literal);
        }
    
        if (match(L_PAREN)) {
          Expr expr = expression();
          consume(R_PAREN, "Expect ')' after expression.");
          return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    // Job of parser 
    // 1. given valid tokens, produce corresponding syntax tree
    // 2. given an invalid tokens, detect any errors and tell users about it

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
    
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Code.error(token, message);
        return new ParseError();
    }

    

    // static void error(Token token, String message) {
    //     if (token.type == TokenType.EOF) {
    //       report(token.line, " at end", message);
    //     } else {
    //       report(token.line, " at '" + token.lexeme + "'", message);
    //     }
    // }

    // start of a statement keywords
    private void synchronize() {
        advance();
    
        while (!isAtEnd()) {
          if (previous().type == SEMICOLON) return;
    
          switch (peek().type) {
            case CLASS:
            case WHILE:
            case PRINT:
            case BEGIN:
            case END:
            case BEGIN_IF:
            case END_IF:
            case SCAN:
            case DISPLAY:
              return;
          }
    
          advance();
        }
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }
}