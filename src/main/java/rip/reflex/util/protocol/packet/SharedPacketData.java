package rip.reflex.util.protocol.packet;

public class SharedPacketData {
   private double x;
   private double y;
   private double z;
   private double oldX;
   private double oldY;
   private double oldZ;
   private float yaw;
   private float pitch;
   private double oldYaw;
   private double oldPitch;
   private boolean vehicleMove;
   private double movedX;
   private double movedY;
   private double movedZ;
   private double rotatedYaw;
   private double rotatedPitch;

   public SharedPacketData x(double x) {
      this.x = x;
      return this;
   }

   public SharedPacketData oldX(double oldX) {
      this.oldX = oldX;
      return this;
   }

   public SharedPacketData movedX(double movedX) {
      this.movedX = movedX;
      return this;
   }

   public SharedPacketData y(double y) {
      this.y = y;
      return this;
   }

   public SharedPacketData oldY(double oldY) {
      this.oldY = oldY;
      return this;
   }

   public SharedPacketData movedY(double movedY) {
      this.movedY = movedY;
      return this;
   }

   public SharedPacketData z(double z) {
      this.z = z;
      return this;
   }

   public SharedPacketData oldZ(double oldZ) {
      this.oldZ = oldZ;
      return this;
   }

   public SharedPacketData movedZ(double movedZ) {
      this.movedZ = movedZ;
      return this;
   }

   public SharedPacketData yaw(float yaw) {
      this.yaw = yaw;
      return this;
   }

   public SharedPacketData oldYaw(double oldYaw) {
      this.oldYaw = oldYaw;
      return this;
   }

   public SharedPacketData rotatedYaw(double rotatedYaw) {
      this.rotatedYaw = rotatedYaw;
      return this;
   }

   public SharedPacketData pitch(float pitch) {
      this.pitch = pitch;
      return this;
   }

   public SharedPacketData oldPitch(double oldPitch) {
      this.oldPitch = oldPitch;
      return this;
   }

   public SharedPacketData rotatedPitch(double rotatedPitch) {
      this.rotatedPitch = rotatedPitch;
      return this;
   }

   public SharedPacketData vehicleMove() {
      this.vehicleMove = true;
      return this;
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

   public double getOldX() {
      return this.oldX;
   }

   public double getOldY() {
      return this.oldY;
   }

   public double getOldZ() {
      return this.oldZ;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public double getOldYaw() {
      return this.oldYaw;
   }

   public double getOldPitch() {
      return this.oldPitch;
   }

   public boolean isVehicleMove() {
      return this.vehicleMove;
   }

   public double getMovedX() {
      return this.movedX;
   }

   public double getMovedY() {
      return this.movedY;
   }

   public double getMovedZ() {
      return this.movedZ;
   }

   public double getRotatedYaw() {
      return this.rotatedYaw;
   }

   public double getRotatedPitch() {
      return this.rotatedPitch;
   }
}
