package tech.lacambra.fabric.client.messaging;

public class MessageIOException extends RuntimeException {

  public MessageIOException() {
  }

  public MessageIOException(String message) {
    super(message);
  }

  public MessageIOException(String message, Throwable cause) {
    super(message, cause);
  }

  public MessageIOException(Throwable cause) {
    super(cause);
  }

  public MessageIOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
