package owlery;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();
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

    void assign(String name, Object value) {
        Environment defined = isDefined(name);
        if (defined == null || defined == this) {
            values.put(name, value);
        } else {
            defined.assign(name, value);
        }
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "variable <" + name.lexeme +"> is not defined in this scope.");
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
