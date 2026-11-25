package me.darksidecode.kantanj.currency;

import java.util.Arrays;
import java.util.NoSuchElementException;
import me.darksidecode.kantanj.types.Check;

public enum Currency {
   USD("US Dollar", (short)840),
   EUR("Euro", (short)978),
   RUB("Russian Ruble", (short)643),
   CNY("Yuan Renminbi", (short)156),
   GBP("Pound Sterling", (short)826),
   JPY("Yen", (short)392),
   CHF("Swiss Franc", (short)756),
   AUD("Australian Dollar", (short)30),
   CAD("Canadian Dollar", (short)124),
   SEK("Swedish Krona", (short)752),
   NZD("New Zealand Dollar", (short)554);

   private final String name;
   private final short number;

   private Currency(String name, short number) {
      this.name = name;
      this.number = number;
   }

   @Override
   public String toString() {
      return this.name() + '/' + this.number + " (" + this.name + ")";
   }

   public static Currency byHumanReadableName(String name) throws NoSuchElementException {
      Check.notNull(name, "currency name cannot be null");
      return Arrays.stream(values())
         .filter(currency -> currency.getName().equalsIgnoreCase(name))
         .findAny()
         .orElseThrow(() -> new NoSuchElementException("unsupported currency: name: " + name));
   }

   public static Currency byNumber(short number) throws NoSuchElementException {
      if (number >= 0 && number <= 999) {
         return Arrays.stream(values())
            .filter(currency -> currency.getNumber() == number)
            .findAny()
            .orElseThrow(() -> new NoSuchElementException("unsupported currency: number: " + number));
      } else {
         throw new IllegalArgumentException("currency number must be between 0 and 999 (inclusive)");
      }
   }

   public String getName() {
      return this.name;
   }

   public short getNumber() {
      return this.number;
   }
}
