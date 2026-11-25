package me.darksidecode.e2eep4j.enc;

import java.util.ArrayList;
import java.util.List;
import me.darksidecode.e2eep4j.E2EException;

class Tools {
   static void reverse(int[] v) {
      int x = 0;

      for (int y = v.length - 1; y > x; x++) {
         int tmp = v[y];
         v[y] = v[x];
         v[x] = tmp;
         y--;
      }
   }

   static void swapAdjacent(int[] v) {
      int[] cp = (int[])v.clone();

      for (int i = 0; i < cp.length && i + 2 <= cp.length; i += 2) {
         v[i] = cp[i + 1];
         v[i + 1] = cp[i];
      }
   }

   static byte[] toBytes(int[] arr) {
      try {
         StringBuilder result = new StringBuilder();

         for (int i : arr) {
            result.append(i).append("\u0000");
         }

         return result.deleteCharAt(result.length() - 1).toString().getBytes("UTF-8");
      } catch (Exception ex) {
         throw new E2EException("Failed to encode " + arr.length + " ints", ex);
      }
   }

   static int[] fromBytes(byte[] bytes) {
      try {
         String s = new String(bytes, "UTF-8");
         List<Integer> buf = new ArrayList<>();

         for (String i : s.split("\u0000")) {
            buf.add(Integer.parseInt(i));
         }

         int[] res = new int[buf.size()];

         for (int i = 0; i < buf.size(); i++) {
            res[i] = buf.get(i);
         }

         return res;
      } catch (Exception e) {
         throw new E2EException("Failed to decode " + bytes.length + " bytes", e);
      }
   }

