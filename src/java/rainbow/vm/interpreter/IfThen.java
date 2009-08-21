package rainbow.vm.interpreter;

import rainbow.types.ArcObject;
import rainbow.types.Symbol;
import rainbow.vm.instructions.cond.Cond;
import rainbow.vm.instructions.cond.Cond_Lex;
import rainbow.Nil;

import java.util.List;

public class IfThen extends ArcObject implements Conditional {
  public ArcObject ifExpression;
  public ArcObject thenExpression;
  public Conditional next;

  public ArcObject type() {
    return Symbol.mkSym("if-then-clause");
  }

  public void add(Conditional c) {
    if (next != null) {
      next.add(c);
    } else {
      next = c;
    }
  }

  public void take(ArcObject expression) {
    if (ifExpression == null) {
      ifExpression = expression;
    } else if (thenExpression == null) {
      thenExpression = expression;
    } else {
      next.take(expression);
    }
  }

  public void addInstructions(List i) {
    if (ifExpression instanceof BoundSymbol) {
      Cond_Lex.addInstructions(i, (BoundSymbol) ifExpression, thenExpression, next);
    } else {
      ifExpression.addInstructions(i);
      i.add(new Cond(thenExpression, (ArcObject) next));
    }
  }

  public Conditional reduce() {
    next = next.reduce();
    if (reduceToIfExpr()) {
      Else e = new Else();
      e.take(ifExpression);
      return e;
    } else {
      return this;
    }
  }

  private boolean reduceToIfExpr() {
    if (!(ifExpression instanceof BoundSymbol) || !(thenExpression instanceof BoundSymbol) || !(next instanceof Else)) {
      return false;
    } else {
      BoundSymbol b1 = (BoundSymbol) ifExpression;
      BoundSymbol b2 = (BoundSymbol) thenExpression;
      Else e = (Else) next;
      return b1.isSameBoundSymbol(b2) && (e.ifExpression instanceof Nil);
    }
  }

  public String toString() {
    return ifExpression + " " + thenExpression + " " + next;
  }
}
