package rainbow.vm.continuations;

import rainbow.ArcError;
import rainbow.LexicalClosure;
import rainbow.types.ArcObject;
import rainbow.types.Output;
import rainbow.vm.Continuation;

public class CallWStdoutContinuation extends ContinuationSupport {
  private Output previousOutput;

  public CallWStdoutContinuation(LexicalClosure lc, Continuation caller, Output previousOutput) {
    super(lc, caller);
    this.previousOutput = previousOutput;
  }

  public void onReceive(ArcObject o) {
    thread.swapStdOut(previousOutput);
    caller.receive(o);
  }

  public void error(ArcError error) {
    thread.swapStdOut(previousOutput);
    super.error(error);
  }
}
