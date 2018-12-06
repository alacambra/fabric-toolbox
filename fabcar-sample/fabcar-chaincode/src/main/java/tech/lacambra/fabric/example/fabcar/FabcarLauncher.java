package tech.lacambra.fabric.example.fabcar;

import tech.lacambra.fabric.chaincode.dispatch.RequestDispatcher;
import tech.lacambra.fabric.chaincode.execution.ContractExecutor;
import tech.lacambra.fabric.chaincode.metainfo.ContractMetaInfo;
import tech.lacambra.fabric.chaincode.metainfo.ContractsRegister;
import tech.lacambra.fabric.chaincode.metainfo.FunctionMetaInfo;
import tech.lacambra.fabric.chaincode.metainfo.FunctionMetaInfoLoader;
import tech.lacambra.fabric.client.messaging.DefaultMessageConverterProvider;
import tech.lacambra.fabric.client.messaging.MessageConverterProvider;

import java.util.ArrayList;
import java.util.List;

public class FabcarLauncher {

  private FunctionMetaInfoLoader functionMetaInfoLoader;
  private ContractsRegister register;
  private static RequestDispatcher requestDispatcher;

  public FabcarLauncher(FunctionMetaInfoLoader functionMetaInfoLoader, ContractsRegister register, String[] args) {
    this.functionMetaInfoLoader = functionMetaInfoLoader;
    this.register = register;

    registerContract(FabcarContract.class);
    MessageConverterProvider messageConverterProvider = new DefaultMessageConverterProvider().addConverter(new CarConverter());
    requestDispatcher = new RequestDispatcher(new ContractExecutor(), register, messageConverterProvider);
    requestDispatcher.start(args);
  }

  public void registerContract(Class contractType) {

    List<FunctionMetaInfo> functionMetaInfo = functionMetaInfoLoader.exploreContract(contractType);
    ContractMetaInfo contractMetaInfo = new ContractMetaInfo(contractType, functionMetaInfo);

    register.register(contractMetaInfo);

  }

  public static void main(String[] args) {
    new FabcarLauncher(new FunctionMetaInfoLoader(), new ContractsRegister(new ArrayList<>()), args);
  }

  public ContractsRegister getRegister() {
    return register;
  }

  public static RequestDispatcher getRequestDispatcher() {
    return requestDispatcher;
  }
}