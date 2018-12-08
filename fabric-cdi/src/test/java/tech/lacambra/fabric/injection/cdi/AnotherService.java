package tech.lacambra.fabric.injection.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AnotherService {

  @Inject
  SomeService someService;

  public AnotherService() {
    System.out.println("Init AnotherService: " + hashCode());
  }

  public SomeService getSomeService() {
    return someService;
  }
}
