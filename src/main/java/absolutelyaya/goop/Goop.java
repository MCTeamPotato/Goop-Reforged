package absolutelyaya.goop;

import absolutelyaya.goop.client.GoopConfig;
import absolutelyaya.goop.network.NetworkHandler;
import absolutelyaya.goop.registries.ParticleRegistry;
import com.mojang.logging.LogUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Goop.MOD_ID)
public class Goop {
    public static final String MOD_ID = "goop";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ConfigScreenHandler.ConfigScreenFactory FACTORY = new ConfigScreenHandler.ConfigScreenFactory(GoopConfig::getConfigScreen);

    public Goop() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        AutoConfig.register(GoopConfig.class, GsonConfigSerializer::new);
        ModLoadingContext.get().registerExtensionPoint(FACTORY.getClass(), () -> FACTORY);
        ParticleRegistry.PARTICLE_REGISTER.register(eventBus);
        NetworkHandler.register();
        //GoopEmitterRegistry.freeze();
    }

    public static void LogInfo(String warning) {
        LOGGER.info(warning);
    }

    public static void LogWarning(String warning) {
        LOGGER.warn(warning);
    }
}
