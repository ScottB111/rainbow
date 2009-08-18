package rainbow.functions;

import rainbow.types.ArcObject;
import rainbow.types.Symbol;
import rainbow.types.Pair;

public class Uniq extends Builtin {
  private static long count = 0;

  public Uniq() {
    super("uniq");
  }

  public ArcObject invoke(Pair args) {
    checkMaxArgCount(args, getClass(), 1);
    synchronized (getClass()) {
      return Symbol.mkSym("gs" + (++count));
    }
  }
}
