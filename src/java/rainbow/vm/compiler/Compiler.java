package rainbow.vm.compiler;

import rainbow.functions.Evaluation;
import rainbow.types.ArcObject;
import rainbow.types.Pair;
import rainbow.types.Symbol;
import rainbow.types.Tagged;
import rainbow.vm.VM;
import rainbow.vm.interpreter.BoundSymbol;
import rainbow.vm.interpreter.Invocation;
import rainbow.vm.interpreter.QuasiQuotation;
import rainbow.vm.interpreter.Quotation;
import rainbow.Nil;

import java.util.Map;

public class Compiler {

  public static ArcObject compile(VM vm, ArcObject expression, Map[] lexicalBindings) {
    if (expression instanceof Nil) {
      return expression;
    } else if (Evaluation.isSpecialSyntax(expression)) {
      return compile(vm, Evaluation.ssExpand(expression), lexicalBindings);
    } else if (expression instanceof Pair) {
      return compilePair(vm, (Pair) expression, lexicalBindings);
    } else if (expression instanceof Symbol) {
      for (int i = 0; i < lexicalBindings.length; i++) {
        if (lexicalBindings[i].containsKey(expression)) {
          return new BoundSymbol((Symbol)expression, i, (Integer)lexicalBindings[i].get(expression));
        }
      }
      return expression;
    } else {
      return expression;
    }
  }

  protected static ArcObject receive(VM vm, Pair expression, ArcObject expanded, Map[] lexicalBindings) {
    if (expanded.isNotPair()) {
      return compile(vm, expanded, lexicalBindings);
    } else if (expression.equals(expanded)) {
      return new Invocation((Pair) expanded);
    } else {
      return receive(vm, (Pair) expanded, compile(vm, expanded, lexicalBindings), lexicalBindings);
    }
  }

  public static ArcObject compilePair(VM vm, Pair expression, Map[] lexicalBindings) {
    ArcObject f = getMacro(expression);
    if (f != null) {
      ArcObject expanded = f.invokeAndWait(vm, (Pair) expression.cdr());
      return receive(vm, expression, expanded, lexicalBindings);
    } else {
      ArcObject fun = expression.car();
      if (Symbol.is("quote", fun)) {
        return new Quotation(expression.cdr().car());
      } else if (fun == QuasiQuoteCompiler.QUASIQUOTE) {
        return new QuasiQuotation(QuasiQuoteCompiler.compile(vm, expression.cdr().car(), lexicalBindings, 1));
      } else if (Symbol.is("fn", fun)) {
        return FunctionBodyBuilder.build(vm, (Pair) expression.cdr(), lexicalBindings);
      } else if (Symbol.is("if", fun)) {
        return IfBuilder.build(vm, expression.cdr(), lexicalBindings);
      } else if (Symbol.is("assign", fun)) {
        return AssignmentBuilder.build(vm, expression.cdr(), lexicalBindings);
      } else if (Symbol.is("compose", fun.xcar())) {
        return compile(vm, decompose((Pair) fun.cdr(), (Pair) expression.cdr()), lexicalBindings);
      } else if (Symbol.is("complement", fun.xcar())) {
        return compile(vm, decomplement(fun.cdr().car(), (Pair) expression.cdr()), lexicalBindings);
      } else if (Evaluation.isSpecialSyntax(fun)) {
        return compile(vm, new Pair(Evaluation.ssExpand(fun), expression.cdr()), lexicalBindings);
      } else {
        ArcObject expanded = PairExpander.expand(vm, expression, lexicalBindings); // todo I've no idea if this else clause is even used
        ArcObject macexed = MacExpander.expand(vm, expanded);
        return receive(vm, expression, macexed, lexicalBindings);
      }
    }
  }

  private static ArcObject decompose(Pair fns, Pair args) {
    return new Pair(fns.car(), (fns.cdr() instanceof Nil) ? args : new Pair(decompose((Pair) fns.cdr(), args), ArcObject.NIL));
  }

  private static ArcObject decomplement(ArcObject not, Pair args) {
    return new Pair(Symbol.mkSym("no"), new Pair(new Pair(not, args), ArcObject.NIL));
  }

  private static ArcObject getMacro(Pair maybeMacCall) {
    ArcObject first = maybeMacCall.car();
    if (!(first instanceof Symbol)) {
      return null;
    }

    Symbol sym = (Symbol) first;
    if (!sym.bound()) {
      return null;
    }

    return Tagged.ifTagged(sym.value(), "mac");
  }

}
