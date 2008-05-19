package rainbow.functions;

import rainbow.Environment;
import rainbow.LexicalClosure;
import rainbow.vm.ArcThread;
import rainbow.vm.Continuation;
import rainbow.types.*;

import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public abstract class Java {
  public static void collect(Environment top) {
    top.add(new Builtin[] {
        new Builtin("java-new") {
          protected ArcObject invoke(Pair args) {
            String className = ArcString.cast(args.car(), this).value();
            return JavaObject.instantiate(className);
          }
        }, new Builtin("java-class") {
          protected ArcObject invoke(Pair args) {
            String className = ArcString.cast(args.car(), this).value();
            return JavaObject.getClassInstance(className);
          }
        }, new Builtin("java-invoke") {
          protected ArcObject invoke(Pair args) {
            JavaObject target = JavaObject.cast(args.car(), this);
            String methodName = Symbol.cast(args.cdr().car(), this).name();
            return wrap(target.invoke(methodName));
          }
        }, new Builtin("java-static-invoke") {
          protected ArcObject invoke(Pair args) {
            JavaObject target = JavaObject.cast(args.car(), this);
            String methodName = Symbol.cast(args.cdr().car(), this).name();
            return wrap(target.staticInvoke(methodName, (Pair) args.cdr().cdr()));
          }
        }, new Builtin("java-static-field") {
          protected ArcObject invoke(Pair args) {
            JavaObject target = JavaObject.cast(args.car(), this);
            String fieldName = Symbol.cast(args.cdr().car(), this).name();
            return wrap(target.getStaticFieldValue(fieldName));
          }
        }, new Builtin("java-implement") {
          public void invoke(ArcThread thread, LexicalClosure lc, Continuation caller, Pair args) {
            String className = ArcString.cast(args.car(), this).value();
            Hash functions = Hash.cast(args.cdr().car(), this);
            caller.receive(JavaProxy.create(thread.environment(), className, functions));
          }
        }
    });
  }

  private static ArcObject wrap(Object o) {
    if (o instanceof ArcObject) {
      return (ArcObject) o;
    } else if (o instanceof Integer || o instanceof Long) {
      return Rational.make(((Number)o).longValue());
    } else if (o instanceof Float || o instanceof Double) {
      return Real.make(((Number)o).doubleValue());
    } else if (o instanceof String) {
      return ArcString.make(o.toString());
    } else if (o instanceof Character) {
      return ArcCharacter.make((Character)o);
    } else if (o instanceof List) {
      return wrapList((List)o);
    } else if (o instanceof Map) {
      return wrapMap((Map)o);
    } else {
      return new JavaObject(o);
    }
  }

  private static ArcObject wrapList(List list) {
    List result = new ArrayList(list.size());
    for (Iterator it = list.iterator(); it.hasNext();) {
      result.add(wrap(it.next()));
    }
    return Pair.buildFrom(result);
  }

  private static ArcObject wrapMap(Map map) {
    Hash hash = new Hash();
    for (Iterator it = map.keySet().iterator(); it.hasNext();) {
      Object key = it.next();
      Object value = map.get(key);
      hash.sref(wrap(key), wrap(value));
    }
    return hash;
  }

}
