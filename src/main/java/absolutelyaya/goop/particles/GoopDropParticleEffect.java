package absolutelyaya.goop.particles;

import absolutelyaya.goop.api.ExtraGoopData;
import absolutelyaya.goop.api.WaterHandling;
import absolutelyaya.goop.registries.ParticleRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

public class GoopDropParticleEffect extends AbstractGoopParticleEffect {
    ResourceLocation effectOverride;
    public static final Codec<GoopDropParticleEffect> CODEC;

    static {
        CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                                Vec3.CODEC.fieldOf("color").forGetter((effect) -> effect.color),
                                Codec.FLOAT.fieldOf("scale").forGetter((effect) -> effect.scale),
                                Codec.BOOL.fieldOf("mature").forGetter((effect) -> effect.mature),
                                Codec.BYTE.fieldOf("waterHandling").forGetter((effect) -> (byte) effect.waterHandling.ordinal()))
                        .apply(instance, GoopDropParticleEffect::new));
    }

    @ApiStatus.Internal
    public GoopDropParticleEffect(Vec3 color, float scale, boolean mature, byte waterHandling) {
        super(color, scale, mature, null, WaterHandling.values()[waterHandling]);
    }

    public GoopDropParticleEffect(Vec3 color, float scale, boolean mature, WaterHandling waterHandling) {
        super(color, scale, mature, null, waterHandling);
    }

    public GoopDropParticleEffect(Vec3 color, float scale, boolean mature, WaterHandling waterHandling, ResourceLocation effectOverride, ExtraGoopData extraData) {
        super(color, scale, mature, extraData, waterHandling);
        this.effectOverride = effectOverride;
    }

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.GOOP_DROP.get();
    }

    public ResourceLocation getEffectOverride() {
        return effectOverride;
    }

    public static class Factory implements ParticleOptions.Deserializer<GoopDropParticleEffect> {
        @Override
        public GoopDropParticleEffect fromCommand(ParticleType type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            Vec3 color = AbstractGoopParticleEffect.readVec3(reader);
            reader.expect(' ');
            float size = reader.readFloat();
            reader.expect(' ');
            boolean mature = reader.readBoolean();
            return new GoopDropParticleEffect(color, size, mature, WaterHandling.REPLACE_WITH_CLOUD_PARTICLE);
        }

        @Override
        public GoopDropParticleEffect fromNetwork(ParticleType type, FriendlyByteBuf buf) {
            Vec3 color = readVec3(buf);
            float scale = buf.readFloat();
            boolean mature = buf.readBoolean();
            WaterHandling waterHandling = buf.readEnum(WaterHandling.class);
            if (buf.readBoolean())
                return new GoopDropParticleEffect(color, scale, mature, waterHandling, buf.readResourceLocation(), ExtraGoopData.read(buf));
            return new GoopDropParticleEffect(color, scale, mature, waterHandling);
        }
    }
}
