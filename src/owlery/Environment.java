package owlery;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Value> values = new HashMap<>();
    final Environment enclosing;

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    boolean contains(String name) {
        return values.containsKey(name);
    }


    void assign(Token name, Object value) {
        Environment defined = isDefined(name.lexeme);
        if (defined == null || defined == this) {
            Value definedValue = get(name);
            if (definedValue == null) {
                throw new RuntimeError(name, "target of assignment is not defined");
            }

            if (definedValue.compatibleTypes(value)) {
                values.put(name.lexeme, new Value(definedValue.type, value));
            } else {
                throw new RuntimeError(name, "incompatible types");
            }
        } else {
            defined.assign(name, value);
        }
    }

    void define(Token name, Object value, OType type) {
        Value checker = new Value(type, null);
        if (checker.compatibleTypes(value)) {
            values.put(name.lexeme, new Value(type, value));
            return;
        }

        throw new RuntimeError(name, "incompatible types");
    }

    void define(String name, Object value, OType type) {
        values.put(name, new Value(type, value));
    }

    Value get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "variable <" + name.lexeme +"> is not defined in this scope.");
    }

    Value get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }
        return null;
    }

    Environment isDefined(String name) {
        if (values.containsKey(name)) {
            return this;
        }

        if (enclosing != null) {
            return enclosing.isDefined(name);
        }

        return null;
    }
}
