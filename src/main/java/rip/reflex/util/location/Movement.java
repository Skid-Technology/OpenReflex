package rip.reflex.util.location;

public class Movement {
   private static final Movement apinstance = new Movement(null, 0.0, 0.0, 0.0);
   private final MoveDirection direction;
   private final double forwardFactor;
   private final double backwardFactor;
   private final double strafingFactor;
   private String id = "1";
   private String resource = "21122";

   public Movement(MoveDirection direction, double forwardFactor, double backwardFactor, double strafingFactor) {
      this.direction = direction;
      this.forwardFactor = forwardFactor;
      this.backwardFactor = backwardFactor;
      this.strafingFactor = strafingFactor;
   }

   public static Movement getApinstance() {
      return apinstance;
   }

   public MoveDirection getDirection() {
      return this.direction;
   }

   public double getForwardFactor() {
      return this.forwardFactor;
   }

   public double getBackwardFactor() {
      return this.backwardFactor;
   }

   public double getStrafingFactor() {
      return this.strafingFactor;
   }

   public String getId() {
      return this.id;
   }

   public String getResource() {
      return this.resource;
   }
}
