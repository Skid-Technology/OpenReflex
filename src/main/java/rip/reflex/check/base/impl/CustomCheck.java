package rip.reflex.check.base.impl;

import rip.reflex.api.Cheat;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckType;

public class CustomCheck extends Check {
   public CustomCheck(String name, String comp, Cheat cheat) {
      super(name, comp, CheckType.CUSTOM, cheat);
   }
}
