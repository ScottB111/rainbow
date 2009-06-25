package rainbow.vm.continuations;

import rainbow.LexicalClosure;
import rainbow.types.ArcObject;
import rainbow.types.Pair;
import rainbow.vm.ArcThread;
import rainbow.vm.Continuation;
import rainbow.vm.compiler.QuasiQuoteCompiler;
import rainbow.vm.compiler.Rebuilder;

import java.util.LinkedList;
import java.util.List;

public class QuasiQuoteContinuation extends ContinuationSupport {
  private ArcObject expression;
  private List<ArcObject> result = new LinkedList<ArcObject>();
  private ArcObject current;
  private int nesting;

  public QuasiQuoteContinuation(LexicalClosure lc, Continuation caller, ArcObject expression) {
    this(lc, caller, expression, 1);
  }

  private QuasiQuoteContinuation(LexicalClosure lc, Continuation caller, ArcObject expression, int nesting) {
    super(lc, caller);
    this.expression = expression;
    this.nesting = nesting;
    start();
  }

  public void start() {
    if (expression.isNotPair()) {
      caller.receive(expression);
    } else if (isUnQuote(expression)) {
      current = expression;
      expression.cdr().car().interpret(lc, caller);
    } else if (!isPair(expression)) {
      caller.receive(expression);
    } else {
      repeat();
    }
  }

  private void repeat() {
    if (expression.isNotPair()) {
      caller.receive(Pair.buildFrom(result, expression));
    } else {
      current = expression.car();
      expression = expression.cdr();
      if (isUnQuote(current)) {
        if (nesting == 1) {
          current.cdr().car().interpret(lc, this);
        } else {
          Rebuilder rb = new Rebuilder(this, QuasiQuoteCompiler.UNQUOTE);
          new QuasiQuoteContinuation(lc, rb, current.cdr().car(), nesting - 1);
        }
      } else if (isUnQuoteSplicing(current)) {
        if (nesting == 1) {
          current.cdr().car().interpret(lc, new UnquoteSplicer(this, result));
        } else {
          append(current);
          repeat();
        }
      } else if (isQuasiQuote(current)) {
        Rebuilder rb = new Rebuilder(this, QuasiQuoteCompiler.QUASIQUOTE);
        new QuasiQuoteContinuation(lc, rb, current.cdr().car(), nesting + 1);
      } else if (isPair(current)) {
        new QuasiQuoteContinuation(lc, this, current, nesting);
      } else {
        append(current);
        repeat();
      }
    }
  }

  private void append(ArcObject obj) {
    result.add(obj);
  }

  public static boolean isUnQuote(ArcObject expression) {
    return expression.isCar(QuasiQuoteCompiler.UNQUOTE);
  }

  public static boolean isUnQuoteSplicing(ArcObject expression) {
    return expression.isCar(QuasiQuoteCompiler.UNQUOTE_SPLICING);
  }

  public static boolean isQuasiQuote(ArcObject expression) {
    return expression.isCar(QuasiQuoteCompiler.QUASIQUOTE);
  }

  private static boolean isPair(ArcObject expression) {
    return expression instanceof Pair;
  }

  public void onReceive(ArcObject o) {
    if (o != null) {
      append(o);
    }
    repeat();
  }

  protected ArcObject getCurrentTarget() {
    return current;
  }

  public Continuation cloneFor(ArcThread thread) {
    QuasiQuoteContinuation e = (QuasiQuoteContinuation) super.cloneFor(thread);
    e.expression = this.expression.copy();
    e.result = new LinkedList(this.result);
    return e;
  }
}
