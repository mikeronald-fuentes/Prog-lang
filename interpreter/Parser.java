package interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static interpreter.TokenType.*;


class Parser {
    private static class ParseError extends RuntimeException {}
    private boolean inBlock = false;
    private final List<Token> tokens;
    private boolean startedExecutable = false;
    // to point to the next token
    private int current = 0;
    private boolean block = false;
    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // first rule
    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();
    
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
    
        while (match(NOT_EQUAL, EQUAL_EQUAL, NEW_LINE)) {
          Token operator = previous();
          Expr right = comparison();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    // same with equality but matches different token
    // same with comparison but it is now term()
    private Expr comparison() {
        Expr expr = term();
    
        while (match(GREATER_THAN, GREATER_THAN_EQUAL, LESS_THAN, LESS_THAN_EQUAL, NEW_LINE)) {
          Token operator = previous();
          Expr right = term();
          startedExecutable = true;
        //   System.out.println("omcmcomparison");
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    // order of precedence, addition - substraction
    private Expr term() {
        Expr expr = factor();
    
        while (match(SUBTRACTION, ADDITION, CONCATENATOR, NEW_LINE)) {
          Token operator = previous();
          Expr right = factor();
          startedExecutable = true;
        //   System.out.println("omcmterm");
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    // same with term, multiplication - division
    private Expr factor() {
        Expr expr = unary();
    
        while (match(DIVISION, MULTIPLY, MODULO, NEW_LINE)) {
          Token operator = previous();
          Expr right = unary();
          startedExecutable = true;
        //   System.out.println("omcmfactor");
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    // for unary operators
    private Expr unary() {
        if (match(NOT, SUBTRACTION, NEW_LINE)) {
          Token operator = previous();
          Expr right = unary();
          startedExecutable = true;
        //   System.out.println("omcmunary");
          return new Expr.Unary(operator, right);
        }
    
        return primary();
    }

    // highest level of precedence, primary expressions
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NULL)) return new Expr.Literal(null);
    
        if (match(NUMBER, STRING, CHAR)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(ESCAPECODE)) {
            // Handle escape code interpretation
            Token objectToken = previous();
            return new Expr.Literal(objectToken.literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        // return null;
        throw error(peek(), "Expect expression.");
    }
    
    private Expr or() {
        Expr expr = and();
    
        while (match(OR)) {
          Token operator = previous();
          Expr right = and();
          expr = new Expr.Logical(expr, operator, right);
        }
    
        return expr;
      }

      private Expr and() {
        Expr expr = equality();
    
        while (match(AND)) {
          Token operator = previous();
          Expr right = equality();
          expr = new Expr.Logical(expr, operator, right);
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
    private boolean checkNewline(TokenType type) {
        if (isAtEnd()) return false;
        return previous().type == type;
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
    
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
    
        throw error(peek(), message);
    }
    private Token consumeNewLine(TokenType type, String message) {
        if (checkNewline(type)) {System.out.println(type); return advance();}
    
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
          if (previous().type == CODE) return;
    
          switch (peek().type) {
            case INT:
            case CHAR:
            case NUMBER:
            case BOOL:
            case COMMA:
            case FLOAT:
            case IF:
            case WHILE:
            case SCAN:
            case DISPLAY:
            case END:
            case ESCAPECODE:
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
            if (match(CHAR)) 
                return variableDeclaration("CHAR");
            if (match(STRING)) 
                return variableDeclaration("STRING");
            if (match(BOOL)) 
                return variableDeclaration("BOOL");
            if (match(INT)) 
                return variableDeclaration("INT");
            if (match(FLOAT)) 
                return variableDeclaration("FLOAT");
            
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    
    private Stmt newLineStatement() {  
        consumeNewLine(NEW_LINE, "Expected '$' for new line.");
        return new Stmt.NewLine();
    }

    private Stmt variableDeclaration(String type) {
        List<Stmt> declarations = new ArrayList<>();
        Expr initializer = null;

        if (!(Character.isLetter(peek().lexeme.charAt(0))) && !(peek().lexeme.charAt(0) == '_')) {
            consume(IDENTIFIER, "Variable name must start with a letter or underscore.");
        }
        
        Token name = consume(IDENTIFIER, "Reserved keyword cannot be used as variable name.");

        if (startedExecutable){
            Code.error(previous().line, "Variable declarations must precede executable statements.");
        }

        if (match(ASSIGN)) {
            initializer = expression();
        }

        declarations.add(createVariableStmt(type, name, initializer));
    
        while (match(COMMA)) {
            name = consume(IDENTIFIER, "Expect variable name after comma.");
            initializer = null;
            if (match(ASSIGN)) {
                initializer = expression();
            }
            declarations.add(createVariableStmt(type, name, initializer));
        }
    
        if (declarations.size() == 1) {
            return declarations.get(0);
        } else {
            return new Stmt.variableDeclaration(declarations);
        }
    }

    private Stmt createVariableStmt(String type, Token name, Expr initializer) {
        switch (type) {
            case "CHAR":
                return new Stmt.Char(name, initializer);
            case "STRING":
                return new Stmt.String(name, initializer);
            case "BOOL":
                return new Stmt.Bool(name, initializer);
            case "INT":
                return new Stmt.Int(name, initializer);
            case "FLOAT":
                return new Stmt.Float(name, initializer);
            default:
                throw new ParseError();
        }
    }

    private Stmt statement() {
        
        if (match(BEGIN) && match(CODE)) {
            if(inBlock){
                throw error(peek(), "Unexpected input found after END CODE.");
            }
            inBlock = true;
            return new Stmt.Block(block());
        }
        
        if (match(END) && match(CODE)){
            if(inBlock) throw error(peek(), "Unexpected input found after END CODE.");
        }
        if (match(DISPLAY) && match(COLON)) return displayStatement();
        if (match(FOR)) return forStatement();
        if (match(SCAN) && match(COLON)) {return scanStatement();}
        if (match(NEW_LINE)) return newLineStatement();
        if (match(IF)) return ifStatement();
        if (match(WHILE)) return whileStatement();
        
        return expressionStatement();
    
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(INT)) {
            initializer = variableDeclaration("INT");
        } else {
            initializer = expressionStatement();
        }
        consume(SEMICOLON, "Expect ';' after loop initializer.");

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        Stmt body = null;
        if (match(BEGIN) && match(FOR)) {
            List<Stmt> statements = new ArrayList<>();
            while (!check(END) && !isAtEnd()) {
                statements.add(declaration());
            }
            body = new Stmt.Block(statements);
            consume(END, "Expected 'END' after 'BEGIN FOR' block.");
            consume(FOR, "Expected 'FOR' after 'END'.");
        } else {
            throw error(peek(), "Expected 'BEGIN FOR' after ')' in for loop.");
        }

        // Add increment as the last statement in the loop body
        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }

        // Construct the while loop with condition and body
        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);

        // Add initializer before the loop if present
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");

        Stmt body = null;
        if (match(BEGIN) && match(WHILE)) {
            List<Stmt> statements = new ArrayList<>();
            while (!check(END) && !isAtEnd()) {
                statements.add(declaration());
            }
            consume(END, "Expected 'END' after 'BEGIN IF' block.");
            consume(WHILE, "Expected 'IF' after 'END'.");
            body= new Stmt.Block(statements);
        } else {
            throw error(peek(), "Expected 'BEGIN IF' after condition");
        }

        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after if condition."); // [parens]

        Stmt thenBranch = null;
        if (match(BEGIN) && match(IF)) {
            List<Stmt> statements = new ArrayList<>();
            while (!check(END) && !isAtEnd()) {
                statements.add(declaration());
            }
            consume(END, "Expected 'END' after 'BEGIN IF' block.");
            consume(IF, "Expected 'IF' after 'END'.");
            thenBranch = new Stmt.Block(statements);
        } else {
            throw error(peek(), "Expected 'BEGIN IF' after condition");
        }

        Stmt elseBranch = null;
        if (match(ELSE)) {
            if (match(IF)) {
                elseBranch = ifStatement(); // Recursively handle else-if
            } else if (match(BEGIN) && match(IF)) {
                List<Stmt> elseStatements = new ArrayList<>();
                while (!check(END) && !isAtEnd()) {
                    elseStatements.add(declaration());
                }
                consume(END, "Expected 'END' after 'BEGIN IF' block.");
                consume(IF, "Expected 'IF' after 'END'.");
                elseBranch = new Stmt.Block(elseStatements);
            } else {
                throw error(peek(), "Expected 'BEGIN IF' after 'else'");
            }
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(END) && !checkNext(CODE) && !isAtEnd()) {
            inBlock = true;
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

    private Stmt scanStatement() {
        Token name = consume(IDENTIFIER, "Expect variable name after 'scan'.");
        return new Stmt.Scan(name, null);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        return new Stmt.Expression(expr);
    }
}