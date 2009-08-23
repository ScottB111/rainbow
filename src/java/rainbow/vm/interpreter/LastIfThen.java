package rainbow.vm.interpreter;

import rainbow.ArcError;
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
    return this;
  }

  public String toString() {
    return ifExpression + " " + thenExpression;
  }

  public int highestLexicalScopeReference() {
    int hif = ifExpression.highestLexicalScopeReference();
    int hthen = thenExpression.highestLexicalScopeReference();
    return Math.max(hif, hthen);
  }
}
