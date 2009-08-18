package rainbow.vm.compiler;

import rainbow.functions.InterpretedFunction;
import rainbow.types.ArcObject;
import static rainbow.types.ArcObject.NIL;
import rainbow.types.Pair;
import rainbow.vm.VM;

import java.util.HashMap;
import java.util.Map;

public class FunctionBodyBuilder {

  public static ArcObject build(VM vm, Pair args, Map[] lexicalBindings) {
    if (lexicalBindings == null) {
      throw new IllegalArgumentException("can't have null lexical bindings!");
    }
    Map myParams = new HashMap();
    ArcObject parameters = args.car();
    ArcObject complexParams;
    ArcObject parameterList;
    if (parameters.isNil()) {
      complexParams = NIL;
      parameterList = NIL;
    } else {
      lexicalBindings = concat(myParams, lexicalBindings);
      ArcObject fpl = FunctionParameterListBuilder.build(vm, parameters, lexicalBindings);
      complexParams = fpl.car();
      parameterList = fpl.cdr();
    }

    Pair body = (Pair) args.cdr();
    Pair expandedBody = PairExpander.expand(vm, body, lexicalBindings);
    return buildFunctionBody(parameterList, myParams, expandedBody, complexParams);
  }

  private static Map[] concat(Map map, Map[] lexicalBindings) {
    Map[] result = new Map[lexicalBindings.length + 1];
    result[0] = map;
    System.arraycopy(lexicalBindings, 0, result, 1, lexicalBindings.length);
    return result;
  }

  private static ArcObject buildFunctionBody(ArcObject parameterList, Map lexicalBindings, Pair expandedBody, ArcObject complexParams) {
    if (parameterList.isNil()) {
      return new InterpretedFunction.ZeroArgs(lexicalBindings, expandedBody);
    } else if (!complexParams.isNil()) {
      return new InterpretedFunction.ComplexArgs(parameterList, lexicalBindings, expandedBody);
    } else {
      return new InterpretedFunction.SimpleArgs(parameterList, lexicalBindings, expandedBody);
    }
  }
}
