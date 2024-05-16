package interpreter;

import static interpreter.TokenType.ADDITION;
import static interpreter.TokenType.SUBTRACTION;
import interpreter.Expr.Assign;
import interpreter.Expr.Binary;
import interpreter.Expr.Grouping;
import interpreter.Expr.Literal;
import interpreter.Expr.Unary;
import interpreter.Expr.Variable;
import interpreter.Stmt.Block;
import interpreter.Stmt.Bool;
import interpreter.Stmt.Char;
import interpreter.Stmt.Display;
import interpreter.Stmt.Float;
import interpreter.Stmt.Int;
import interpreter.Stmt.Scan;
import interpreter.Stmt.NewLine;
import java.util.List;

class Interpreter implements Expr.Visitor<Object>,
                             Stmt.Visitor<Void> {
    private Environment environment = new Environment();

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }
    
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }
    @Override
    public Void visitDisplayStmt(Stmt.Display stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitScanStmt(Stmt.Scan stmt) {
        Object value = null;
        if (stmt.initializer != null) {
        value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right); 

        switch (expr.operator.type) {
        case GREATER_THAN:
            checkNumberOperands(expr.operator, left, right);
            return (double)left > (double)right;
        case GREATER_THAN_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return (double)left >= (double)right;
        case LESS_THAN:
            checkNumberOperands(expr.operator, left, right);
            return (double)left < (double)right;
        case LESS_THAN_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return (double)left <= (double)right;
        case SUBTRACTION:
            checkNumberOperands(expr.operator, left, right);
            return (double)left - (double)right;
        case ADDITION:
            // if (left instanceof Double && right instanceof Double) {
            return (double)left + (double)right;
            // } 

            // if (left instanceof String && right instanceof String) {
            // return (String)left + (String)right;
            // }
            
            // throw new RuntimeError(expr.operator,
            // "Operands must be two numbers or two strings.");
        case DIVISION:
            checkNumberOperands(expr.operator, left, right);
            return (double)left / (double)right;
        case MULTIPLY:
            checkNumberOperands(expr.operator, left, right);
            return (double)left * (double)right;
        case NOT_EQUAL: return !isEqual(left, right);
        case EQUAL_EQUAL: return isEqual(left, right);
        case MODULO: 
            checkNumberOperands(expr.operator, left, right);
            return (double)left % (double)right;
        case CONCATENATOR: 
            return stringify(left) + stringify(right);
        // case NEW_LINE:
        //     return (stringify(left) + "\n" + stringify(right));

        }

        // Unreachable.
        return null;
    }

    @Override
    public Void visitIntStmt(Int stmt) {
        Object value = null;
        if (stmt.intializer != null) {
            value = evaluate(stmt.intializer);
            if (!(value instanceof Double)) {
                throw new RuntimeError(stmt.name, "Input must be an Integer");
            }
        }

        String Tokentype = "Integer";

        environment.define(stmt.name.lexeme, value, Tokentype);
        return null;
    }

    @Override
    public Void visitCharStmt(Char stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof Character)) {
                throw new RuntimeError(stmt.name, "Input must be an Character");
            }
        }

        String Tokentype = "Character";

        environment.define(stmt.name.lexeme, value, Tokentype);
        return null;
    }

    @Override
    public Void visitStringStmt(Stmt.String stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof String)) {
                throw new RuntimeError(stmt.name, "Input must be an String");
            }
        }

        String Tokentype = "String";

        environment.define(stmt.name.lexeme, value, Tokentype);
        return null;
    }

    @Override
    public Void visitBoolStmt(Bool stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof Boolean)) {
                throw new RuntimeError(stmt.name, "Input must be an Boolean");
            }
        }

        String Tokentype = "Boolean";

        environment.define(stmt.name.lexeme, value, Tokentype);
        return null;
    }

    @Override
    public Void visitFloatStmt(Float stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof Float)) {
                throw new RuntimeError(stmt.name, "Input must be an Float");
            }
        }

        String Tokentype = "Float";

        environment.define(stmt.name.lexeme, value, Tokentype);
        return null;
    }
    
    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
        case NOT:
            return !isTruthy(right);
        case SUBTRACTION:
            checkNumberOperand(expr.operator, right);
            return -(double)right;
        }

        // Unreachable.
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }
    
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitVariableDeclarationStmt(Stmt.variableDeclaration stmt) {
        for (Stmt declaration : stmt.declarations) {
            execute(declaration);
        }
        return null;
    }

    @Override
    public Void visitNewLineStmt(NewLine stmt) { 
        System.out.println(); 
        return null;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
    
        return a.equals(b);
    }

    void interpret(List<Stmt> statements) {
        try {
        for (Stmt statement : statements) {
            execute(statement);
        }
        } catch (RuntimeError error) {
            Code.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private String stringify(Object object) {
        if (object == null) return "null";
    
        if (object instanceof Double) {
          String text = object.toString();
          if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
          }
          return text;
        }
    
        return object.toString();
      }
}
