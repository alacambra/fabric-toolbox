package tech.lacambra.fabric.injection.cdi;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Objects;

//@ApplicationScoped
public class AnotherService {

  @Inject
  SomeService someService;

  @Inject
  ChaincodeStubMock stubMock;

  @PostConstruct
  public void init() {
    Objects.requireNonNull(stubMock);
    System.out.println("Init AnotherService:" + hashCode() + ", stubMock:" + stubMock);
  }

  public AnotherService() {
    System.out.println("Init AnotherService: " + hashCode());
  }

  public SomeService getSomeService() {
    return someService;
  }
}
