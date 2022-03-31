package owlery;

import java.util.List;

public interface OCallable {
    int arity();
    Object call (Interpreter interpreter, List<Object> args);
}
