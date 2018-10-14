package tech.lacambra.fabric.client.stateless;

public class StatelessClientException extends RuntimeException {

  public StatelessClientException() {
  }

  public StatelessClientException(String message) {
    super(message);
  }

  public StatelessClientException(String message, Throwable cause) {
    super(message, cause);
  }

  public StatelessClientException(Throwable cause) {
    super(cause);
  }

  public StatelessClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
