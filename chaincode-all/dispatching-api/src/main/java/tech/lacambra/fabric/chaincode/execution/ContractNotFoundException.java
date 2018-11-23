package tech.lacambra.fabric.chaincode.execution;

public class ContractNotFoundException extends ContractExecutionException {

  public ContractNotFoundException() {
  }

  public ContractNotFoundException(String message) {
    super(message);
  }

  public ContractNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ContractNotFoundException(Throwable cause) {
    super(cause);
  }

  public ContractNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
