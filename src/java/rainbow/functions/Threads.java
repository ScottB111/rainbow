package rainbow.functions;

import rainbow.vm.ArcThread;
import rainbow.vm.Continuation;
import rainbow.vm.continuations.TopLevelContinuation;
import rainbow.vm.continuations.Atomic;
import rainbow.*;
import rainbow.types.Pair;
import rainbow.types.ArcNumber;
import rainbow.types.ArcObject;
import rainbow.types.Tagged;

public class Threads {
  public static void collect(Environment top) {
    top.add(new Builtin[]{
      new Builtin("new-thread") {
        public void invoke(ArcThread thread, final LexicalClosure lc, Continuation caller, final Pair args) {
          final ArcThread newThread = new ArcThread(thread.environment());
          new Thread() {
            public void run() {
              Function fn = Builtin.cast(args.car(), this);
              fn.invoke(newThread, lc, new TopLevelContinuation(newThread), NIL);
              newThread.run();
            }
          }.start();
          caller.receive(newThread);
        }
      }, new Builtin("kill-thread") {
        public ArcObject invoke(Pair args) {
          ArcThread.cast(args.car(), this).stop();
          return NIL;
        }
      }, new Builtin("sleep") {
        public ArcObject invoke(Pair args) {
          ArcNumber seconds = ArcNumber.cast(args.car(), this);
          try {
            Thread.sleep((long) (seconds.toDouble() * 1000));
          } catch (InterruptedException e) {
            throw new ArcError("sleep: thread interruped : " + e.getMessage(), e);
          }
          return NIL;
        }
      }, new Builtin("dead") {
        public void invoke(ArcThread thread, LexicalClosure lc, Continuation caller, Pair args) {
          ArcThread target = ArcThread.cast(args.car(), this);
          caller.receive(Truth.valueOf(target.isDead()));
        }
      }, new Builtin("atomic-invoke") {
        public void invoke(ArcThread thread, LexicalClosure lc, Continuation caller, Pair args) {
          Builtin.cast(args.car(), this).invoke(thread, lc, new Atomic(thread, lc, caller), NIL);
        }
      }, new Builtin("ccc") {
        public void invoke(ArcThread thread, LexicalClosure lc, Continuation caller, Pair args) {
          checkMaxArgCount(args, getClass(), 1);
          ContinuationWrapper e = new ContinuationWrapper(caller);
          Function toCall = (Function) args.car();
          toCall.invoke(thread, lc, caller, Pair.buildFrom(e));
        }
      }
    });
  }
  
  public static class ContinuationWrapper extends Builtin {
    private Continuation continuation;

    public ContinuationWrapper(Continuation continuation) {
      this.continuation = continuation.cloneFor(null);
    }

    public void invoke(ArcThread thread, LexicalClosure lc, Continuation deadContinuation, Pair args) {
      deadContinuation.stop();
      continuation.cloneFor(thread).receive(args.car());
    }
  }

  public static class Closure extends ArcObject implements Function {
    private Function expression;
    private LexicalClosure lc;

    public Closure(Function expression, LexicalClosure lc) {
      this.expression = expression;
      this.lc = lc;
    }

    public void invoke(ArcThread thread, LexicalClosure lc, Continuation caller, Pair args) {
      expression.invoke(thread, this.lc, caller, args);
    }

    public ArcObject type() {
      return Builtin.TYPE;
    }

    public String toString() {
      return expression.toString();
    }
  }
}
