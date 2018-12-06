package tech.lacambra.fabric.chaincode.metainfo;

import tech.lacambra.fabric.chaincode.execution.Contract;

import java.util.*;

public class ContractMetaInfo {

  private Class<?> contractClass;
  private String contractId;
  private Map<String, FunctionMetaInfo> functions;


  public ContractMetaInfo(Class<?> clazz, List<FunctionMetaInfo> functions) {

    this.contractClass = Objects.requireNonNull(clazz);
    this.contractId = loadContractId();

    Objects.requireNonNull(functions);

    this.functions = new HashMap<>();

    for (FunctionMetaInfo fcn : functions) {
      this.functions.put(fcn.getFunctionName(), fcn);
    }
  }

  private String loadContractId() {

    return Optional
        .ofNullable(contractClass.getAnnotation(Contract.class))
        .map(Contract::id)
        .orElseThrow(() -> new ContractMetaInfoLoaderException("No Id found for the contract " + contractClass.getName()));
  }


  public Class<?> getContractClass() {
    return contractClass;
  }

  public String getContractId() {
    return contractId;
  }

  public Map<String, FunctionMetaInfo> getFunctions() {
    return new LinkedHashMap<>(functions);
  }

  public FunctionMetaInfo getFunctionMetaInfo(String fnKey) {
    return functions.get(fnKey);
  }

  @Override
  public String toString() {
    return "ContractMetaInfo{" +
        "contractClass=" + contractClass +
        ", contractId='" + contractId + '\'' +
        ", functions=" + functions +
        '}';
  }
}
