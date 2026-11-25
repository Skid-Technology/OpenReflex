package me.darksidecode.kantanj.formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;

public class Condition {
   final List<Condition.Branch> branches = new ArrayList<>();
   Condition.Branch elseBranch;
   String pre;
   private boolean validated;
   private int validatedHash = -1;

   void validate() {
      int h = this.hashCode();
      if (!this.validated || h == 0 || h != this.validatedHash) {
         this.validated = false;
         if (this.pre == null) {
            this.pre = "";
         }

         if (this.branches.isEmpty()) {
            throw new SyntaxException("missing conditional branches");
         } else if (this.elseBranch == null) {
            throw new SyntaxException("missing else branch");
         } else if (this.elseBranch.result == null) {
            throw new SyntaxException("missing result at the else branch");
         } else {
            for (int i = 0; i < this.branches.size(); i++) {
               Condition.Branch branch = this.branches.get(i);
               if (branch.obj == -1) {
                  throw new SyntaxException("missing target obj pointer at branch " + i);
               }

               if (branch.op == null) {
                  throw new SyntaxException("missing comparison operator at branch " + i);
               }

               if (branch.against == null) {
                  throw new SyntaxException("missing against obj at branch " + i);
               }

               if (branch.result == null) {
                  throw new SyntaxException("missing result at branch " + i);
               }
            }

            this.validated = true;
            this.validatedHash = this.hashCode();
         }
      }
   }

   private void ensureValidated() {
      if (!this.validated) {
         throw new IllegalStateException("condition is not validated");
      }
   }

   public String apply(Object... args) {
      if (args != null && args.length != 0) {
         this.ensureValidated();
         StringBuilder s = new StringBuilder(this.pre);
         String result = null;

         for (Condition.Branch branch : this.branches) {
            if (branch.test(args)) {
               result = branch.result;
               break;
            }
         }

         if (result == null) {
            result = this.elseBranch.result;
         }

         s.append(result);
         return s.toString();
      } else {
         throw new IllegalArgumentException("args must not be empty or null");
      }
   }

   @Override
   public int hashCode() {
      if (this.elseBranch != null && this.pre != null) {
         int h = 31 * (1 + this.branches.size());
         h += this.pre.hashCode();
         h += 31 * this.elseBranch.result.hashCode();

         for (int i = 0; i < this.branches.size(); i++) {
            int hBranch = this.branches.get(i).hashCode();
            if (hBranch == 0) {
               return 0;
            }

            h += 31 * (i + 1) + 31 * hBranch;
         }

         return Math.abs(h);
      } else {
         return 0;
      }
   }

   @Override
   public String toString() {
      this.ensureValidated();
      StringBuilder s = new StringBuilder("Condition: { branches: (");
      if (this.branches.isEmpty()) {
         s.append("none");
      } else {
         for (int i = 0; i < this.branches.size(); i++) {
            s.append(this.branches.get(i).toString());
            if (i < this.branches.size() - 1) {
               s.append(", ");
            }
         }
      }

      s.append("); elseBranch: ").append(this.elseBranch.toString());
      s.append("; pre: \"").append(this.pre).append("\"}");
      return s.toString();
   }

   static class Branch {
      private static final String RANGE_LIMITS_SPLIT = Pattern.quote("..");
      int obj = -1;
      String op;
      boolean negate;
      String against;
      String result;

      private boolean test(Object... args) {
         if (this.obj >= args.length) {
            throw new IllegalArgumentException("too few args: expected value at index " + this.obj);
         } else {
            Object target = args[this.obj];
            if (target == null) {
               throw new NullPointerException("object at index " + this.obj + " must not be null");
            } else {
               int idxOfOpening = this.against.indexOf(123);
               int idxOfClosing = this.against.indexOf(125);
               Object compareTo;
               if (idxOfClosing > idxOfOpening) {
                  String pointerStr = this.against.replace("{", "").replace("}", "");

                  int pointer;
                  try {
                     pointer = Integer.parseInt(pointerStr);
                  } catch (NumberFormatException ignored) {
                     throw new SyntaxException("invalid pointer: " + this.against);
                  }

                  if (pointer >= args.length) {
                     throw new IllegalArgumentException("too few args: expected value at index " + pointer);
                  }

                  compareTo = args[pointer];
                  if (compareTo == null) {
                     throw new NullPointerException("object at index " + this.obj + " must not be null");
                  }
               } else if (this.op.equals("~")) {
                  compareTo = null;
               } else if (this.against.contains(".")) {
                  try {
                     compareTo = Double.parseDouble(this.against);
                  } catch (NumberFormatException ignoredEx) {
                     compareTo = this.against;
                  }
               } else {
                  try {
                     compareTo = Integer.parseInt(this.against);
                  } catch (NumberFormatException ignoredErr) {
                     compareTo = this.against;
                  }
               }

               Class targetClass = target.getClass();
               Class againstClass = compareTo == null ? null : compareTo.getClass();
               if (compareTo != null && targetClass != againstClass) {
                  throw new SyntaxException(String.format("types mismatch: cannot compare %s to %s", targetClass.getSimpleName(), againstClass.getSimpleName()));
               } else {
                  boolean match;
                  if (this.op.equals("~")) {
                     if (!(target instanceof Number)) {
                        throw new SyntaxException("target object must be a number for range checks");
                     }

                     String[] limits = this.against.split(RANGE_LIMITS_SPLIT);
                     double low = Double.parseDouble(limits[0]);
                     double high = Double.parseDouble(limits[1]);
                     if (low >= high) {
                        throw new SyntaxException(String.format("low range limit must be less than high range limit; low: %s, high: %s", low, high));
                     }

                     double tNum = ((Number)target).doubleValue();
                     match = tNum >= low && tNum <= high;
                  } else {
                     assert compareTo != null;

                     if (target instanceof Number) {
                        double nTarget = ((Number)target).doubleValue();
                        double nAgainst = ((Number)compareTo).doubleValue();
                        match = this.compareNumbers(nTarget, nAgainst);
                     } else {
                        if (!this.op.equals("=")) {
                           throw new SyntaxException("operator " + this.op + " is not applicable for objects of type String");
                        }

                        match = target.equals(compareTo);
                     }
                  }

                  return this.negate != match;
               }
            }
         }
      }

      private boolean compareNumbers(double nTarget, double nAgainst) {
         switch (this.op) {
            case "=":
               return nTarget == nAgainst;
            case "<":
               return nTarget < nAgainst;
            case "<=":
               return nTarget <= nAgainst;
            case ">":
               return nTarget > nAgainst;
            case ">=":
               return nTarget >= nAgainst;
            default:
               throw new SyntaxException("operator " + this.op + " is not applicable for objects of type Number");
         }
      }

      @Override
      public int hashCode() {
         if (this.obj != -1 && this.op != null && this.against != null && this.result != null) {
            int h = 31 * this.obj + (this.negate ? 33 : 31) * this.op.hashCode() + this.against.hashCode() + this.result.hashCode();
            return Math.abs(h);
         } else {
            return 0;
         }
      }

      @Override
      public String toString() {
         return String.format("Branch[obj: %s, op: %s, negate: %s, against: %s, result: \"%s\"]", this.obj, this.op, this.negate, this.against, this.result);
      }
   }
}
