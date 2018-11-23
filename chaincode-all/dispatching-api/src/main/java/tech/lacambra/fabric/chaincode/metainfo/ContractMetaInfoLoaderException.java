package tech.lacambra.fabric.chaincode.metainfo;

public class ContractMetaInfoLoaderException extends RuntimeException {

  public ContractMetaInfoLoaderException() {
  }

  public ContractMetaInfoLoaderException(String message) {
    super(message);
  }

  public ContractMetaInfoLoaderException(String message, Throwable cause) {
    super(message, cause);
  }

  public ContractMetaInfoLoaderException(Throwable cause) {
    super(cause);
  }

  public ContractMetaInfoLoaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
