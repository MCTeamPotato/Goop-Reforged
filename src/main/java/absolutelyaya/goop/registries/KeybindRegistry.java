package absolutelyaya.goop.registries;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class KeybindRegistry {
    public static final KeyMapping CLEAR_GOOP = new KeyMapping(
            "key.goop.clear",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "category.goop"
    );
}


