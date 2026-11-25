package me.darksidecode.kantanj.formatting;

import java.util.ArrayList;
import java.util.List;
import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;

public class ConditionalFormatter {
   private static final String OPS = "~<=>";
   private static final int EXPECTING_OBJ = 0;
   private static final int EXPECTING_CONDITION = 1;
   private static final int EXPECTING_RESULT = 2;
   private static final int EXPECT_RANGE_START = 0;
   private static final int EXPECT_ONE_DOT = 1;
   private static final int EXPECT_RANGE_END = 2;
   private final List<Condition> conditions = new ArrayList<>();
   private String plainTail = "";
   private boolean openingQuote;
   private int lastQuotePos;
   private int state = 0;
   private int rangeSpecState = 0;
   private StringBuilder curNum = new StringBuilder();
   private StringBuilder op = new StringBuilder();
   private StringBuilder against = new StringBuilder();
   private StringBuilder result = new StringBuilder();
   private Condition.Branch curBranch = new Condition.Branch();
   private Condition curCond;
   private boolean appendCurNum;
   private boolean elseBranch;
   private boolean rangeSpec;

   public ConditionalFormatter(String s) {
      if (s != null && !s.trim().isEmpty()) {
         char[] chars = s.toCharArray();
         StringBuilder pre = new StringBuilder();

         for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\'') {
               this.openingQuote = !this.openingQuote;
               this.lastQuotePos = i;
               if (this.openingQuote) {
                  if (i == chars.length - 1) {
                     throw new SyntaxException("unclosed quote at char " + (i + 1));
                  }

                  i = this.parseCondition(chars, i, pre.toString());
                  pre = new StringBuilder();
               }
            } else {
               pre.append(c);
            }
         }

