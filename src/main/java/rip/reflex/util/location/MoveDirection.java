package rip.reflex.util.location;

public enum MoveDirection {
   FORWARD,
   BACKWARD,
   STRAFING;

   @Override
   public String toString() {
      String color = "§";
      switch (this) {
         case FORWARD:
            color = color + "a";
            break;
         case BACKWARD:
            color = color + "c";
            break;
         case STRAFING:
            color = color + "b";
      }

      return color + super.toString();
   }

   public String shorten() {
      return "" + super.toString().toLowerCase().charAt(0);
   }
}
