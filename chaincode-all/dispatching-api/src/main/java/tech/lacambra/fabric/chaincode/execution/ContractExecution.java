package tech.lacambra.fabric.chaincode.execution;

public class ContractExecution {

  private boolean succeed;
  private Throwable throwable;
  private Object payload;

  public ContractExecution(Object payload) {
    this.payload = payload;
    succeed = true;
  }

  public ContractExecution(Throwable throwable) {
    succeed = false;
    this.throwable = throwable;
  }

  public boolean isSucceed() {
    return succeed;
  }

  public int getStatus() {
    return 1;
  }

  public Object getPayload() {
    return payload;
  }

  public Throwable getThrowable() {
    return throwable;
  }
}
