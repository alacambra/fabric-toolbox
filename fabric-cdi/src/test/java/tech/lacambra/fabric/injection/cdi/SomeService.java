package tech.lacambra.fabric.injection.cdi;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Objects;
import java.util.Random;

@ChaincodeRequestScope
public class SomeService {

  @Inject
  ChaincodeStubMock stubMock;

  final int id;

  @PostConstruct
  public void init() {
    Objects.requireNonNull(stubMock);
    System.out.println("Init SomeService:" + hashCode() + ", id:" + id + "m stubMock:" + stubMock);
  }

  public SomeService() {
    id = new Random().nextInt();
  }

  public void sayHello() {
    System.out.println("hellooooo:" + this + " - " + hashCode());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SomeService that = (SomeService) o;
    return id == that.id;
  }

  public int getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}