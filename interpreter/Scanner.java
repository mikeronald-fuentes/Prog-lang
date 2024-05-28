package interpreter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static interpreter.TokenType.*; 

class Scanner {
  private static final Map<String, TokenType> keywords;
    private final List<Token> tokens = new ArrayList<>();
    private final String source;

    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
      this.source = source;
    }

    List<Token> scanTokens() {
      while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
        start = current;
        scanToken();
      }

      tokens.add(new Token(EOF, "", null, line));
      return tokens;
    }

    static {
      keywords = new HashMap<>();
      keywords.put("INT", INT);
      keywords.put("CHAR", CHAR);
      keywords.put("BOOL", BOOL);
      keywords.put("FLOAT", FLOAT);
      keywords.put("STRING", STRING);
      keywords.put("AND", AND);
      keywords.put("OR", OR);
      keywords.put("NOT", NOT);
      keywords.put("DISPLAY", DISPLAY);
      keywords.put("SCAN", SCAN);
      keywords.put("NULL",  NULL);
      keywords.put("CODE", CODE);
      keywords.put("BEGIN", BEGIN);
      keywords.put("END", END);
    }
    
    private void scanToken() {
        char c = advance();
        switch (c) {
        case '(': addToken(LEFT_PAREN); break;
        case ')': addToken(RIGHT_PAREN); break;
        case '[': consumeEscapeCode(); break;
        case '-': addToken(SUBTRACTION); break;
        case '+': addToken(ADDITION); break;
        case '*': addToken(MULTIPLY); break; 
        case '/': addToken(DIVISION); break;
        case '%': addToken(MODULO); break;
        case ',': addToken(COMMA); break;
        case '.': addToken(DOT); break;
        case ';': addToken(SEMICOLON); break;
        case ':': addToken(COLON); break;
        case '$': addToken(NEW_LINE); break;
        case '&': addToken(CONCATENATOR); break;
        case '"': string(); break;
        case '\'': charLiteral(); break;
        case '#': while (peek() != '\n' && !isAtEnd()) advance(); break;
        case '=':
          addToken(match('=') ? EQUAL_EQUAL : ASSIGN);
        break;
        case '<':
          if (match('>')){
            addToken(NOT_EQUAL);
          } else if (match('=')){
            addToken(LESS_THAN_EQUAL);
          } else {
            addToken(LESS_THAN);
          }
          break;
        case '>':
          if (match('=')){
            addToken(GREATER_THAN_EQUAL);
          } else {
            addToken(GREATER_THAN);
          }
          break;

        case ' ':
        case '\r':
        case '\t':
            break;

        case '\n':
            line++;
            break;

        default:
        if (isDigit(c)) {
            number();
        }else if(isAlpha(c)) {
            identifier();
        }else {
            Code.error(line, "Unexpected character.");
        }
        break;
        }
    }


    //Helper functions
    private boolean isAtEnd() {
      return current >= source.length();
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

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
    
        current++;
        return true;
      }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
          if (peek() == '\n') line++;
          advance();
        }
    
        if (isAtEnd()) {
          Code.error(line, "Unterminated string.");
          return;
        }
    
        advance();
    
        String value = source.substring(start + 1, current - 1);
        if(value.equals("TRUE")) {
          addToken(TRUE, Boolean.parseBoolean(value));
        } else if(value.equals("FALSE")) {
          addToken(FALSE, Boolean.parseBoolean(value));
        } else {
          addToken(STRING, value);
        }
      }
    
    private void charLiteral() {
      while (peek() != '\'' && !isAtEnd()) {
        if (peek() == '\n') line++;
        advance();
      }
  
      if (isAtEnd()) {
        Code.error(line, "Unterminated string.");
        return;
      }
  
      advance();
  
      char value = source.charAt(start + 1);
      addToken(CHAR, value);
    }

    private void consumeEscapeCode() {
      while(((peek() == ']' && peekNext() == ']') || peek() != ']') && !isAtEnd()) {
        if(peek() == '\n') return;
        advance();
      }

      if (!(current - start == 2)) {
        if(current - start < 2){
          Code.error(line, "Expected Escape Code between [ ]");
          return;
        }
        else{
          Code.error(line, "Expected One character between [ ]");
          return;
        }
      }

      advance();

      char value = source.charAt(start);
      if(value == '[') {
          addToken(ESCAPECODE, source.charAt(start+1));
          return;
      }
      Code.error(line, "Invalid Escape Character: '" + value + "'");
    }

    private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
    } 

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {

        
          advance();
    
          while (isDigit(peek())) advance();
        
    
        // return;
      } else if (isAlpha(peek())){
        Code.error(line, "Invalid variable name");
        return;
      }
      addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peekNext() {
      if (current + 1 >= source.length()) return '\0';
      return source.charAt(current + 1);
    }
    
    private void identifier() {
      while (isAlphaNumeric(peek())) advance();

      String text = source.substring(start, current);
      TokenType type = keywords.get(text);

      if (type == null) 
        type = IDENTIFIER;

      addToken(type);
    }

    private boolean isAlpha(char c) {
      return (c >= 'a' && c <= 'z') ||
              (c >= 'A' && c <= 'Z') ||
              c == '_';
    }
  
    private boolean isAlphaNumeric(char c) {
      return isAlpha(c) || isDigit(c);
    }  
}