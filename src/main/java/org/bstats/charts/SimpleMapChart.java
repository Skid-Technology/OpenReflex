package org.bstats.charts;

import org.bstats.Country;
import org.bstats.Metrics;
import org.json.simple.JSONObject;

public abstract class SimpleMapChart extends Metrics.CustomChart {
   public SimpleMapChart(String chartId) {
      super(chartId);
   }

   public abstract Country getValue();

   @Override
   protected JSONObject getChartData() {
      JSONObject data = new JSONObject();
      Country value = this.getValue();
      if (value == null) {
         return null;
      } else {
         data.put("value", value.getCountryIsoTag());
         return data;
      }
   }
}
