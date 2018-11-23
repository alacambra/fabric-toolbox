package tech.lacambra.fabric.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.json.*;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class JsonUtils {

  private static final Log logger = LogFactory.getLog(JsonUtils.class);
  private static final NilJsonObjectBuilder NIL_JSON_OBJECT_BUILDER;
  private static Charset charset;


  static {
    NIL_JSON_OBJECT_BUILDER = new NilJsonObjectBuilder();
    charset = Charset.forName("UTF-8");
  }

  private JsonUtils() {

  }

  public static JsonArray serializeToJsonArray(Collection<String> entities) {
    return entities.stream().collect(Json::createArrayBuilder, JsonArrayBuilder::add, JsonArrayBuilder::add).build();
  }

  public static byte[] toBytes(JsonObject jsonObject) {
    if (jsonObject == null) {
      jsonObject = JsonValue.EMPTY_JSON_OBJECT;
    }

    return jsonObject.toString().getBytes(charset);
  }

  public static Optional<JsonObject> fromBytes(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      logger.warn("[fromBytes] Not enough bytes received");
      return Optional.empty();
    }

    try (JsonReader reader = Json.createReader(new ByteArrayInputStream(bytes))) {
      return Optional.of(reader.readObject());
    } catch (JsonException e) {
      String content = new String(bytes, charset);
      logger.warn("[fromBytes] not possible to serialize bytes to Json. Received bytes as String= " + content);
      return Optional.empty();
    }
  }

  public static Optional<JsonObject> stringToJsonObject(String input) {
    return fromBytes(input.getBytes());
  }

  public static void setCharset(Charset charset) {
    JsonUtils.charset = Objects.requireNonNull(charset);
  }

  public static boolean isValidJsonValue(Object value) {
    return addValueToObjectBuilder(NIL_JSON_OBJECT_BUILDER, "NIL", value);
  }

