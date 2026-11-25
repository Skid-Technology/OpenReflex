package me.darksidecode.kantanj.currency;

public interface CurrencyConverter {
   float convert(float amount, Currency from, Currency to);
}
