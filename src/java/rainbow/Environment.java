package rainbow;

import rainbow.functions.*;
import rainbow.types.*;

public class Environment {
  public static boolean debugJava = false;

  public Environment() { // todo: macex1, pipe-from, complex numbers, threading
    Java.collect(this);
    IO.collect(this);
    SystemFunctions.collect(this);
    Maths.collect(this);
    Typing.collect(this);
    ThreadLocals.collect(this);
    Threads.collect(this);
    FileSystem.collect(this);

    addBuiltin("t", ArcObject.T);
    addBuiltin("uniq", new Uniq());
    addBuiltin("newstring", new Lists.NewString());
    addBuiltin("macex", new Macex());

    /* errors */
    addBuiltin("protect", new Errors.Protect());
    addBuiltin("err", new Errors.Err());
    addBuiltin("on-err", new Errors.OnErr());
    addBuiltin("details", new Errors.Details());

    /* lists */
    addBuiltin("car", new Lists.Car());
    addBuiltin("cdr", new Lists.Cdr());
    addBuiltin("scar", new Lists.Scar());
    addBuiltin("scdr", new Lists.Scdr());
    addBuiltin("cons", new Lists.Cons());
    addBuiltin("len", new Lists.Len());

    /* predicates */
    addBuiltin("bound", new Predicates.Bound());
    addBuiltin("<", new Predicates.LessThan());
    addBuiltin(">", new Predicates.GreaterThan());
    addBuiltin("exact", new Predicates.Exact());
    addBuiltin("is", new Predicates.Is());

    /* special */
    addBuiltin("assign", new Specials.Set());
    addBuiltin("quote", new Specials.Quote());
    addBuiltin("quasiquote", new Specials.QuasiQuote());
    addBuiltin("if", new Specials.If());

    /* evaluation */
    addBuiltin("apply", new Evaluation.Apply());
    addBuiltin("eval", new Evaluation.Eval());
    addBuiltin("ssexpand", new Evaluation.SSExpand());
    addBuiltin("ssyntax", new Evaluation.SSyntax());

    /* tables */
    addBuiltin("table", new Tables.Table());
    addBuiltin("maptable", new Tables.MapTable());
    addBuiltin("sref", new Tables.Sref());
    addBuiltin("sig", new Hash());

    /* IO */
    addBuiltin("instring", new StringIO.InString());
    addBuiltin("outstring", new StringIO.OutString());
    addBuiltin("inside", new StringIO.Inside());
    addBuiltin("rmfile", new FileSystem.RmFile());
    addBuiltin("open-socket", new Network.OpenSocket());
    addBuiltin("socket-accept", new Network.SocketAccept());

    if (Console.ANARKI_COMPATIBILITY) {
      addBuiltin("seval", new Evaluation.Seval());
    }

    if (!Console.ARC2_COMPATIBILITY) {
      Maths.extra(this);
    }
  }

  private void addBuiltin(String name, ArcObject o) {
    ((Symbol) Symbol.make(name)).setValue(o);
  }

  public void addToNamespace(Symbol s, ArcObject o) {
    s.setValue(o);
  }

  public ArcObject lookup(Symbol s) {
    if (!s.bound()) {
      return null;
    }
    return s.value();
  }

  public Output stdOut() {
    return IO.STD_OUT;
  }

  public Input stdIn() {
    return IO.STD_IN;
  }

  public String fullNamespace() {
    return "Namespace\n" + toString();
  }

  public void add(Builtin[] builtins) {
    for (int i = 0; i < builtins.length; i++) {
      addBuiltin(builtins[i].name(), builtins[i]);
    }
  }
}
