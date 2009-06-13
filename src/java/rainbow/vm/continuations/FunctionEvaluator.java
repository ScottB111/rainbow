package rainbow.vm.continuations;

import rainbow.vm.Continuation;
import rainbow.vm.ArcThread;
import rainbow.vm.Interpreter;
import rainbow.LexicalClosure;
import rainbow.functions.InterpretedFunction;
import rainbow.types.ArcObject;

public class FunctionEvaluator extends ContinuationSupport {
  private InterpretedFunction f;
  private int index;

  public FunctionEvaluator(ArcThread thread, LexicalClosure lc, Continuation caller, InterpretedFunction f) {
    super(thread, lc, caller);
    this.f = f;
  }

  public void onReceive(ArcObject o) {
    ArcObject expression = f.nth(index++);
    Interpreter.interpret(thread, lc, f.last(index) ? caller : this, expression);
  }

  protected ArcObject getCurrentTarget() {
    return index == 0 ? ArcObject.NIL : f.nth(index - 1);
  }

  public static void evaluate(ArcThread thread, LexicalClosure lc, Continuation caller, InterpretedFunction f) {
    switch (f.length()) {
      case 0:
        caller.receive(ArcObject.NIL);
        break;
      case 1:
        Interpreter.interpret(thread, lc, caller, f.nth(0));
        break;
      default:
        new FunctionEvaluator(thread, lc, caller, f).receive(ArcObject.NIL);
    }
  }
}
