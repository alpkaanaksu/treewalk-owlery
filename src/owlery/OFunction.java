package owlery;

import java.util.List;

public class OFunction implements OCallable {
    Expr.Function function;

    OFunction(Expr.Function function) {
        this.function = function;
    }

    @Override
    public int arity() {
        return function.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment environment = new Environment(interpreter.globals);
        int argCount = 0;
        for (Token param : function.params) {
            environment.define(param, args.get(argCount++), OType.Flexible);
        }
        try {
            interpreter.executeBlock(function.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }
}
