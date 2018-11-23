package tech.lacambra.fabric.client.messaging;

public class SomeClass {

  private int value;
  private String anotherValue;

  public SomeClass(int value, String anotherValue) {
    this.value = value;
    this.anotherValue = anotherValue;
  }

  public int getValue() {
    return value;
  }

  public String getAnotherValue() {
    return anotherValue;
  }

  @Override
  public String toString() {
    return "SomeClass{" +
        "value=" + value +
        ", anotherValue='" + anotherValue + '\'' +
        '}';
  }
}
