package absolutelyaya.goop.event;

import absolutelyaya.goop.Goop;
import absolutelyaya.goop.api.Examples;
import absolutelyaya.goop.particles.EggGoopParticle;
import absolutelyaya.goop.particles.GoopDropParticle;
import absolutelyaya.goop.particles.GoopParticle;
import absolutelyaya.goop.particles.GoopStringParticle;
import absolutelyaya.goop.registries.KeybindRegistry;
import absolutelyaya.goop.registries.ParticleRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Goop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvent {
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void keyBinding(RegisterKeyMappingsEvent event) {
        event.register(KeybindRegistry.CLEAR_GOOP);
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(()->{
            new Examples().registerGoopEmitters();
        });
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.GOOP_DROP.get(), GoopDropParticle.Factory::new);
        event.registerSpriteSet(ParticleRegistry.GOOP.get(), GoopParticle.Factory::new);
        event.registerSpriteSet(ParticleRegistry.GOOP_STRING.get(), GoopStringParticle.Factory::new);
        event.registerSpriteSet(ParticleRegistry.EGG_GOOP.get(), EggGoopParticle.Factory::new);
    }
}