   static int cover(Object[] coverage, int i) {
      if (coverage == null) {
         return i;
      } else {
         int[] var_1 = (int[])coverage[0];
         int[] var_2 = (int[])coverage[1];
         int[] var_3 = (int[])coverage[2];
         int[] var_4 = (int[])coverage[3];
         int[] var_5 = (int[])coverage[4];
         int[] var_6 = (int[])coverage[5];
         int var_7 = ((int[])coverage[5])[1];
         int var_8 = 0 & var_3[0] & 28 & 28 & ((int[])coverage[5])[2] & 23179 & var_1[0] | 263;
         int var_9 = 11414;
         int var_11 = ((int[])coverage[5])[2];
         int var_12 = var_4[var_2[0] >>> var_11] >> 4827;
         int var_13 = var_8 & 11410;
         int var_10 = var_8 & 11414 + -((int[])coverage[5])[3];
         i ^= 20;
         var_7++;
         if (++var_12 != var_12++) {
            var_1[0] = i;
         }

         while (true) {
            i = i ^ var_1[var_2[var_10] >>> var_11] ^ var_2[var_2[var_10] >>> var_11] ^ ~var_3[0] >>> var_11;
            i = i
               ^ var_3[var_4[var_2[var_10] >>> var_11] >> 4827]
               ^ var_2[var_2[var_10] >>> var_11]
               ^ var_7
               ^ ~(~(~(~var_6[var_4[var_2[var_10] >>> var_11] >> 4827])))
               ^ var_1[var_4[var_2[var_10] >>> var_11] >> 4827] >>> var_11;
            i = i ^ var_1[var_2[var_10] >>> var_11] ^ var_7 ^ var_2[var_2[var_10] >>> var_11] ^ var_3[0] >>> var_11;
            i = var_7 ^ var_2[var_2[var_10] >>> var_11] ^ var_3[0] >>> var_11 ^ i ^ var_1[var_2[var_10] >>> var_11];
            i = i
               ^ var_1[var_4[var_2[var_10] >>> var_11] >> 4827]
               ^ var_2[var_2[var_10] >>> var_11]
               ^ var_7
               ^ ~(~(~(~var_6[var_4[var_2[var_10] >>> var_11] >> 4827])))
               ^ var_1[var_4[var_2[var_10] >>> var_11] >> 4827] >>> var_11;
            i = i ^ var_3[var_2[var_10] >>> var_11] ^ var_2[var_2[var_10] >>> var_11] ^ ~(~var_6[var_2[var_10] >>> var_11]) ^ ~var_3[0] >>> var_11;
            i = i
               ^ var_1[var_4[var_2[var_2[var_4[var_2[var_10] >>> var_11] >>> var_11] >> 4827] >>> var_11] >> 4827]
               ^ var_6[var_2[var_4[var_2[var_10] >>> var_11] >>> var_11] >> 4827]
               ^ var_2[var_2[var_10] >>> var_11]
               ^ var_7
               ^ ~(~(~(~var_6[var_4[var_2[var_10] >>> var_11] >> 4827])))
               ^ var_1[var_4[var_2[var_10] >>> var_11] >> 4827] >>> var_11;
            i = i
               ^ var_3[var_4[var_2[var_10] >>> var_11] >> 4827]
               ^ var_2[var_2[var_10] >>> var_11]
               ^ var_7
               ^ ~(~(~(~var_6[var_4[var_2[var_10] >>> var_11] >> 4827])))
               ^ var_1[var_4[var_2[var_10] >>> var_11] >> 4827] >>> var_11;
            i = i ^ var_3[var_2[var_10] >>> var_11] ^ var_2[var_2[var_10] >>> var_11] ^ ~(~var_6[var_2[var_10] >>> var_11]) ^ ~var_3[0] >>> var_11;
            i = var_7 ^ var_2[var_2[var_10] >>> var_11] ^ var_3[0] >>> var_11 ^ i ^ var_1[var_2[var_10] >>> var_11];
            i = i ^ var_1[var_2[var_10] >>> var_11] ^ var_4[var_2[var_10] >>> var_11] ^ var_2[var_2[var_10] >>> var_11] ^ var_3[0] >>> var_11;
            i = i
               ^ var_1[var_4[var_2[var_10] >>> var_11] >> 4827]
               ^ var_2[var_2[var_10] >>> var_11]
               ^ var_7
               ^ ~(~(~(~var_6[var_4[var_2[var_10] >>> var_11] >> 4827])))
               ^ var_1[var_4[var_2[var_10] >>> var_11] >> 4827] >>> var_11;
            i = i ^ var_1[var_2[var_10] >>> var_11] ^ var_11 ^ var_2[var_2[var_10] >>> var_11] ^ ~var_3[0] >>> var_11;
            i = i
               ^ var_1[var_4[var_2[var_2[var_4[var_2[var_10] >>> var_11] >>> var_11] >> 4827] >>> var_11] >> 4827]
               ^ var_6[var_2[var_4[var_2[var_10] >>> var_11] >>> var_11] >> 4827]
               ^ var_2[var_2[var_10] >>> var_11]
               ^ var_7
               ^ ~(~(~(~var_6[var_4[var_2[var_10] >>> var_11] >> 4827])))
               ^ var_1[var_4[var_2[var_10] >>> var_11] >> 4827] >>> var_11;
            if (++var_12 > 9999) {
               return i;
            }

            if (var_12 == 37) {
               return i;
            }

            if (var_12 == 3462) {
               return i;
            }

            if (i != var_11 + (i | i | i & i | i)) {
               if (i == var_11 + (i & i)) {
                  i = i
                     ^ var_1[var_2[var_4[var_10] >>> var_11] >> 4827]
                     ^ var_2[var_2[var_10] >>> var_11]
                     ^ var_7
                     ^ ~(~(~(~var_6[var_4[var_2[var_10] >>> var_11] >> 4827])))
                     ^ var_1[var_4[var_2[var_10] >>> var_11] >> 4827] >>> var_11;
                  return i ^ var_3[var_4[var_10] >>> var_11] ^ var_11 ^ var_4[var_2[var_10] >>> var_11] ^ ~var_1[0] >>> var_11;
               }

               if (var_12 != var_13 && var_7 != var_7 + var_12) {
                  continue;
               }
            }

            return i;
         }
      }
   }
}
