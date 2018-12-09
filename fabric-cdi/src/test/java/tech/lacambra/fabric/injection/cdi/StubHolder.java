package tech.lacambra.fabric.injection.cdi;

@ChaincodeRequestScope
public class StubHolder {

  private ChaincodeStubMock stubMock;

  public ChaincodeStubMock getStubMock() {
    return stubMock;
  }

  public void setStubMock(ChaincodeStubMock stubMock) {
    this.stubMock = stubMock;
  }

  @Override
  public String toString() {
    return "StubHolder{" +
        "stubMock=" + stubMock +
        '}';
  }
}
