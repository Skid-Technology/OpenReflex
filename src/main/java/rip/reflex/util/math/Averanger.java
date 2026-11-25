package rip.reflex.util.math;

import java.util.Arrays;

public class Averanger {
   private final int size;
   private final long[] arr;

   public Averanger(int size) {
      this.size = size;
      this.arr = new long[size];
   }

   public void next(long i) {
      System.arraycopy(this.arr, 0, this.arr, 1, this.arr.length - 1);
      this.arr[0] = i;
   }

   public long value() {
      return Arrays.stream(this.arr).sum() / (long)this.size;
   }
}
