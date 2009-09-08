package rainbow.vm;

import rainbow.ArcError;
import rainbow.LexicalClosure;
import rainbow.Nil;
import rainbow.types.ArcException;
import rainbow.types.ArcObject;
import rainbow.types.Pair;
import rainbow.types.Symbol;
import rainbow.vm.instructions.Catch;
import rainbow.vm.instructions.Finally;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VM extends ArcObject {
  public static final Symbol TYPE = Symbol.mkSym("thread");
  private static long threadCount = 0;

  private final long threadId;

  {
    synchronized(VM.class) {
      threadId = threadCount++;
    }
  }

  public ArcObject[] args = new ArcObject[100];
  public int ap = -1;

  public ArcObject[][] params = new ArcObject[100][];
  public LexicalClosure[] lcs = new LexicalClosure[100];
  public Pair[] ins = new Pair[100];
  public int ip = -1;

  private LexicalClosure currentLc;
  private ArcObject[] currentParams;

  private ArcException error;
  private VMInterceptor interceptor = VMInterceptor.NULL;
  private boolean dead = false;
  private int ipThreshold;
  public Map<String, Integer> profileData;
  public int debug_target_frame;

  public ArcObject thread(LexicalClosure lc, Pair instructions) { // todo dump this, inline to callers
    pushInvocation(lc, instructions);
    return thread();
  }

  public ArcObject thread() {
    int oldThreshold = ipThreshold;
    ipThreshold = ip;
    try {
      loop();
    } finally {
      ipThreshold = oldThreshold;
    }
    if (error != null) {
      ArcException ae = error;
      error = null;
      throw new ArcError("Unhandled exception on thread#" + threadId + ": " + ae.getOriginal().getMessage(), ae.getOriginal());
    }
    interceptor.end(this);
    return popA();
  }

  private void loop() {
    interceptor.check(this);
    while (ip >= ipThreshold) {
      try {
        if (ins[ip] instanceof Nil) {
          ip--;
        } else {
          currentLc = lcs[ip];
          currentParams = params[ip];
          Instruction i = (Instruction) ins[ip].car();
          ins[ip] = ((Pair) ins[ip].cdr());
//          Instruction.invoke(i);
          i.operate(this);
          interceptor.check(this);
        }
      } catch (Throwable e) {
        handleError(e);
      }
    }
  }

  public void loadCurrentContext() {
    currentLc = peekL();
  }

  public boolean hasInstructions() {
    return ip >= ipThreshold;
  }

  private void handleError(Throwable e) {
    List instructions = new ArrayList();
    List lexClosures = new ArrayList();

    while (ip >= ipThreshold && (!(peekI().car() instanceof Catch))) {
      if (peekI().car() instanceof Finally) {
        instructions.add(peekI());
        lexClosures.add(peekL());
      }
      popFrame();
    }

    this.error = new ArcException(e);

    for (int i = instructions.size() - 1; i >= 0; i--) {
      pushInvocation((LexicalClosure) lexClosures.get(i), (Pair) instructions.get(i));
    }
  }

  public void die() {
    dead = true;
    List instructions = new ArrayList();
    List lexClosures = new ArrayList();

    while (ip >= 0) {
      if (peekI().car() instanceof Finally) {
        instructions.add(peekI());
        lexClosures.add(peekL());
      }
      popFrame();
    }

    for (int i = instructions.size() - 1; i >= 0; i--) {
      pushInvocation((LexicalClosure) lexClosures.get(i), (Pair) instructions.get(i));
    }
  }

  public ArcException error() {
    return error;
  }

  public void show() {
    System.out.println("Thread Dump for thread#" + threadId);
    System.out.println("" + (ap + 1) + " args");
    showArgs();
    System.out.println();
    System.out.println("" + (ip + 1) + " instruction frames");
    showInstructions();
  }

  public void popFrame() {
    ip--;
  }

  public void pushFrame(Instruction i) {
    if (ip >= ipThreshold && peekI() instanceof Nil) {
      ins[ip] = new Pair(i, NIL);
    } else {
      ++ip;
      try {
        ins[ip] = new Pair(i, NIL);
      } catch (ArrayIndexOutOfBoundsException e) {
        newInstructionArray(ins.length * 2);
        newClosureArray(lcs.length * 2);
        newParamsArray(params.length * 2);
        ins[ip] = new Pair(i, NIL);
      }
    }
  }

  public void pushInvocation(LexicalClosure lc, Pair instructions) {
    if (ip >= ipThreshold && peekI() instanceof Nil) {
      ins[ip] = instructions;
      lcs[ip] = lc;
    } else {
      ++ip;
      try {
        ins[ip] = instructions;
        lcs[ip] = lc;
      } catch (ArrayIndexOutOfBoundsException e) {
        newInstructionArray(ins.length * 2);
        newClosureArray(lcs.length * 2);
        newParamsArray(params.length * 2);
        ins[ip] = instructions;
        lcs[ip] = lc;
      }
    }
  }

  public void pushInvocation(LexicalClosure lc, Pair instructions, ArcObject[] args) {
    if (ip >= ipThreshold && peekI() instanceof Nil) {
      ins[ip] = instructions;
      lcs[ip] = lc;
      params[ip] = args;
    } else {
      ++ip;
      try {
        ins[ip] = instructions;
        lcs[ip] = lc;
        params[ip] = args;
      } catch (ArrayIndexOutOfBoundsException e) {
        newInstructionArray(ins.length * 2);
        newClosureArray(lcs.length * 2);
        newParamsArray(params.length * 2);
        ins[ip] = instructions;
        lcs[ip] = lc;
        params[ip] = args;
      }
    }
  }

  public void pushConditional(Pair instructions) {
    if (ip >= ipThreshold && peekI() instanceof Nil) {
      ins[ip] = instructions;
    } else {
      ++ip;
      try {
        ins[ip] = instructions;
        lcs[ip] = currentLc;
        params[ip] = currentParams;
      } catch (ArrayIndexOutOfBoundsException e) {
        newInstructionArray(ins.length * 2);
        newClosureArray(lcs.length * 2);
        newParamsArray(params.length * 2);
        ins[ip] = instructions;
        lcs[ip] = currentLc;
        params[ip] = currentParams;
      }
    }
  }

  public Pair peekI() {
    return ins[ip];
  }

  public LexicalClosure peekL() {
    return lcs[ip];
  }

  public ArcObject param(int index) {
    return currentParams[index];
  }

  public void pushParam(int index) {
    ++ap;
    try {
      args[ap] = currentParams[index];
    } catch (ArrayIndexOutOfBoundsException e) {
      newArgArray(args.length * 2);
      args[ap] = currentParams[index];
    }
  }

  public void pushA(ArcObject arg) {
    ++ap;
    try {
      args[ap] = arg;
    } catch (ArrayIndexOutOfBoundsException e) {
      newArgArray(args.length * 2);
      args[ap] = arg;
    }
  }

  public ArcObject peekA() {
    return args[ap];
  }

  public ArcObject popA() {
    return args[ap--];
  }

  public Pair popArgs(int argCount) {
    Pair result = ArcObject.EMPTY_LIST;
    for (int i = 0; i < argCount; i++) {
      ArcObject arg = args[ap - i];
      result = new Pair(arg, result);
    }
    ap -= argCount;
    return result;
  }

  private void showArgs() {
    int start = (ap > 5) ? (ap - 5) : 0;
    for (int i = start; i < ap + 1; i++) {
      System.out.println(i + ". " + args[i]);
    }
  }

  private void showInstructions() {
    int end = (ip > 4) ? (ip - 4) : 0;
    for (int i = ip; i >= end; i--) {
      showFrame(i);
    }
  }

  private void showFrame(int frame) {
    Pair instructions = ins[frame];
    LexicalClosure lc = lcs[frame];
    System.out.print("\nInstruction Frame " + frame + ":");
    while (!(instructions instanceof Nil)) {
      Instruction i = (Instruction) instructions.car();
      instructions = (Pair) instructions.cdr();
//      System.out.print(i.toString());
      System.out.print(i.toString(lc));
      System.out.print(" ");
    }
    System.out.println();
  }

  public LexicalClosure lc() {
    return currentLc;
  }

  public void clearError() {
    this.error = null;
  }

  public int ap() {
    return ap;
  }

  public void ap(int ap) {
    this.ap = ap;
  }

  public void setInterceptor(VMInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  public ArcObject type() {
    return TYPE;
  }

  public VM copy() {
    VM vm = new VM();
    copyTo(vm);
    return vm;
  }

  public void copyTo(VM vm) {
    vm.ap = this.ap;
    vm.ip = this.ip;
    vm.ins = this.ins.clone();
    vm.args = this.args.clone();
    vm.lcs = this.lcs.clone();
    vm.params = this.params.clone();
    vm.currentLc = this.currentLc;
    vm.error = this.error;
    vm.interceptor = this.interceptor;
    vm.dead = this.dead;
    vm.ipThreshold = this.ipThreshold;
  }

  public boolean dead() {
    return dead;
  }

  public String toString() {
    return "[thread#" + threadId + ": instruction stack " + ip + "; arg stack " + ap + "]";
  }

  private void newArgArray(int newLength) {
    ArcObject[] newArgs = new ArcObject[newLength];
    copy(args, newArgs);
    this.args = newArgs;
  }

  private void newClosureArray(int newLength) {
    LexicalClosure[] newL = new LexicalClosure[newLength];
    copy(lcs, newL);
    this.lcs = newL;
  }

  private void newInstructionArray(int newLength) {
    Pair[] newI = new Pair[newLength];
    copy(ins, newI);
    this.ins = newI;
  }

  private void newParamsArray(int newLength) {
    ArcObject[][] newP = new ArcObject[newLength][];
    copy(params, newP);
    this.params = newP;
  }

  private void copy(Object[] src, Object[] dest) {
    System.arraycopy(src, 0, dest, 0, src.length);
  }
}

