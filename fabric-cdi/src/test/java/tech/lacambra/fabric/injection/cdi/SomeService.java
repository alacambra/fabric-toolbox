package tech.lacambra.fabric.injection.cdi;

import java.util.Objects;
import java.util.Random;

@ChaincodeRequestScope
public class SomeService {

  final int id;

  public SomeService() {
    id = new Random().nextInt();
    System.out.println("Init SomeService:" + hashCode() + ", id:" + id);
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