//  public static <T> T toObject(JsonObject jsonObject, Class<T> clazz) {
//    return OBJECT_SERIALIZATION.toObject(jsonObject, clazz);
//  }
//
//  public static JsonObject toJsonObject(Object o) {
//    return OBJECT_SERIALIZATION.toJsonObject(o);
//  }

  public static JsonObjectBuilder toBuilder(JsonObject source) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    source.forEach(builder::add);
    return builder;
  }

  public static boolean addValueToObjectBuilder(JsonObjectBuilder builder, String key, Object value) {

    if (value == null) {
      builder.addNull(key);
      return true;
    } else if (value instanceof Integer) {
      builder.add(key, (Integer) value);
      return true;
    } else if (value instanceof Long) {
      builder.add(key, (Long) value);
      return true;
    } else if (value instanceof Float) {
      builder.add(key, (Float) value);
      return true;
    } else if (value instanceof Double) {
      builder.add(key, (Double) value);
      return true;
    } else if (value instanceof Boolean) {
      builder.add(key, (Boolean) value);
      return true;
    } else if (value instanceof String) {
      builder.add(key, (String) value);
      return true;
    } else if (value instanceof JsonValue) {
      builder.add(key, (JsonValue) value);
      return true;
    } else if (value instanceof BigInteger) {
      builder.add(key, (BigInteger) value);
      return true;
    } else if (value instanceof BigDecimal) {
      builder.add(key, (BigDecimal) value);
      return true;
    } else if (value instanceof JsonObjectBuilder) {
      builder.add(key, (JsonObjectBuilder) value);
      return true;
    } else if (value instanceof JsonArrayBuilder) {
      builder.add(key, (JsonArrayBuilder) value);
      return true;
    } else if (int.class.equals(value.getClass())) {
      builder.add(key, (int) value);
      return true;
    } else if (long.class.equals(value.getClass())) {
      builder.add(key, (long) value);
      return true;
    } else if (double.class.equals(value.getClass())) {
      builder.add(key, (double) value);
      return true;
    } else if (boolean.class.equals(value.getClass())) {
      builder.add(key, (boolean) value);
      return true;
    }

    return false;
  }

  public static <T> boolean jsonValueCanBeConverted(JsonValue jsonValue, Class<T> expectedType) {

    if (expectedType.equals(String.class)) {
      return true;
    }

    switch (jsonValue.getValueType()) {
      case ARRAY:
        return expectedType.isAssignableFrom(JsonArray.class);
      case OBJECT:
        return expectedType.isAssignableFrom(JsonObject.class);
      case STRING:
        String v = ((JsonString) jsonValue).getString();

        if (expectedType.isAssignableFrom(Long.class) || long.class.equals(expectedType)) {
          return actionProducesException(Long::parseLong, v);
        }

        if (expectedType.isAssignableFrom(Integer.class) || int.class.equals(expectedType)) {
          return actionProducesException(Integer::parseInt, v);
        }

        return true;
      case NUMBER:
        return expectedType.isAssignableFrom(Number.class);
      case TRUE:
      case FALSE:
        return expectedType.isAssignableFrom(Boolean.class);
      case NULL:
        return !expectedType.isPrimitive();
    }

    return false;
  }

  private static boolean actionProducesException(Function<String, ?> fn, String v) {
    try {
      fn.apply(v);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static <T> T convertJsonValueToJavaValue(JsonValue jsonValue, Class<T> expectedType) {

    Object o;

    if (expectedType.equals(String.class)
        && !jsonValue.getValueType().equals(JsonValue.ValueType.STRING)) {
      return (T) jsonValue.toString();
    }

    switch (jsonValue.getValueType()) {
      case ARRAY:
        o = jsonValue;
        break;
      case OBJECT:
        o = jsonValue;
        break;
      case STRING:
        String v = ((JsonString) jsonValue).getString();
        if (expectedType.isAssignableFrom(Long.class) || long.class.equals(expectedType)) {
          o = Long.parseLong(v);
        } else if (expectedType.isAssignableFrom(Integer.class) || int.class.equals(expectedType)) {
          o = Integer.parseInt(v);
        } else {
          o = v;
        }
        break;
      case NUMBER:
        if (expectedType.equals(Integer.class)) {
          o = ((JsonNumber) jsonValue).intValue();
        } else if (expectedType.equals(Long.class)) {
          o = ((JsonNumber) jsonValue).longValue();
        } else {
          o = ((JsonNumber) jsonValue).doubleValue();
        }
        break;
      case TRUE:
        o = Boolean.TRUE;
        break;
      case FALSE:
        o = Boolean.FALSE;
        break;
      case NULL:
        o = null;
        break;
      default:
        o = null;
    }

    return (T) o;
  }

  public static Object convertJsonValueToJavaValue(JsonValue jsonValue) {

    Object o;

    switch (jsonValue.getValueType()) {
      case ARRAY:
        o = jsonValue;
        break;
      case OBJECT:
        o = jsonValue;
        break;
      case STRING:
        o = ((JsonString) jsonValue).getString();
        break;
      case NUMBER:
        o = ((JsonNumber) jsonValue).numberValue();
        break;
      case TRUE:
        o = Boolean.TRUE;
        break;
      case FALSE:
        o = Boolean.FALSE;
        break;
      case NULL:
        o = null;
        break;
      default:
        o = null;
    }

    return o;
  }

  public static List<String> deserializeToArray(JsonArray jsonArray) {
    List<String> strings = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      strings.add(jsonArray.getString(i));
    }
    return strings;
  }

  public static JsonArray serializeToJsonArrayWithJsonObjects(Collection<JsonObject> entities) {
    return entities.stream().collect(Json::createArrayBuilder, JsonArrayBuilder::add, JsonArrayBuilder::add).build();
  }

  public static <T> void serializeValueIfPresent(BiConsumer<String, T> builder, String key, T object) {
    if (object != null) {
      try {
        builder.accept(key, object);
      } catch (NullPointerException e) {
        //Only there in case applying transformation when passing the object, than can try to access the object
        // before to check for the null value
      }
    }
  }

  public static <T, R> void serializeValueIfPresent(BiConsumer<String, R> builder, Function<T, R> converter, String key, T object) {
    if (object != null) {
      try {
        R convertedObject = converter.apply(object);
        builder.accept(key, convertedObject);
      } catch (NullPointerException e) {
        //Only there in case applying transformation when passing the object, than can try to access the object
        // before to check for the null value
      }
    }
  }

  /**
   * @param builder:      the json value bilder
   * @param key:          assigned key to the element to be added
   * @param object:       object to be added
   * @param defaultValue: value to be used in case the object given is null
   * @param <T>:          type of the object to be added
   */
  public static <T> void serializeValue(BiConsumer<String, T> builder, String key, T object, T defaultValue) {
    Objects.requireNonNull(defaultValue);
    if (object == null) {
      object = defaultValue;
    }
    builder.accept(key, object);
  }

  /**
   * @param builder:      JsonBuilder where the value must be added (e.g. jsonBuilder::add)
   * @param converter:    Function that converts the passed value, to a json compatible value
   * @param key:          key of the value in the jsonObject to be added
   * @param value:        value to be added
   * @param defaultValue: value to use in case the given value is null
   * @param <T>:          passed  value type
   * @param <R>:          added value type
   */
  public static <T, R> void serializeValue(BiConsumer<String, R> builder, Function<T, R> converter, String key, T value, R defaultValue) {
    Objects.requireNonNull(defaultValue);
    R transformedValue;
    if (value == null) {
      transformedValue = defaultValue;
    } else {
      transformedValue = converter.apply(value);
    }
    builder.accept(key, transformedValue);
  }


  /**
   * @param key:       key of the json object
   * @param fetcher:   function to get the value from the jsonObject (json::getString)
   * @param converter: Function to convert the fetched data to another type (Enum::valueOf).
   * @param <T>:       Type fetched from the JsonObject
   * @param <R>:       Type returned after the convertion (e.g. PaymentType)
   * @return
   */
  public static <T, R> Optional<R> getValue(String key, Function<String, T> fetcher, Function<T, R> converter) {
    return JsonUtils.getValue(key, fetcher).map(converter);
  }

  public static <T, R> void assignValue(Consumer<R> assigner, String key, Function<String, T> fetcher, Function<T, R> converter, R defaultValue) {
    Objects.requireNonNull(defaultValue);
    R value = JsonUtils.getValue(key, fetcher).map(converter).orElse(defaultValue);
    assigner.accept(value);
  }

  public static <T, R, K> K assignValue(Function<R, K> assigner, String key, Function<String, T> fetcher, Function<T, R> converter, R defaultValue) {
    Objects.requireNonNull(defaultValue);
    R value = JsonUtils.getValue(key, fetcher).map(converter).orElse(defaultValue);
    return assigner.apply(value);
  }

  /**
   * @param assigner      assignation function. Normally a normal setter (e.g. bean::setValue)
   * @param key:          key of the json object
   * @param fetcher:      function to get the value from the jsonObject (json::getString)
   * @param defaultValue: returned value in case null value has been provided
   * @param <T>:          Type fetched from the JsonObject
   * @return
   */
  public static <T> void assignValue(Consumer<T> assigner, String key, Function<String, T> fetcher, T defaultValue) {
    Objects.requireNonNull(defaultValue);
    T value = JsonUtils.getValue(key, fetcher).orElse(defaultValue);
    assigner.accept(value);
  }

  public static <T, K> K assignValue(Function<T, K> assigner, String key, Function<String, T> fetcher, T defaultValue) {
    Objects.requireNonNull(defaultValue);
    T value = JsonUtils.getValue(key, fetcher).orElse(defaultValue);
    return assigner.apply(value);
  }

  /**
   * @param key:          key of the json object
   * @param fetcher:      function to get the value from the jsonObject (json::getString)
   * @param defaultValue: returned value in case null value has been provided
   * @param <T>:          Type fetched from the JsonObject
   * @return
   */
  public static <T> T getValueOrDefault(String key, Function<String, T> fetcher, T defaultValue) {
    Objects.requireNonNull(defaultValue);
    return JsonUtils.getValue(key, fetcher).orElse(defaultValue);
  }

  /**
   * @param key:          key of the json object
   * @param fetcher:      function to get the value from the jsonObject (json::getString)
   * @param converter:    Function to convert the fetched data to another type (Enum::valueOf).
   * @param defaultValue: returned value in case null value has been provided
   * @param <T>:          Type fetched from the JsonObject
   * @param <R>:          Type returned after the convertion (e.g. PaymentType)
   * @return
   */
  public static <T, R> R getValue(String key, Function<String, T> fetcher, Function<T, R> converter, R defaultValue) {
    Objects.requireNonNull(defaultValue);
    return JsonUtils.getValue(key, fetcher).map(converter).orElse(defaultValue);
  }

  public static <T> Optional<T> getValue(String key, Function<String, T> fetcher) {

    try {
      T value = fetcher.apply(key);
      return Optional.ofNullable(value);
    } catch (NullPointerException e) {
      return Optional.empty();
    }

  }
}
