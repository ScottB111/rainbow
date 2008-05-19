package rainbow.types;

import java.text.DecimalFormat;

public class Real extends ArcNumber {
  private double value;

  public Real(double value) {
    this.value = value;
  }

  public static Real parse(String rep) {
    return make(Double.parseDouble(rep));
  }

  public static Real make(double v) {
    return new Real(v);
  }

  public String toString() {
    return new DecimalFormat("0.0##############").format(value);
  }

  public ArcObject eqv(ArcObject other) {
    return ((Real) other).value == value ? T : NIL;
  }

  public Object unwrap() {
    return value();
  }

  public double value() {
    return value;
  }

  public boolean isInteger() {
    return Math.floor(value) == value;
  }

  public double toDouble() {
    return value;
  }

  public long toInt() {
    return (long) value;
  }

  public ArcNumber negate() {
    return make(-value);
  }

  public int hashCode() {
    return new Double(value).hashCode();
  }

  public boolean equals(Object other) {
    return (this == other) || (other instanceof Real && value == ((Real) other).value);
  }
}
