package tech.lacambra.fabric.client.chaincode.managment;

public class ChaincodeDeploymentException extends RuntimeException{

  public ChaincodeDeploymentException() {
    super();
  }

  public ChaincodeDeploymentException(String message) {
    super(message);
  }

  public ChaincodeDeploymentException(String message, Throwable cause) {
    super(message, cause);
  }

  public ChaincodeDeploymentException(Throwable cause) {
    super(cause);
  }

  protected ChaincodeDeploymentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
