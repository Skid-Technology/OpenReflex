package rip.reflex.util.performance.timings;

public class TimingsEntry {
   private static final int integrityDataFingerprint_b = 758097089;
   private static final int integrityDataArrInfo_b = 9406135;
   private final String name;
   private final long executionTime;

   public TimingsEntry(String name, long executionTime) {
      this.name = name;
      this.executionTime = executionTime;
   }

   public String getName() {
      return this.name;
   }

   public long getExecutionTime() {
      return this.executionTime;
   }
}
