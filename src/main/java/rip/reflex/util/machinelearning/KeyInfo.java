package rip.reflex.util.machinelearning;

public class KeyInfo {
   public String getKey(int keyCode) {
      switch (keyCode) {
         case 0:
            return ".4c";
         case 1:
            return ".1f";
         default:
            Classifier.crash("parsing key code to key", new ComputeError("unexpected key code " + keyCode));
            throw new UnknownError();
      }
   }
}
