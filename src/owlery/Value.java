package owlery;

import java.awt.*;
import java.util.List;

public class Value {
    OType type;
    Object value;

    Value(OType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public boolean compatibleTypes(Object o) {
        return switch (type) {
            case Integer -> o instanceof Integer;
            case Double -> o instanceof Double;
            case String -> o instanceof String;
            case Boolean -> o instanceof Boolean;
            case List -> o instanceof List<?>;
            case Callable -> o instanceof OCallable;
            case Flexible -> true;
        };
    }
}
