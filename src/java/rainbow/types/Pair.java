package rainbow.types;

import rainbow.types.ArcObject;
import rainbow.ArcError;
import rainbow.Bindings;
import rainbow.Function;
import rainbow.Nil;
import rainbow.functions.Builtin;
import rainbow.parser.Token;
import rainbow.vm.ArcThread;
import rainbow.vm.Continuation;

import java.util.*;

public class Pair extends ArcObject implements Function {
  public static final Symbol TYPE = (Symbol) Symbol.make("cons");
  private static final Map specials = new HashMap();

  static {
    specials.put("quasiquote", "`");
    specials.put("quote", "'");
    specials.put("unquote", ",");
    specials.put("unquote-splicing", ",@");
  }

  private ArcObject car;
  private ArcObject cdr;

  public Pair() {
  }

  public Pair(ArcObject car, ArcObject cdr) {
    if (car == null) {
      throw new ArcError("Can't create Pair with null car: use NIL instead");
    }
    if (cdr == null) {
      throw new ArcError("Can't create Pair with null cdr: use NIL instead");
    }
    this.car = car;
    this.cdr = cdr;
  }

  public ArcObject car() {
    return car == null ? NIL : car;
  }

  public ArcObject cdr() {
    return cdr == null ? NIL : cdr;
  }

  public String toString() {
    if (isSpecial()) {
      Symbol s = (Symbol) car();
      return specials.get(s.name()) + ((Pair)cdr()).internalToString();
    } else {
      return "(" + internalToString() + ")";
    }
  }

  private String internalToString() {
    if (isNil()) {
      return "";
    }
    if (car == null) {
      throw new Error("Can't have null car and non-null cdr: " + cdr);
    }
    if (cdr instanceof Pair) {
      Pair rest = (Pair) cdr;
      if (rest.isNil()) {
        return toString(car);
      } else {
        return toString(car) + " " + rest.internalToString();
      }
    } else if (cdr.isNil()) {
      return toString(car);
    } else {
      return toString(car) + " . " + toString(cdr);
    }
  }

  private String toString(ArcObject object) {
    return (car instanceof Builtin ? car.getClass().getSimpleName() : object.toString());
  }

  public void setCar(ArcObject item) {
    this.car = item;
  }

  public void setCdr(ArcObject cdr) {
    this.cdr = cdr;
  }

  public boolean isNil() {
    return car == null && cdr == null;
  }

  public static Pair buildFrom(List items, ArcObject last) {
    Pair pair = new Pair();
    if (items.size() != 0) {
      pair.car = (ArcObject) items.get(0);
      if (items.size() == 1) {
        pair.cdr = last;
      } else {
        pair.cdr = buildFrom(items.subList(1, items.size()), last);
      }
    }
    return pair;
  }

  public static Pair buildFrom(List items) {
    return buildFrom(items, NIL);
  }

  public static Pair buildFrom(ArcObject... items) {
    return buildFrom(Arrays.asList(items), NIL);
  }

  public ArcObject type() {
    return isNil() ? NIL.type() : TYPE;
  }

  public int size() {
    if (isNil()) {
      return 0;
    } else if (cdr instanceof Pair) {
      return 1 + ((Pair)cdr).size();
    } else {
      throw new ArcError("cannot take size: not a proper list: " + this);
    }
  }

  public int compareTo(ArcObject right) {
    throw new ArcError("Pair.compareTo:unimplemented");
  }

  public Collection copyTo(Collection c) {
    if (isNil()) {
      return c;
    }
    c.add(car());
    if (cdr().isNil()) {
      return c;
    } else if (!(cdr() instanceof Pair)) {
      throw new ArcError("Not a list: " + this);
    }
    ((Pair)cdr()).copyTo(c);
    return c;
  }

  public boolean equals(Object other) {
    if ((this == other)) {
      return true;
    } else {
      boolean iAmNil = isNil();
      boolean itIsNil = ((ArcObject) other).isNil();
      if ((iAmNil != itIsNil)) {
        return false;
      } else if (iAmNil && itIsNil) {
        return true;
      } else {
        boolean isPair = other instanceof Pair;
        if (isPair) {
          boolean eqCar = ((Pair) other).car.equals(car);
          boolean eqCdr = ((Pair) other).cdr.equals(cdr);
          if ((eqCar && eqCdr)) {
            return true;
          } else {
            return false;
          }
        } else {
          return false;
        }
      }
    }
  }

  public int hashCode() {
    return car.hashCode() + (37 * cdr().hashCode());
  }

  public void invoke(ArcThread thread, Bindings namespace, Continuation whatToDo, Pair args) {
    ArcNumber index = cast(args.car(), ArcNumber.class);
    whatToDo.eat(nth(index.toInt()).car());
  }

  public String code() {
    return "<pair>";
  }

  public Pair nth(long index) {
    if (index == 0) {
      return this;
    } else {
      return ((Pair) cdr()).nth(index - 1);
    }
  }

  public boolean isSpecial() {
    return car() instanceof Symbol && specials.containsKey(((Symbol) car()).name()) && cdr() instanceof Pair;
  }

  public ArcObject source(String sourceName, Token source) {
    ArcObject object = super.source(sourceName, source);
    if (!isNil() && !cdr().isNil() && cdr() instanceof Pair) {
      cdr().source(sourceName, source);
    }
    return object;
  }

  public String source() {
    return (isNil() || sourceName != null) ? super.source() : car().source();
  }

  public Pair copy() {
    if (isNil()) {
      return this;
    }
    return new Pair(car(), cdr().copy());
  }
}
