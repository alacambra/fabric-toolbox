package tech.lacambra.fabric.chaincode.metainfo;

public class ContractRegistrationException extends RuntimeException {
  public ContractRegistrationException() {
  }

  public ContractRegistrationException(String message) {
    super(message);
  }

  public ContractRegistrationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ContractRegistrationException(Throwable cause) {
    super(cause);
  }

  public ContractRegistrationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
