package interpreter;

import java.util.ArrayList;
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
        return assignment();
    }
    private Expr assignment() {
        Expr expr = equality();
    
        if (match(ASSIGN)) {
          Token equals = previous();
          Expr value = assignment();
    
          if (expr instanceof Expr.Variable) {
            Token name = ((Expr.Variable)expr).name;
            return new Expr.Assign(name, value);
          }
    
          error(equals, "Invalid assignment target."); 
        }
    
        return expr;
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

    private boolean checkNext(TokenType type) {
        if (isAtEnd())
            return false;
        return peekNext().type == type;
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

    private Token peekNext() {
        return tokens.get(current + 1);
    }
    
    // returns the most recent consumed token
    private Token previous() {
        return tokens.get(current - 1);
    }

    // same with equality but matches different token
    // same with comparison but it is now term()
    private Expr comparison() {
        Expr expr = term();
    
        while (match(GREATER_THAN, GREATER_THAN_EQUAL, LESS_THAN, LESS_THAN_EQUAL)) {
          Token operator = previous();
          Expr right = term();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    // order of precedence, addition - substraction
    private Expr term() {
        Expr expr = factor();
    
        while (match(SUBTRACTION, ADDITION)) {
          Token operator = previous();
          Expr right = factor();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    // same with term, multiplication - division
    private Expr factor() {
        Expr expr = unary();
    
        while (match(DIVISION, MULTIPLY)) {
          Token operator = previous();
          Expr right = unary();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    // for unary operators
    private Expr unary() {
        if (match(NOT, SUBTRACTION)) {
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
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
          }
        if (match(LEFT_PAREN)) {
          Expr expr = expression();
          consume(RIGHT_PAREN, "Expect ')' after expression.");
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

    List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
        statements.add(declaration());
    }
    
    return statements; 
  }
  private Stmt declaration() {
    try {
      if (match(INT)) return varDeclaration("INT");

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt varDeclaration(String type) {
    Token name = consume(IDENTIFIER, "Expect variable name.");

    Expr initializer = null;
    if (match(ASSIGN)) {
      initializer = expression();
    }
    
    if(type.equals("INT")){
        return new Stmt.Int(name, initializer);
    }
    return new Stmt.Scan(name, initializer);
  }

  private Stmt statement() {
    if (match(DISPLAY) && match(COLON)) return displayStatement();
    if (match(BEGIN) && match(CODE)) {
        return new Stmt.Block(block());
    }
    if (match(LEFT_BRACE)) return new Stmt.Block(block());
    return expressionStatement();
    }

    
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(END) && !checkNext(CODE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(END, "Expect END after block.");
        consume(CODE, "Expect CODE after END.");
        return statements;
    }

    private Stmt displayStatement() {
        Expr value = expression();
        return new Stmt.Display(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }
}