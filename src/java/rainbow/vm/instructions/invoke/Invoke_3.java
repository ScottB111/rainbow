package rainbow.vm.instructions.invoke;

import rainbow.types.ArcObject;
import rainbow.types.Symbol;
import rainbow.types.Pair;
import rainbow.vm.interpreter.BoundSymbol;
import rainbow.vm.VM;
import rainbow.vm.Instruction;

import java.util.List;

public class Invoke_3 {

  public static void addInstructions(List i, ArcObject fn, ArcObject arg1, ArcObject arg2, ArcObject arg3) {
    arg1.addInstructions(i);
    arg2.addInstructions(i);
    arg3.addInstructions(i);
    if (fn instanceof BoundSymbol) {
      i.add(new Lex(((BoundSymbol) fn)));
    } else if (fn instanceof Symbol) {
      i.add(new Free(((Symbol) fn)));
    } else {
      fn.addInstructions(i);
      i.add(new Other());
    }
  }

  private static class Lex extends Instruction implements Invoke {
    protected BoundSymbol fn;

    public Lex(BoundSymbol fn) {
      this.fn = fn;
    }

    public void operate(VM vm) {
      ArcObject arg3 = vm.popA();
      ArcObject arg2 = vm.popA();
      ArcObject arg1 = vm.popA();
      fn.interpret(vm.lc()).invoke(vm, new Pair(arg1, new Pair(arg2, new Pair(arg3, NIL))));
    }

    public String toString() {
      return "(invoke " + fn + " <3>)";
    }

    public ArcObject getInvokee(VM vm) {
      return fn.interpret(vm.lc());
    }
  }

  private static class Free extends Instruction implements Invoke {
    protected Symbol fn;

    public Free(Symbol fn) {
      this.fn = fn;
    }

    public void operate(VM vm) {
      ArcObject arg3 = vm.popA();
      ArcObject arg2 = vm.popA();
      ArcObject arg1 = vm.popA();
      fn.value().invoke(vm, new Pair(arg1, new Pair(arg2, new Pair(arg3, NIL))));
    }

    public String toString() {
      return "(invoke " + fn + " <3>)";
    }

    public ArcObject getInvokee(VM vm) {
      return fn;
    }
  }

  private static class Other extends Instruction implements Invoke {
    public void operate(VM vm) {
      ArcObject f = vm.popA();
      ArcObject arg3 = vm.popA();
      ArcObject arg2 = vm.popA();
      ArcObject arg1 = vm.popA();
      f.invoke(vm, new Pair(arg1, new Pair(arg2, new Pair(arg3, NIL))));
    }

    public String toString() {
      return "(invoke <3>)";
    }

    public ArcObject getInvokee(VM vm) {
      return vm.peekA();
    }
  }
}
