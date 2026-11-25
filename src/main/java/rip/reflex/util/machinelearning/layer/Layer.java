package rip.reflex.util.machinelearning.layer;

import java.util.ArrayList;
import rip.reflex.util.machinelearning.Dataset;

public abstract class Layer {
   public abstract void computeOutput();

   public abstract void learn(ArrayList<Dataset> datasets);
}
