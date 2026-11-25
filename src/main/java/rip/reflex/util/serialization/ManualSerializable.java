package rip.reflex.util.serialization;

import java.io.Serializable;
import java.util.List;

public interface ManualSerializable {
   List<Serializable> serialize();
}
