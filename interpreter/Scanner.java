package interpreter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static interpreter.TokenType.*; 

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
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

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
        case '(': addToken(L_PAREN); break;
        case ')': addToken(R_PAREN); break;
        case '{': addToken(L_BRACE); break;
        case '}': addToken(R_BRACE); break;
        case ',': addToken(COMMA); break;
        case '.': addToken(DOT); break;
        case '-': addToken(MINUS); break;
        case '+': addToken(PLUS); break;
        case ';': addToken(SEMICOLON); break;
        case '*': addToken(MULTIPLY); break; 
        case '"': string(); break;
        case '\'': charLiteral(); break;
        case '!':
        addToken(match('=') ? NOT_EQUAL : NOT);
        break;
        case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
        case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
        case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
        case '/':
        if (match('/')) {
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(SLASH);
        }
        break;

        case ' ':
        case '\r':
        case '\t':
            // Ignore whitespace.
            break;

        case '\n':
            line++;
            break;

        
        case 'o':
            if (match('r')) {
            addToken(OR);
        }
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
    
        // The closing ".
        advance();
    
        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
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
  
      // The closing ".
      advance();
  
      // Trim the surrounding quotes.
      char value = source.charAt(start + 1);
      addToken(CHAR, value);
    }
    
    private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
    } 

    private void number() {
        while (isDigit(peek())) advance();
    
        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
          // Consume the "."
          advance();
    
          while (isDigit(peek())) advance();
        }
    
        addToken(NUMBER,
            Double.parseDouble(source.substring(start, current)));
      }

    private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
    }
    
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
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

      private static final Map<String, TokenType> keywords;

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
}