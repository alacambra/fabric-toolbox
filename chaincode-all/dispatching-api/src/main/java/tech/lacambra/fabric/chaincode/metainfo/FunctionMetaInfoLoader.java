package tech.lacambra.fabric.chaincode.metainfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tech.lacambra.fabric.chaincode.execution.ContractFunction;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FunctionMetaInfoLoader {

  private static final Log LOGGER = LogFactory.getLog(FunctionMetaInfo.class);

  public List<FunctionMetaInfo> exploreContract(Class clazz) {

    validateNonArgsConstructor(clazz);

    Map<String, FunctionMetaInfo> validMethods = new HashMap<>();

    Method[] methods = clazz.getMethods();

    List<FunctionMetaInfo> functionMetaInfos = Stream.of(methods)
        .filter(method -> method.isAnnotationPresent(ContractFunction.class))
        .map(this::extractFunctionMetaInfo)
        .collect(Collectors.toList());

    if (functionMetaInfos.isEmpty()) {
      LOGGER.warn("[exploreContract] no functions found for contract " + clazz.getName());
    }

    validateFunctions(functionMetaInfos);

    LOGGER.info("[exploreContract] found Contract functions=" + validMethods.values().stream().map(FunctionMetaInfo::toString).collect(Collectors.joining(", ")));

    return functionMetaInfos;
  }

  private List<FunctionMetaInfo> validateFunctions(List<FunctionMetaInfo> functionMetaInfos) {
    Set<FunctionMetaInfo> infos = new HashSet<>();
    for (FunctionMetaInfo info : functionMetaInfos) {
      if (infos.contains(info)) {
        throw new ContractMetaInfoLoaderException(String.format("Repeated contract function found: %s.", info.getFunctionName()));
      }
      infos.add(info);
    }

    return functionMetaInfos;
  }

  private FunctionMetaInfo extractFunctionMetaInfo(Method method) {

    String defaultName = method.getName();
    String givenName = getContractFunction(method).value();
    String functionName = givenName.isEmpty() ? defaultName : givenName;
    List<Class<?>> params = Arrays.asList(method.getParameterTypes());
    Class<?> returnType = method.getReturnType();

    return new FunctionMetaInfo(functionName, method, returnType, params);
  }

  private ContractFunction getContractFunction(Method method) {
    return method.getAnnotation(ContractFunction.class);
  }

  private void validateNonArgsConstructor(Class clazz) {
    try {
      clazz.getConstructor();
    } catch (NoSuchMethodException e) {
      throw new ContractMetaInfoLoaderException("Non-Args constructor is needed for a Contract");
    }
  }
}