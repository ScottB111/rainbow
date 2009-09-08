package rainbow.vm.interpreter;

import rainbow.types.ArcObject;
import rainbow.types.Symbol;
import rainbow.vm.interpreter.visitor.Visitor;

import java.util.List;
import java.util.Map;

public class IfClause extends ArcObject {
  private static final ArcObject TYPE = Symbol.mkSym("if-clause");
  private Conditional first;

  public ArcObject type() {
    return TYPE;
  }

  public void add(Conditional c) {
    if (first != null) {
      first.add(c);
    } else {
      first = c;
    }
  }

  public ArcObject reduce() {
    first = (Conditional) first.reduce();
    if (first instanceof Else) {
      return ((Else)first).ifExpression;
    } else {
      return this;
    }
  }

  public void take(ArcObject expression) {
    first.take(expression);
  }

  public void addInstructions(List i) {
    first.addInstructions(i);
  }

  public String toString() {
    return "(if " + first + ")";
  }

  public int countReferences(int refs, BoundSymbol p) {
    return first.countReferences(refs, p);
  }

  public int highestLexicalScopeReference() {
    return first.highestLexicalScopeReference();
  }

  public boolean assigns(int nesting) {
    return first.assigns(nesting);
  }

  public boolean hasClosures() {
    return first.hasClosures();
  }

  public ArcObject inline(BoundSymbol p, ArcObject arg, boolean unnest, int nesting, int paramIndex) {
    IfClause ic = new IfClause();
    ic.first = (Conditional) this.first.inline(p, arg, unnest, nesting, paramIndex).reduce();
    return ic;
  }

  public ArcObject inline(StackSymbol p, ArcObject arg, int paramIndex) {
    IfClause ic = new IfClause();
    ic.first = (Conditional) this.first.inline(p, arg, paramIndex).reduce();
    return ic;
  }

  public ArcObject nest(int threshold) {
    IfClause ic = new IfClause();
    ic.first = (Conditional) this.first.nest(threshold);
    return ic;
  }

  public ArcObject replaceBoundSymbols(Map<Symbol, Integer> lexicalBindings) {
    IfClause ic = new IfClause();
    ic.first = (Conditional) this.first.replaceBoundSymbols(lexicalBindings);
    return ic;
  }

  public void visit(Visitor v) {
    v.accept(this);
    first.visit(v);
    v.end(this);
  }
}
