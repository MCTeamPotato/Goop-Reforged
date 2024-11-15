package absolutelyaya.goop.particles;

import absolutelyaya.goop.api.ExtraGoopData;
import absolutelyaya.goop.api.IGoopEffectFactory;
import absolutelyaya.goop.api.WaterHandling;
import absolutelyaya.goop.registries.ParticleRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

public class EggGoopParticleEffect extends GoopParticleEffect {
    public static final Codec<EggGoopParticleEffect> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                                Vec3.CODEC.fieldOf("color").forGetter((effect) -> effect.color),
                                Codec.FLOAT.fieldOf("scale").forGetter((effect) -> effect.scale),
                                Vec3.CODEC.fieldOf("dir").forGetter((effect) -> effect.dir),
                                Codec.BOOL.fieldOf("mature").forGetter((effect) -> effect.mature),
                                Codec.BYTE.fieldOf("waterHandling").forGetter((effect) -> (byte) effect.waterHandling.ordinal()),
                                ExtraGoopData.getCodec().fieldOf("extraData").forGetter((eggGoopParticleEffect -> eggGoopParticleEffect.extraGoopData)))
                        .apply(instance, EggGoopParticleEffect::new));
    }

    /**
     * @param color     The Particles Color.
     * @param scale     The Particles Scale.
     * @param dir       The normal direction of the surface the particle is attached to.
     * @param mature    Whether the Particle is Mature Content
     * @param extraData Extra Arguments for custom Particle Effects extending this Class.
     */
    public EggGoopParticleEffect(Vec3 color, float scale, Vec3 dir, boolean mature, WaterHandling waterHandling, ExtraGoopData extraData) {
        super(color, scale, dir, mature, waterHandling, extraData);
    }

    @ApiStatus.Internal
    public EggGoopParticleEffect(Vec3 color, float scale, Vec3 dir, boolean mature, byte waterHandling, ExtraGoopData extraData) {
        super(color, scale, dir, mature, WaterHandling.values()[waterHandling], extraData);
    }

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.EGG_GOOP.get();
    }

    public static class Factory implements ParticleOptions.Deserializer<EggGoopParticleEffect>, IGoopEffectFactory {
        @Override
        public EggGoopParticleEffect fromCommand(ParticleType type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            Vec3 color = AbstractGoopParticleEffect.readVec3(reader);
            reader.expect(' ');
            float scale = reader.readFloat();
            reader.expect(' ');
            Vec3 dir = readVec3(reader);
            reader.expect(' ');
            boolean mature = reader.readBoolean();
            return new EggGoopParticleEffect(color, scale, dir, mature, WaterHandling.REPLACE_WITH_CLOUD_PARTICLE, new ExtraGoopData());
        }

        @Override
        public EggGoopParticleEffect fromNetwork(ParticleType type, FriendlyByteBuf buf) {
            return new EggGoopParticleEffect(readVec3(buf), buf.readFloat(), readVec3(buf), buf.readBoolean(),
                    buf.readEnum(WaterHandling.class), ExtraGoopData.read(buf));
        }

        @Override
        public Class<? extends AbstractGoopParticleEffect> getParticleEffectClass() {
            return EggGoopParticleEffect.class;
        }
    }
}
