package rip.reflex.util.time;

public enum FullTimeUnit {
   MILLISECONDS(0.0),
   SECONDS(1000.0),
   MINUTES(60.0),
   HOURS(60.0),
   DAYS(24.0),
   WEEKS(7.0),
   MONTHS(4.0),
   YEARS(12.0);

   private final double factor;

   public static void main(String[] args) {
      System.out.println(MONTHS.to(16.0, YEARS));
   }

   public double to(double input, FullTimeUnit result) {
      int target = result.ordinal();
      int current = this.ordinal();
      boolean down = target < current;
      if (target == current) {
         return input;
      } else {
         double work = input;

         while (current != target) {
            if (down) {
               work *= values()[current--].factor;
            } else {
               FullTimeUnit[] units = values();
               current++;
               work /= units[current].factor;
            }
         }

         return (double)Math.round(work * 100.0) / 100.0;
      }
   }

   private FullTimeUnit(double factor) {
      this.factor = factor;
   }
}
