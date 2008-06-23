package rainbow.vm.continuations;

import rainbow.LexicalClosure;
import rainbow.functions.InterpretedFunction;
import rainbow.types.ArcObject;
import rainbow.types.Pair;
import rainbow.types.Symbol;
import rainbow.vm.ArcThread;
import rainbow.vm.Continuation;
import rainbow.vm.Interpreter;

public class NamespaceBuilder extends ContinuationSupport {
  private static final Symbol o = (Symbol) Symbol.make("o");
  private ArcObject parameters;
  private Pair args;
  private InterpretedFunction f;

  private NamespaceBuilder(ArcThread thread, LexicalClosure lc, Continuation caller, ArcObject parameters, Pair arguments, InterpretedFunction f) {
    super(thread, lc, caller);
    this.parameters = parameters;
    this.args = arguments;
    this.f = f;
  }

  public static void build(ArcThread thread, LexicalClosure lc, Continuation caller, ArcObject parameters, Pair arguments, InterpretedFunction f) {
    if (parameters.isNil()) {
      FunctionEvaluator.evaluate(thread, lc, caller, f);
    } else {
      new NamespaceBuilder(thread, lc, caller, parameters, arguments, f).start();
    }
  }

  public void start() {
    if (parameters.isNil()) {
      finished();
      return;
    } else if (parameters instanceof Symbol) {
      lc.add(args);
      finished();
      return;
    }

    ArcObject nextParameter = parameters.car();
    ArcObject nextArg = args.car();
    if (nextParameter instanceof Symbol) {
      lc.add(nextArg);
    } else if (optional(nextParameter)) {
      Pair optional = optionalParam(nextParameter);
      if (!args.isNil()) {
        lc.add(nextArg);
      } else {
        Interpreter.interpret(thread, lc, this, optional.cdr().car());
        return;
      }
    } else {
      shift();
      new NamespaceBuilder(thread, lc, this, Pair.cast(nextParameter, this), Pair.cast(nextArg, this), null).start();
      return;
    }

    shift();
    start();
  }
  
  private void finished() {
    if (f == null) {
      ((NamespaceBuilder)caller).start();
    } else {
      FunctionEvaluator.evaluate(thread, lc, caller, f);
    }
  }

  private void shift() {
    parameters = parameters.cdr();
    args = (Pair) args.cdr();
  }

  public static boolean optional(ArcObject nextParameter) {
    if (!(nextParameter instanceof Pair)) {
      return false;
    }

    Pair p = (Pair) nextParameter;
    return p.car() == o;
  }

  public void onReceive(ArcObject o) {
    args = new Pair(o, args);
    start();
  }

  private Pair optionalParam(ArcObject nextParameter) {
    return (Pair) nextParameter.cdr();
  }

  public Continuation cloneFor(ArcThread thread) {
    NamespaceBuilder e = (NamespaceBuilder) super.cloneFor(thread);
    e.parameters = this.parameters.copy();
    e.args = this.args.copy();
    return e;
  }
}
