package rainbow;

import rainbow.types.ArcObject;
import rainbow.types.Pair;
import rainbow.types.Symbol;
import rainbow.vm.ArcThread;
import rainbow.vm.Continuation;

import java.util.Collection;

public final class Nil extends Pair {
  public static final Symbol TYPE = Symbol.TYPE;
  public static final Nil NIL = new Nil();

  private Nil() {
  }

  public boolean isNil() {
    return true;
  }

  public String toString() {
    return "nil";
  }

  public ArcObject car() {
    return this;
  }

  public ArcObject cdr() {
    return this;
  }

  public void setCar(ArcObject item) {
    throw new Error("can't set the car of " + this);
  }

  public void setCdr(ArcObject cdr) {
    throw new Error("can't set the cdr of " + this);
  }

  public int size() {
    return 0;
  }

  public Collection copyTo(Collection c) {
    return c;
  }

  public Pair evalAll(Bindings arc) {
    return this;
  }

  public ArcObject type() {
    return Symbol.TYPE;
  }

  public int hashCode() {
    return "nil".hashCode();
  }

  public boolean equals(Object other) {
    return this == other || (other instanceof ArcObject && ((ArcObject)other).isNil());
  }

  public void invoke(ArcThread thread, Bindings namespace, Continuation whatToDo, Pair args) {
    throw new ArcError("Can't invoke " + this);
  }
}
