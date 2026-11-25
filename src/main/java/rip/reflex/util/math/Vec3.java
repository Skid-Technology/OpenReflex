package rip.reflex.util.math;

public class Vec3 {
   private double x;
   private double y;
   private double z;

   public Vec3() {
      this(0.0);
   }

   public Vec3(double cor) {
      this(cor, cor, cor);
   }

   public Vec3(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vec3 copy() {
      return new Vec3(this.x, this.y, this.z);
   }

   public Vec3 setX(double x) {
      this.x = x;
      return this;
   }

   public Vec3 setY(double y) {
      this.y = y;
      return this;
   }

   public Vec3 setZ(double z) {
      this.z = z;
      return this;
   }

   public Vec3 add(double n) {
      return this.add(n, n, n);
   }

   public Vec3 add(Vec3 v) {
      return this.add(v.x, v.y, v.z);
   }

   public Vec3 add(double x, double y, double z) {
      this.x += x;
      this.y += y;
      this.z += z;
      return this;
   }

   public Vec3 subtract(double n) {
      return this.subtract(n, n, n);
   }

   public Vec3 subtract(Vec3 v) {
      return this.subtract(v.x, v.y, v.z);
   }

   public Vec3 subtract(double x, double y, double z) {
      this.x -= x;
      this.y -= y;
      this.z -= z;
      return this;
   }

   public Vec3 multiply(double n) {
      return this.multiply(n, n, n);
   }

   public Vec3 multiply(Vec3 v) {
      return this.multiply(v.x, v.y, v.z);
   }

   public Vec3 multiply(double x, double y, double z) {
      this.x *= x;
      this.y *= y;
      this.z *= z;
      return this;
   }

   public Vec3 divide(double n) {
      return this.divide(n, n, n);
   }

   public Vec3 divide(Vec3 v) {
      return this.divide(v.x, v.y, v.z);
   }

   public Vec3 divide(double x, double y, double z) {
      this.x /= x;
      this.y /= y;
      this.z /= z;
      return this;
   }

   public Vec3 pow(double n) {
      return this.pow(n, n, n);
   }

   public Vec3 pow(Vec3 v) {
      return this.pow(v.x, v.y, v.z);
   }

   public Vec3 pow(double x, double y, double z) {
      this.x = Math.pow(this.x, x);
      this.y = Math.pow(this.y, y);
      this.z = Math.pow(this.z, z);
      return this;
   }

   public Vec3 sqrt() {
      this.x = Math.sqrt(this.x);
      this.y = Math.sqrt(this.y);
      this.z = Math.sqrt(this.z);
      return this;
   }

   public Vec3 cbrt() {
      this.x = Math.cbrt(this.x);
      this.y = Math.cbrt(this.y);
      this.z = Math.cbrt(this.z);
      return this;
   }

   public Vec3 abs() {
      this.x = Math.abs(this.x);
      this.y = Math.abs(this.y);
      this.z = Math.abs(this.z);
      return this;
   }

   public Vec3 negate() {
      this.x = -Math.abs(this.x);
      this.y = -Math.abs(this.y);
      this.z = -Math.abs(this.z);
      return this;
   }

   public Vec3 angle(Vec3 v) {
      return this.angle(v.x, v.y, v.z);
   }

   public Vec3 angle(double x, double y, double z) {
      return new Vec3(x - this.x, y - this.y, z - this.z);
   }

   public double distance(Vec3 v) {
      return this.distance(v.x, v.y, v.z);
   }

   public double distance(double x, double y, double z) {
      return Math.sqrt(Maths.square(x - this.x) + Maths.square(y - this.y) + Maths.square(z - this.z));
   }

   public double horizontalDistance(Vec3 v) {
      return this.horizontalDistance(v.x, v.z);
   }

   public double horizontalDistance(double x, double z) {
      return Math.sqrt(Maths.square(x - this.x) + Maths.square(z - this.z));
   }

   public double verticalDistance(Vec3 v) {
      return this.verticalDistance(v.y);
   }

   public double verticalDistance(double y) {
      return Math.abs(y - this.y);
   }

   public double correlate(Vec3 v) {
      return this.correlate(v.x, v.y, v.z);
   }

   public double correlate(double x, double y, double z) {
      return this.x / x * (this.y / y) * (this.z / z);
   }

   public boolean isCodirectional(Vec3 v) {
      return this.isCodirectional(v.x, v.y, v.z);
   }

   public boolean isCodirectional(double x, double y, double z) {
      int ax = Double.compare(this.x, 0.0);
      int ay = Double.compare(this.y, 0.0);
      int az = Double.compare(this.z, 0.0);
      int bx = Double.compare(x, 0.0);
      int by = Double.compare(y, 0.0);
      int bz = Double.compare(z, 0.0);
      return ax == bx && ay == by && az == bz;
   }

   public boolean isHorizontallyCodirectional(Vec3 v) {
      return this.isHorizontallyCodirectional(v.x, v.z);
   }

   public boolean isHorizontallyCodirectional(double x, double z) {
      int ax = Double.compare(this.x, 0.0);
      int az = Double.compare(this.z, 0.0);
      int bx = Double.compare(x, 0.0);
      int bz = Double.compare(z, 0.0);
      return ax == bx && az == bz;
   }

   public boolean isVerticallyCodirectional(Vec3 v) {
      return this.isVerticallyCodirectional(v.y);
   }

   public boolean isVerticallyCodirectional(double y) {
      return Double.compare(this.y, 0.0) == Double.compare(y, 0.0);
   }

   public boolean equals(Vec3 v) {
      return this.x == v.x && this.y == v.y && this.z == v.z;
   }

   @Override
   public String toString() {
      return "[" + this.x + "," + this.y + "," + this.z + "]";
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }
}
