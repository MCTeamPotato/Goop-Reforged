package absolutelyaya.goop.client;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class GoopClient {
    static ConfigHolder<GoopConfig> config;

    public static GoopConfig getConfig() {
        return config.getConfig();
    }

    // @Override
    public void onInitializeClient() {
        config = AutoConfig.register(GoopConfig.class, GsonConfigSerializer::new);

//		ParticleFactoryRegistry particleRegistry = ParticleFactoryRegistry.getInstance();
//		particleRegistry.register(ParticleRegistry.GOOP_DROP, GoopDropParticle.Factory::new);
//		particleRegistry.register(ParticleRegistry.GOOP, GoopParticle.Factory::new);
//		particleRegistry.register(ParticleRegistry.GOOP_STRING, GoopStringParticle.Factory::new);
//		particleRegistry.register(ParticleRegistry.EGG_GOOP, EggGoopParticle.Factory::new);

//        PacketRegistry.registerClient();
//        KeybindRegistry.register();
    }

    public static boolean recolorMature() {
        return config.get().censorMature && config.get().censorMode.equals(CensorMode.RECOLOR);
    }

    public static boolean hideMature() {
        return config.get().censorMature && config.get().censorMode.equals(CensorMode.HIDE);
    }
}
