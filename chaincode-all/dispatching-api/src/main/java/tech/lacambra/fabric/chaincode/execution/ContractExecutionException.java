package tech.lacambra.fabric.chaincode.execution;

public class ContractExecutionException extends RuntimeException {

  public ContractExecutionException() {
  }

  public ContractExecutionException(String message) {
    super(message);
  }

  public ContractExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public ContractExecutionException(Throwable cause) {
    super(cause);
  }

  public ContractExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
