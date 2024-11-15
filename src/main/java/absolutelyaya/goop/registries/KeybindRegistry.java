package absolutelyaya.goop.registries;

import absolutelyaya.goop.Goop;
import absolutelyaya.goop.particles.GoopParticle;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class KeybindRegistry {
    public static final KeyMapping CLEAR_GOOP = new KeyMapping(
            "key.goop.clear",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "category.goop"
    );
}


