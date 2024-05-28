package interpreter;

import java.util.HashMap;
import java.util.Map;

class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, String> token = new HashMap<>();
    
    Environment() {
        this.enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
        return values.get(name.lexeme);
        }
        if (enclosing != null) return enclosing.get(name);
        throw new RuntimeError(name,
            "Undefined variable '" + name.lexeme + "'.");
    }

    String getTokenType(String name) {
        if (token.containsKey(name)) {
            return token.get(name);
        }
        if (enclosing != null) {
            return enclosing.getTokenType(name);
        }
        return null;
    }
    
    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void define(Token name, Object value) {
        if (values.containsKey(name.lexeme)){
            throw new RuntimeError(name, "Undefined variable '" + name + "'.");
        }
        values.put(name.lexeme, value);
    }

    void define(Token name, Object value, String token_type) {
        values.put(name.lexeme, value);
        token.put(name.lexeme, token_type);
    }
}