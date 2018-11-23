package tech.lacambra.fabric.chaincode.metainfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FunctionMetaInfo {

  private String functionName;
  private Method method;
  private Class<?> returnType;
  private List<Class<?>> executionParams;

  public FunctionMetaInfo(String functionName, Method method, Class<?> returnType, List<Class<?>> executionParams) {
    this.functionName = functionName;
    this.method = method;
    this.returnType = returnType;
    this.executionParams = Collections.unmodifiableList(new ArrayList<>(executionParams));
  }

  public String getFunctionName() {
    return functionName;
  }

  public Method getMethod() {
    return method;
  }

  public Class<?> getReturnType() {
    return returnType;
  }

  public List<Class<?>> getExecutionParams() {
    return executionParams;
  }

  @Override
  public String toString() {
    return "FunctionMetaInfo{" +
        "functionName='" + functionName + '\'' +
        ", method=" + method +
        ", returnType=" + returnType +
        ", executionParams=" + executionParams +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FunctionMetaInfo that = (FunctionMetaInfo) o;
    return Objects.equals(functionName, that.functionName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(functionName);
  }
}
