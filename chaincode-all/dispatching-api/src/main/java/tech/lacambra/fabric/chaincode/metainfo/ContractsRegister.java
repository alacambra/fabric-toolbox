package tech.lacambra.fabric.chaincode.metainfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContractsRegister {

  private List<ContractMetaInfo> register;

  public ContractsRegister(List<ContractMetaInfo> register) {
    this.register = new ArrayList<>(register);
  }

  public Optional<ContractMetaInfo> getContract(String contractName) {
    return register.stream().filter(contractMetaInfo -> contractMetaInfo.getContractId().equalsIgnoreCase(contractName)).findFirst();
  }

  public ContractsRegister register(ContractMetaInfo contractMetaInfo) {
    if (!register.contains(contractMetaInfo)) {
      register.add(contractMetaInfo);
      return this;
    }

    throw new ContractRegistrationException("Contract info already exists: " + contractMetaInfo);
  }

  public List<ContractMetaInfo> getRegister() {
    return new ArrayList<>(register);
  }
}
