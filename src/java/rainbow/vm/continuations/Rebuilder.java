package rainbow.vm.continuations;

import rainbow.types.ArcObject;
import rainbow.types.Pair;
import rainbow.vm.Continuation;

public class Rebuilder extends ContinuationSupport {
  private ArcObject car;

  public Rebuilder(Continuation caller, ArcObject car) {
    super(null, null, caller);
    this.car = car;
  }

  protected void onReceive(ArcObject returned) {
    caller.receive(Pair.buildFrom(car, returned));
  }
}
