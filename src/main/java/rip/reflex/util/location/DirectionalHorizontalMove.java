package rip.reflex.util.location;

import org.bukkit.util.Vector;
import rip.reflex.util.math.Maths;

public class DirectionalHorizontalMove {
   private final double oldX;
   private final double oldZ;
   private final double newX;
   private final double newZ;
   private final double movedX;
   private final double movedZ;
   private final Vector vector;

   public DirectionalHorizontalMove(double oldX, double oldZ, double newX, double newZ) {
      this(oldX, oldZ, newX, newZ, newX - oldX, newZ - oldZ);
   }

   public DirectionalHorizontalMove(double oldX, double oldZ, double newX, double newZ, double movedX, double movedZ) {
      this.oldX = oldX;
      this.oldZ = oldZ;
      this.newX = newX;
      this.newZ = newZ;
      this.movedX = movedX;
      this.movedZ = movedZ;
      this.vector = new Vector(movedX, 0.0, movedZ);
   }

   public boolean isCodirectional(Vector other) {
      return Maths.areHorizontallyCodirectional(this.vector, other);
   }

   public boolean isCodirectional(DirectionalHorizontalMove other) {
      return this.isCodirectional(other.asVector());
   }

   public double distance(Vector other) {
      Vector o = other.clone().setY(0);
      return this.vector.distance(o);
   }

   public double distance(DirectionalHorizontalMove other) {
      return this.distance(other.asVector());
   }

   public double angle(Vector other) {
      Vector o = other.clone().setY(0);
      return (double)this.vector.angle(o);
   }

   public double angle(DirectionalHorizontalMove other) {
      return this.angle(other.asVector());
   }

   public double dotProduct(Vector other) {
      Vector o = other.clone().setY(0);
      return Maths.dotProduct(this.vector, o);
   }

   public double dotProduct(DirectionalHorizontalMove other) {
      return this.dotProduct(other.asVector());
   }

   public Vector crossProduct(Vector other) {
      Vector o = other.clone().setY(0);
      return this.vector.clone().crossProduct(o);
   }

   public Vector crossProduct(DirectionalHorizontalMove other) {
      return this.crossProduct(other.asVector());
   }

   public Vector asVector() {
      return this.vector.clone();
   }

   public String getMoveString() {
      return String.format("[%s, %s]", this.movedX, this.movedZ);
   }

   public String getCoordsString() {
      return String.format("(%s->%s, %s->%s)", this.oldX, this.newX, this.oldZ, this.newZ);
   }

   @Override
   public String toString() {
      return this.getMoveString() + this.getCoordsString();
   }

   public double getOldX() {
      return this.oldX;
   }

   public double getOldZ() {
      return this.oldZ;
   }

   public double getNewX() {
      return this.newX;
   }

   public double getNewZ() {
      return this.newZ;
   }

   public double getMovedX() {
      return this.movedX;
   }

   public double getMovedZ() {
      return this.movedZ;
   }
}
