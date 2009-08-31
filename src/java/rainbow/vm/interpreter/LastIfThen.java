package rainbow.vm.interpreter;

import rainbow.ArcError;
import rainbow.Nil;
import rainbow.functions.interpreted.InterpretedFunction;
import rainbow.types.ArcObject;
import rainbow.types.Symbol;
import rainbow.vm.instructions.cond.LastCond;

import java.util.List;

public class LastIfThen extends ArcObject implements Conditional {
  public ArcObject ifExpression;
  public ArcObject thenExpression;

  public ArcObject type() {
    return Symbol.mkSym("last-if-then-clause");
  }

  public void add(Conditional c) {
    throw new ArcError("Internal error: if clause: unexpected extra condition: " + c);
  }

  public void take(ArcObject expression) {
    if (ifExpression == null) {
      ifExpression = expression;
    } else if (thenExpression == null) {
      thenExpression = expression;
    } else {
      throw new ArcError("Internal error: if clause: unexpected: " + expression);
    }
  }

  public void addInstructions(List i) {
    ifExpression.addInstructions(i);
    i.add(new LastCond(thenExpression));
  }

  public ArcObject reduce() {
    if (ifExpression instanceof Nil) {
      Else e = new Else();
      e.take(NIL);
      return e;
    } else {
      return this;
    }
  }

  public String toString() {
    return ifExpression + " " + thenExpression;
  }

  public int countReferences(int refs, BoundSymbol p) {
    refs = ifExpression.countReferences(refs, p);
    return thenExpression.countReferences(refs, p);
  }

  public int highestLexicalScopeReference() {
    int hif = ifExpression.highestLexicalScopeReference();
    int hthen = thenExpression.highestLexicalScopeReference();
    return Math.max(hif, hthen);
  }

  public boolean assigns(int nesting) {
    return ifExpression.assigns(nesting) || thenExpression.assigns(nesting);
  }

  public boolean hasClosures() {
    if (ifExpression instanceof InterpretedFunction) {
      if (((InterpretedFunction) ifExpression).requiresClosure()) {
        return true;
      }
    }
    if (thenExpression instanceof InterpretedFunction) {
      if (((InterpretedFunction) thenExpression).requiresClosure()) {
        return true;
      }
    }
    return ifExpression.hasClosures() || thenExpression.hasClosures();
  }

  public ArcObject inline(BoundSymbol p, ArcObject arg, boolean unnest, int nesting, int paramIndex) {
    LastIfThen other = new LastIfThen();
    other.ifExpression = this.ifExpression.inline(p, arg, unnest, nesting, paramIndex);
    other.thenExpression = this.thenExpression.inline(p, arg, unnest, nesting, paramIndex);
    return other;
  }

  public ArcObject nest(int threshold) {
    LastIfThen other = new LastIfThen();
    other.ifExpression = this.ifExpression.nest(threshold);
    other.thenExpression = this.thenExpression.nest(threshold);
    return other;
  }
}
