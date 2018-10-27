package tech.lacambra.fabric.client;

import tech.lacambra.fabric.client.stateless.StatelessClientException;

public class FabricClientException extends StatelessClientException {

  public FabricClientException() {
  }

  public FabricClientException(String message) {
    super(message);
  }

  public FabricClientException(String message, Throwable cause) {
    super(message, cause);
  }

  public FabricClientException(Throwable cause) {
    super(cause);
  }

  public FabricClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