         if (this.openingQuote) {
            throw new SyntaxException("unclosed quote at char " + (this.lastQuotePos + 1));
         } else {
            if (pre.length() > 0) {
               this.plainTail = pre.toString();
            }

            this.conditions.forEach(Condition::validate);
         }
      } else {
         throw new IllegalArgumentException("input string cannot be empty");
      }
   }

   private int parseCondition(char[] chars, int start, String pre) {
      char[] innerChars = this.getInner(chars, start);
      this.curCond = new Condition();
      this.curCond.pre = pre;

      for (int pos = 0; pos < innerChars.length; pos++) {
         char ic = innerChars[pos];
         boolean skipSpace = ic == ' ' && this.state != 2;
         boolean skipColon = ic == ':' && this.elseBranch;
         if (!skipSpace && !skipColon) {
            switch (this.state) {
               case 0:
                  this.parseExpectingObj(ic, start, pos);
                  break;
               case 1:
                  this.parseExpectingCondition(ic, start, pos);
                  break;
               case 2:
                  int skip = this.parseExpectingResult(ic, start, pos);
                  if (skip != -1) {
                     return skip;
                  }
                  break;
               default:
                  throw new IllegalStateException(Integer.toString(this.state));
            }
         }
      }

      throw new SyntaxException("unterminated condition at char " + (start + 1));
   }

   private void parseExpectingObj(char ic, int start, int pos) {
      if (ic == '*') {
         this.elseBranch = true;
         this.state = 2;
      } else if (ic == '{') {
         this.appendCurNum = true;
      } else if (this.appendCurNum) {
         if (ic == '}') {
            if (this.curNum.length() == 0) {
               throw new SyntaxException("empty pointer at char " + (start + 1 + pos + 1));
            }

            this.curBranch.obj = Integer.parseInt(this.curNum.toString());
            this.curNum = new StringBuilder();
            this.appendCurNum = false;
            this.state = 1;
         } else {
            if (!Character.isDigit(ic)) {
               throw new SyntaxException("invalid non-int pointer at char " + (start + 1 + pos + 1));
            }

            this.curNum.append(ic);
         }
      }
   }

   private void parseExpectingCondition(char ic, int start, int pos) {
      boolean operatorChar = "~<=>".indexOf(ic) != -1;
      if (!this.rangeSpec) {
         this.rangeSpec = this.op.length() == 1 && this.op.charAt(0) == '~';
      }

      if (ic == '!') {
         this.curBranch.negate = true;
      } else if (!operatorChar && !this.rangeSpec) {
         if (this.curBranch.op == null) {
            if (this.op.length() == 0) {
               throw new SyntaxException("empty comparison operator at char " + (start + 1 + pos + 1));
            }

            this.curBranch.op = this.op.toString();
            this.op = new StringBuilder();
         }

         if (ic == ':') {
            if (this.against.length() == 0) {
               throw new SyntaxException("nothing to compare to at char " + (start + 1 + pos + 1));
            }

            this.curBranch.against = this.against.toString();
            this.against = new StringBuilder();
            this.state = 2;
         } else {
            this.against.append(ic);
         }
      } else {
         boolean prevCharDigit = this.op.length() > 1 && Character.isDigit(this.op.charAt(this.op.length() - 1));
         this.op.append(ic);
         if (this.rangeSpec) {
            boolean validChar = ic == '.' || ic == ':' || Character.isDigit(ic);
            if (!validChar) {
               throw new SyntaxException(String.format("illegal symbol %s in range spec at char %s", ic, start + 1 + pos + 1));
            }

            if (this.rangeSpecState == 0 && ic == '.') {
               if (!prevCharDigit) {
                  throw new SyntaxException("expected range start number at char " + (start + 1 + pos + 1));
               }

               this.rangeSpecState = 1;
            } else if (this.rangeSpecState == 1) {
               if (ic != '.') {
                  throw new SyntaxException("expected range start/end separator \"..\" at char " + (start + 1 + pos + 1));
               }

               this.rangeSpecState = 2;
            } else if (this.rangeSpecState == 2) {
               if (ic == '.') {
                  throw new SyntaxException("expected colon \":\" at the end of range spec at char " + (start + 1 + pos + 1));
               }

               if (ic == ':') {
                  if (!prevCharDigit) {
                     throw new SyntaxException("expected range end number at char " + (start + 1 + pos + 1));
                  }

                  this.curBranch.op = "~";
                  this.curBranch.against = this.op.substring(1, this.op.length() - 1);
                  this.rangeSpecState = 0;
                  this.rangeSpec = false;
                  this.op = new StringBuilder();
                  this.against = new StringBuilder();
                  this.state = 2;
               }
            }
         }
      }
   }

   private int parseExpectingResult(char ic, int start, int pos) {
      if (ic == '|') {
         if (this.elseBranch) {
            throw new SyntaxException("else branch must be the last branch of a condition: unexpected OR operator at char " + (start + 1 + pos + 1));
         }

         String r = this.result.toString().trim();
         if (r.isEmpty()) {
            throw new SyntaxException("expected result at char " + (start + 1 + pos + 1));
         }

         this.curBranch.result = r;
         this.curCond.branches.add(this.curBranch);
         this.result = new StringBuilder();
         this.curBranch = new Condition.Branch();
         this.state = 0;
      } else {
         if (ic == '\'') {
            if (!this.elseBranch) {
               throw new SyntaxException("illegal condition end: missing else branch at char " + (start + 1 + pos + 1));
            }

            String r = this.result.toString().trim();
            if (r.isEmpty()) {
               throw new SyntaxException("expected result at char " + (start + 1 + pos + 1));
            }

            this.openingQuote = !this.openingQuote;
            this.lastQuotePos = start;
            this.curBranch.result = r;
            this.curCond.elseBranch = this.curBranch;
            this.conditions.add(this.curCond);
            return start + pos + 1;
         }

         this.result.append(ic);
      }

      return -1;
   }

   private char[] getInner(char[] chars, int start) {
      StringBuilder inner = new StringBuilder();

      for (int j = start + 1; j < chars.length; j++) {
         char next = chars[j];
         inner.append(next);
         if (next == '\'') {
            if (j == start + 1) {
               throw new SyntaxException("empty condition at char " + (start + 1));
            }
            break;
         }
      }

      return inner.toString().toCharArray();
   }

   public String format(Object... args) {
      if (args == null) {
         throw new NullPointerException("args cannot be null");
      } else {
         StringBuilder formatted = new StringBuilder();
         this.conditions.forEach(cond -> formatted.append(cond.apply(args)));
         String f = formatted.toString() + this.plainTail;

         for (int i = 0; i < args.length; i++) {
            Object o = args[i];
            if (o == null) {
               throw new NullPointerException("object at index " + i + " must not be null");
            }

            f = f.replace("{" + i + "}", o.toString());
         }

         return f;
      }
   }
}
