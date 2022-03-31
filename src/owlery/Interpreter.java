package owlery;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.DoubleToLongFunction;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;

    Interpreter() {
        globals.assign("time", new OCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return (double)System.currentTimeMillis() / 1000.0;
            }
        });

        globals.assign("print", new OCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                System.out.println(stringify(args.get(0)));
                return args.get(0);
            }
        });

        globals.assign("read", new OCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Scanner scan = new Scanner(System.in);
                return scan.nextLine();
            }
        });

        globals.assign("length", new OCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Object arg = args.get(0);
                if (arg instanceof String str) {
                    return (double) str.length();
                }
                return 0;
            }
        });
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError e) {
            Owlery.runtimeError(e);
        }
    }

    private String stringify(Object object) {
        if (object == null) return "nothing";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name.lexeme, value);
        return value;
    }

    @Override
    public Object visitBooleanBinaryExpr(Expr.BooleanBinary expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else if (expr.operator.type == TokenType.AND) {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object l = evaluate(expr.left);
        Object r = evaluate(expr.right);

        if (expr.operator.type != TokenType.VERTICAL_BAR
                && expr.operator.type != TokenType.DOUBLE_VERTICAL_BAR
                && expr.operator.type != TokenType.EQUAL
                && expr.operator.type != TokenType.BANG_EQUAL

        ) {
                checkNumberOperands(expr.operator, l, r);
        }
        return switch (expr.operator.type) {
            case EQUAL -> isEqual(l, r);
            case BANG_EQUAL -> !isEqual(l, r);
            case GREATER -> (double) l > (double) r;
            case GREATER_EQUAL -> (double) l >= (double) r;
            case LESS -> (double) l < (double) r;
            case LESS_EQUAL -> (double) l <= (double) r;
            case MINUS -> (double) l - (double) r;
            case SLASH -> (double) l / (double) r;
            case STAR -> (double) l * (double) r;
            case PERCENT -> (double) l % (double) r;
            case PLUS -> (double) l + (double) r;
            case VERTICAL_BAR -> stringify(l) + stringify(r);
            case DOUBLE_VERTICAL_BAR -> stringify(l) + " " + stringify(r);
            default -> null;
        };
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object r = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS -> {
                checkNumberOperand(expr.operator, r);
                return -(double)r;}
            case NOT -> {return !isTruthy(r);}
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        List<Object> args = new ArrayList<>();
        for (Expr arg : expr.arguments) {
            args.add(evaluate(arg));
        }

        if (!(callee instanceof OCallable)) {
            throw new RuntimeError(expr.bang, "only functions and classes can be called.");
        }

        OCallable function = (OCallable) callee;

        if (args.size() != function.arity()) {
            throw new RuntimeError(expr.bang, "expected: " + function.arity() + "arguments\nbut got " + args.size());
        }

        return function.call(this, args);
    }

    @Override
    public Object visitFunctionExpr(Expr.Function expr) {
        return new OFunction(expr);
    }

    @Override
    public Object visitIndexExpr(Expr.Index expr) {
        Object value = environment.get(expr.name);
        if (evaluate(expr.index) instanceof Double indexInDouble) {
            int index = indexInDouble.intValue();
            if (value instanceof String str) {
                if (index >= 0 && index < str.length()) {
                    return "" + str.charAt(index);
                } else {
                    throw new RuntimeError(expr.name, "index out of bounds for the given string");
                }
            }
        }
        return null;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object operand1, Object operand2) {
        if (operand1 instanceof Double && operand2 instanceof Double) return;
        throw new RuntimeError(operator, "both operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        if (object instanceof Double num && num.equals(0)) return false;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitEmptyStmt(Stmt.Empty stmt) {
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.cond))) {
            execute(stmt.thenBlock);
        } else if (stmt.elseBlock != null) {
            execute(stmt.elseBlock);
        }

        return null;
    }

    @Override
    public Void visitLoopConditionStmt(Stmt.LoopCondition stmt) {
        while(isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitLoopRangeStmt(Stmt.LoopRange stmt) {
        if (stmt.from instanceof Expr.Assign assignment) {
            environment.assign(assignment.name.lexeme, evaluate(assignment.value));
            while((double) environment.get(assignment.name) < (double) evaluate(stmt.to)) {
                execute(stmt.body);
                environment.assign(assignment.name.lexeme, (double) environment.get(assignment.name) + 1);
            }
        } else {
            double from = (double) evaluate(stmt.from);
            while (from < (double) evaluate(stmt.to)) {
                execute(stmt.body);
                from = from + 1;
            }
        }
        return null;
    }

    @Override
    public Void visitLoopRangeInclStmt(Stmt.LoopRangeIncl stmt) {
        if (stmt.from instanceof Expr.Assign assignment) {
            environment.assign(assignment.name.lexeme, evaluate(assignment.value));
            while((double) environment.get(assignment.name) <= (double) evaluate(stmt.to)) {
                execute(stmt.body);
                environment.assign(assignment.name.lexeme, (double) environment.get(assignment.name) + 1);
            }
        } else {
            double from = (double) evaluate(stmt.from);
            while (from <= (double) evaluate(stmt.to)) {
                execute(stmt.body);
                from = from + 1;
            }
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        }

        throw new Return(value);
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
}
