package tech.lacambra.fabric.injection.cdi;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ChaincodeRequestScope
public class MockStubProducer {

  @Inject
  private StubHolder stubHolder;

  @Produces
  public ChaincodeStubMock getStubHolder() {
    return stubHolder.getStubMock();
  }
}
