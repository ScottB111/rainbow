package rainbow.functions.interpreted;

import rainbow.ArcError;
import rainbow.LexicalClosure;
import rainbow.Nil;
import rainbow.functions.Builtin;
import rainbow.types.ArcObject;
import rainbow.types.Pair;
import rainbow.types.Symbol;
import rainbow.vm.VM;
import rainbow.vm.compiler.FunctionBodyBuilder;
import rainbow.vm.instructions.Close;
import rainbow.vm.instructions.Literal;
import rainbow.vm.instructions.PopArg;
import rainbow.vm.interpreter.BoundSymbol;

import java.util.*;

public abstract class InterpretedFunction extends ArcObject {
  protected final ArcObject parameterList;
  protected final Map lexicalBindings;
  final ArcObject[] body;
  protected final Pair instructions;

  protected InterpretedFunction(ArcObject parameterList, Map lexicalBindings, Pair body) {
    this.parameterList = parameterList;
    this.lexicalBindings = lexicalBindings;
    this.body = body.toArray();
    List i = new ArrayList();
    if (this.body.length > 0) {
      Pair b = body;
      while (!(b instanceof Nil)) {
        b.car().addInstructions(i);
        b = (Pair) b.cdr();
        if (!(b instanceof Nil)) {
          i.add(new PopArg("intermediate-fn-expression"));
        }
      }
    } else {
      i.add(new Literal(NIL));
    }
    instructions = Pair.buildFrom(i);
  }

  public void invokef(VM vm) {
    invokeN(vm, null);
  }

  public void invokeN(VM vm, LexicalClosure lc) {
    invoke(vm, lc, NIL);
  }

  public void invokef(VM vm, ArcObject arg) {
    invokeN(vm, null, arg);
  }

  public void invokeN(VM vm, LexicalClosure lc, ArcObject arg) {
    invoke(vm, lc, new Pair(arg, NIL));
  }

  public void invokef(VM vm, ArcObject arg1, ArcObject arg2) {
    invokeN(vm, null, arg1, arg2);
  }

  public void invokeN(VM vm, LexicalClosure lc, ArcObject arg1, ArcObject arg2) {
    invoke(vm, lc, new Pair(arg1, new Pair(arg2, NIL)));
  }

  public void invoke(VM vm, Pair args) {
    invoke(vm, null, args);
  }

  public abstract void invoke(VM vm, LexicalClosure lc, Pair args);

  public Pair instructions() {
    return instructions;
  }

  public void addInstructions(List i) {
    if (requiresClosure()) {
      i.add(new Close(this));
    } else {
      i.add(new Literal(this));
    }
  }

  private boolean requiresClosure() {
    boolean b = highestLexicalScopeReference() > -1;
    return b;
  }

  public int highestLexicalScopeReference() {
    int highest = Integer.MIN_VALUE;
    for (ArcObject expr : body) {
      int eh = expr.highestLexicalScopeReference();
      if (eh > highest) {
        highest = eh;
      }
    }

    highest = FunctionBodyBuilder.highestLexScopeReference(highest, parameterList, false);

    if (parameterList() instanceof Nil) {
      return highest;
    } else {
      return highest - 1;
    }
  }

  public boolean isIdFn() {
    if (parameterList.len() == 1) {
      if (parameterList.car() instanceof Symbol) {
        if (body.length == 1) {
          if (body[0] instanceof BoundSymbol) {
            Symbol p1 = (Symbol) parameterList.car();
            BoundSymbol rv = (BoundSymbol) body[0];
            BoundSymbol equiv = new BoundSymbol(p1, 0, 0);
            return rv.isSameBoundSymbol(equiv);
          }
        }

      }
    }
    return false;
  }

  public int compareTo(ArcObject right) {
    throw new ArcError("Can't compare " + this + " to " + right);
  }

  public String toString() {
    List<ArcObject> fn = new LinkedList<ArcObject>();
    fn.add(Symbol.mkSym("fn"));
    fn.add(parameterList());
    fn.addAll(Arrays.asList(body));
    return Pair.buildFrom(fn, NIL).toString();
  }

  public ArcObject parameterList() {
    return parameterList;
  }

  public ArcObject type() {
    return Builtin.TYPE;
  }

  public ArcObject nth(int index) {
    return body[index];
  }

  public ArcObject last() {
    return body[body.length - 1];
  }

  public int length() {
    return body.length;
  }

  protected void requireNil(ArcObject test, ArcObject info) {
    try {
      test.cdr().mustBeNil();
    } catch (NotNil notNil) {
      throwArgMismatchError(info);
    }
  }

  protected void requireNotNil(Pair destructured, ArcObject arg) {
    if (destructured instanceof Nil) {
      throwArgMismatchError(arg);
    }
  }

  protected void throwArgMismatchError(ArcObject args) {
    throw new ArcError("args " + args + " doesn't match signature for " + this);
  }

}
