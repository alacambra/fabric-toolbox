package tech.lacambra.fabric.chaincode;

import tech.lacambra.fabric.chaincode.metainfo.ContractMetaInfo;
import tech.lacambra.fabric.chaincode.metainfo.ContractsRegister;
import tech.lacambra.fabric.chaincode.metainfo.FunctionMetaInfo;
import tech.lacambra.fabric.chaincode.metainfo.FunctionMetaInfoLoader;

import java.util.List;

public class ChaincodeBootstrap {


  private FunctionMetaInfoLoader functionMetaInfoLoader;
  private ContractsRegister register;


  public ChaincodeBootstrap registerContract(Class contractType) {

    List<FunctionMetaInfo> functionMetaInfo = functionMetaInfoLoader.exploreContract(contractType);
    ContractMetaInfo contractMetaInfo = new ContractMetaInfo(contractType, functionMetaInfo);

    register.register(contractMetaInfo);
    return this;

  }

}
