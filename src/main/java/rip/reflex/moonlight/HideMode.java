package rip.reflex.moonlight;

public enum HideMode {
   NO,
   COMMAND,
   DEEP;

   public boolean hasMinimum(HideMode required) {
      return this.ordinal() >= required.ordinal();
   }
}
