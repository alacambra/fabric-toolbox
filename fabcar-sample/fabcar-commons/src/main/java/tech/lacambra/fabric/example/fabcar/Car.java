package tech.lacambra.fabric.example.fabcar;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

public class Car {

  private String make;
  private String model;
  private String color;
  private String owner;

  public Car(String make, String model, String color, String owner) {
    this.make = make;
    this.model = model;
    this.color = color;
    this.owner = owner;
  }

  public Car(JsonObject jsonObject) {
    this.make = jsonObject.getString("make", "");
    this.model = jsonObject.getString("model", "");
    this.color = jsonObject.getString("color", "");
    this.owner = jsonObject.getString("owner", "");
  }

  public Car(byte[] bytes) {
    try (ByteArrayInputStream baos = new ByteArrayInputStream(bytes)) {
      JsonObject jsonObject = Json.createReader(baos).readObject();
      this.make = jsonObject.getString("make", "");
      this.model = jsonObject.getString("model", "");
      this.color = jsonObject.getString("color", "");
      this.owner = jsonObject.getString("owner", "");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String getMake() {
    return make;
  }

  public String getModel() {
    return model;
  }

  public String getColor() {
    return color;
  }

  public String getOwner() {
    return owner;
  }

  public void setMake(String make) {
    this.make = make;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public JsonObject toJson() {
    return Json.createObjectBuilder()
        .add("make", make)
        .add("model", model)
        .add("color", color)
        .add("owner", owner)
        .build();
  }

  public static Car fromJson(String carAsString) {
    return new Car(Json.createReader(new StringReader(carAsString)).readObject());
  }
}
