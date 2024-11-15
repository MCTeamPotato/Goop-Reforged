package absolutelyaya.goop.registries;

import absolutelyaya.goop.Goop;
import absolutelyaya.goop.particles.EggGoopParticleEffect;
import absolutelyaya.goop.particles.GoopDropParticleEffect;
import absolutelyaya.goop.particles.GoopParticleEffect;
import absolutelyaya.goop.particles.GoopStringParticleEffect;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_REGISTER = DeferredRegister.create(Registries.PARTICLE_TYPE, Goop.MOD_ID);
    public static final RegistryObject<ParticleType<GoopDropParticleEffect>> GOOP_DROP = register("goop_drop", false, new GoopDropParticleEffect.Factory(), type -> GoopDropParticleEffect.CODEC);
    public static final RegistryObject<ParticleType<GoopParticleEffect>> GOOP = register("goop", false, new GoopParticleEffect.Factory(), type -> GoopParticleEffect.CODEC);
    public static final RegistryObject<ParticleType<GoopStringParticleEffect>> GOOP_STRING = register("goop_string", false, new GoopStringParticleEffect.Factory(), type -> GoopStringParticleEffect.CODEC);
    public static final RegistryObject<ParticleType<EggGoopParticleEffect>> EGG_GOOP = register("egg_goop", false, new EggGoopParticleEffect.Factory(), type -> EggGoopParticleEffect.CODEC);

    private static <T extends ParticleOptions> RegistryObject<ParticleType<T>> register(String pKey, boolean pOverrideLimiter, ParticleOptions.Deserializer<T> pDeserializer, final Function<ParticleType<T>, Codec<T>> pCodecFactory) {
        return PARTICLE_REGISTER.register(pKey, () -> new ParticleType<>(pOverrideLimiter, pDeserializer) {
            public @NotNull Codec<T> codec() {
                return pCodecFactory.apply(this);
            }
        });
    }
    public static void registerProviders(RegisterParticleProvidersEvent event) {
    }
}
