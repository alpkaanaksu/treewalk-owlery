package owlery;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;

    Interpreter() {
        globals.define("time", new OCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return (double)System.currentTimeMillis() / 1000.0;
            }
        }, OType.Callable);

        globals.define("print", new OCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                System.out.println(stringify(args.get(0)));
                return args.get(0);
            }
        }, OType.Callable);

        globals.define("read", new OCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Scanner scan = new Scanner(System.in);
                return scan.nextLine();
            }
        }, OType.Callable);

        globals.define("length", new OCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Object arg = args.get(0);
                if (arg instanceof String str) {
                    return str.length();
                }
                return 0;
            }
        }, OType.Callable);
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
        return object.toString();
    }

    @Override
    public Object visitDefineExpr(Expr.Define expr) {
        environment.define(expr.name, evaluate(expr.value), expr.type);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
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
        switch (expr.operator.type) {
            case EQUAL -> {
                return isEqual(l, r);
            }
            case BANG_EQUAL -> {
                return !isEqual(l, r);
            }
            case GREATER -> {
                checkNumberOperands(expr.operator, l, r);
                if (l instanceof Integer && r instanceof Integer) {
                    return (int) l > (int) r;
                }

                if (l instanceof Double && r instanceof Double) {
                    return (double) l > (double) r;
                }

                if (l instanceof Integer && r instanceof Double) {
                    return (int) l > (double) r;
                }

                if (l instanceof Double && r instanceof Integer) {
                    return (double) l > (int) r;
                }
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, l, r);
                if (l instanceof Integer && r instanceof Integer) {
                    return (int) l >= (int) r;
                }

                if (l instanceof Double && r instanceof Double) {
                    return (double) l >= (double) r;
                }

                if (l instanceof Integer && r instanceof Double) {
                    return (int) l >= (double) r;
                }

                if (l instanceof Double && r instanceof Integer) {
                    return (double) l >= (int) r;
                }
            }
            case LESS -> {
                checkNumberOperands(expr.operator, l, r);
                if (l instanceof Integer && r instanceof Integer) {
                    return (int) l < (int) r;
                }

                if (l instanceof Double && r instanceof Double) {
                    return (double) l < (double) r;
                }

                if (l instanceof Integer && r instanceof Double) {
                    return (int) l < (double) r;
                }

                if (l instanceof Double && r instanceof Integer) {
                    return (double) l < (int) r;
                }
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, l, r);
                if (l instanceof Integer && r instanceof Integer) {
                    return (int) l <= (int) r;
                }

                if (l instanceof Double && r instanceof Double) {
                    return (double) l <= (double) r;
                }

                if (l instanceof Integer && r instanceof Double) {
                    return (int) l <= (double) r;
                }

                if (l instanceof Double && r instanceof Integer) {
                    return (double) l <= (int) r;
                }
            }
            case MINUS -> {
                checkNumberOperands(expr.operator, l, r);
                if (l instanceof Integer && r instanceof Integer) {
                    return (int) l - (int) r;
                }

                if (l instanceof Double && r instanceof Double) {
                    return (double) l - (double) r;
                }

                if (l instanceof Integer && r instanceof Double) {
                    return (int) l - (double) r;
                }

                if (l instanceof Double && r instanceof Integer) {
                    return (double) l - (int) r;
                }
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, l, r);
                if (l instanceof Integer && r instanceof Integer) {
                    return (int) l / (int) r;
                }

                if (l instanceof Double && r instanceof Double) {
                    return (double) l / (double) r;
                }

                if (l instanceof Integer && r instanceof Double) {
                    return (int) l / (double) r;
                }

                if (l instanceof Double && r instanceof Integer) {
                    return (double) l / (int) r;
                }
            }
            case STAR -> {
                checkNumberOperands(expr.operator, l, r);
                if (l instanceof Integer && r instanceof Integer) {
                    return (int) l * (int) r;
                }

                if (l instanceof Double && r instanceof Double) {
                    return (double) l * (double) r;
                }

                if (l instanceof Integer && r instanceof Double) {
                    return (int) l * (double) r;
                }

                if (l instanceof Double && r instanceof Integer) {
                    return (double) l * (int) r;
                }
            }
            case PERCENT -> {
                checkNumberOperands(expr.operator, l, r);
                if (l instanceof Integer && r instanceof Integer) {
                    return (int) l % (int) r;
                }

                if (l instanceof Double && r instanceof Double) {
                    return (double) l % (double) r;
                }

                if (l instanceof Integer && r instanceof Double) {
                    return (int) l % (double) r;
                }

                if (l instanceof Double && r instanceof Integer) {
                    return (double) l % (int) r;
                }
            }
            case PLUS -> {
                checkNumberOperands(expr.operator, l, r);
                if (l instanceof Integer && r instanceof Integer) {
                    return (int) l + (int) r;
                }

                if (l instanceof Double && r instanceof Double) {
                    return (double) l + (double) r;
                }

                if (l instanceof Integer && r instanceof Double) {
                    return (int) l + (double) r;
                }

                if (l instanceof Double && r instanceof Integer) {
                    return (double) l + (int) r;
                }
            }
            case VERTICAL_BAR -> {
                return stringify(l) + stringify(r);
            }
            case DOUBLE_VERTICAL_BAR -> {
                return stringify(l) + " " + stringify(r);
            }
        };
        return null;
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
        return environment.get(expr.name).value;
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
        Object value = environment.get(expr.name).value;
        if (evaluate(expr.index) instanceof Integer indexInInt) {
            int index = indexInInt.intValue();
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

    @Override
    public Object visitConversionExpr(Expr.Conversion expr) {
        switch (expr.type) {
            case String -> {
                return stringify(evaluate(expr.expression));
            }

            case Integer -> {
                if (evaluate(expr.expression) instanceof String s) {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        throw new RuntimeError(expr.hashtag, "given string can not be converted to an integer");
                    }
                }

                if (evaluate(expr.expression) instanceof Double d) {
                    return d.intValue();
                }

                if (evaluate(expr.expression) instanceof Integer i) {
                    return i;
                }

                if (evaluate(expr.expression) instanceof Boolean b) {
                    return b ? 1 : 0;
                }
            }

            case Double -> {
                if (evaluate(expr.expression) instanceof String s) {
                    try {
                        return Double.parseDouble(s);
                    } catch (NumberFormatException e) {
                        throw new RuntimeError(expr.hashtag, "given string can not be converted to an integer");
                    }
                }

                if (evaluate(expr.expression) instanceof Double d) {
                    return d;
                }

                if (evaluate(expr.expression) instanceof Integer i) {
                    return (double) i;
                }

                if (evaluate(expr.expression) instanceof Boolean b) {
                    return b ? 1.0 : 0.0;
                }
            }

            case Boolean -> {
                return isTruthy(evaluate(expr.expression));
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
        if (operand instanceof Number) return;
        throw new RuntimeError(operator, "operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object operand1, Object operand2) {
        if (operand1 instanceof Number && operand2 instanceof Number) return;
        throw new RuntimeError(operator, "both operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        if (object instanceof Double num && num.equals(0)) return false;
        if (object instanceof Integer num && num.equals(0)) return false;
        if (object instanceof String s && s.isEmpty()) return false;
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
            environment.define(assignment.name.lexeme, evaluate(assignment.value), OType.Integer);
            while((int) environment.get(assignment.name).value < (int) evaluate(stmt.to) + (stmt.incl ? 1 : 0)) {
                execute(stmt.body);
                environment.assign(assignment.name, (int) environment.get(assignment.name).value+ 1);
            }
        } else {
            int from = (int) evaluate(stmt.from);
            while (from < (int) evaluate(stmt.to)) {
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
