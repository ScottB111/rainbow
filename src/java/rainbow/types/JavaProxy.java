package rainbow.types;

import rainbow.ArcError;
import rainbow.Environment;
import rainbow.Function;
import rainbow.functions.Java;
import rainbow.vm.ArcThread;
import rainbow.vm.Interpreter;
import rainbow.vm.continuations.TopLevelContinuation;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JavaProxy implements InvocationHandler {
  private Environment environment;
  private Hash functions;
  private boolean strict;

  public JavaProxy(Environment environment, boolean strict, Hash functions) {
    this.environment = environment;
    this.functions = functions;
    this.strict = strict;
  }

  public Object invoke(Object target, Method method, Object[] arguments) throws Throwable {
    ArcThread thread = new ArcThread(environment);
    TopLevelContinuation topLevel = new TopLevelContinuation(thread);
    ArcObject methodImplementation = functions.value(Symbol.make(method.getName()));
    if (methodImplementation.isNil()) {
      if (method.getName().equals("toString")) {
        return functions.toString();
      } else {
        if (strict) {
          throw new ArcError("No implementation provided for " + method + "; implementations include " + functions);
        } else {
          return null;
        }
      }
    }
    Function f = (Function) methodImplementation;
    Pair args = (Pair) Java.wrap(arguments);
    f.invoke(thread, null, topLevel, args);
    thread.run();
    return JavaObject.unwrap(thread.finalValue(), method.getReturnType());
  }

  public static ArcObject create(Environment environment, String className, Hash functions, ArcObject strict) {
    try {
      Class target = Class.forName(className);
      return new JavaObject(Proxy.newProxyInstance(JavaProxy.class.getClassLoader(), new Class[] { target }, new JavaProxy(environment, !strict.isNil(), functions)));
    } catch (ClassNotFoundException e) {
      throw new ArcError("Class " + className + " not found", e);
    }
  }
}
