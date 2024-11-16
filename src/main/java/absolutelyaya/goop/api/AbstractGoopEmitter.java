package absolutelyaya.goop.api;

import absolutelyaya.goop.network.NetworkHandler;
import absolutelyaya.goop.network.s2c.GoopPacketS2C;
import absolutelyaya.goop.registries.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;
import org.joml.Vector4f;

public abstract class AbstractGoopEmitter<T extends Entity> implements IGoopEmitter {
    protected ResourceLocation particleEffectOverride;
    protected boolean mature = false, dev = false, drip = true, deform = true;
    protected WaterHandling waterHandling = WaterHandling.REPLACE_WITH_CLOUD_PARTICLE;
    protected ExtraGoopData extraGoopData = new ExtraGoopData();

    /**
     * Replace the Goop Puddle Particle effect with a different one.<br>
     * NOTE: The Effect HAS TO extend GoopParticleEffect and contain a Constructor taking the same arguments as the standard one!
     *
     * @param override The ResourceLocation of the replacement Particle Effect
     * @param data     Extra Data for the replacement Particle Effect. Override this class use it to supply any extra data your custom Goop particle needs.
     * @see absolutelyaya.goop.particles.GoopParticleEffect
     * @see absolutelyaya.goop.api.ExtraGoopData
     */
    public AbstractGoopEmitter<T> setParticleEffectOverride(ResourceLocation override, ExtraGoopData data) {
        this.particleEffectOverride = override;
        this.extraGoopData = data;
        return this;
    }

    /**
     * Marks this emitter's Particles as Mature Content. Clients can choose to replace "Mature" Particles colors with a Censor Color.
     */
    public AbstractGoopEmitter<T> markMature() {
        mature = true;
        return this;
    }

    /**
     * Marks this emitter as Dev Content. It will only emit particles if the "Show Dev Particles" Client Setting is turned on.
     */
    public AbstractGoopEmitter<T> markDev() {
        dev = true;
        return this;
    }

    /**
     * Disables dripping for ceiling goop caused by this emitter.
     */
    public AbstractGoopEmitter<T> noDrip() {
        drip = false;
        return this;
    }

    /**
     * Disables deformation for ceiling & wall goop caused by this emitter.
     */
    public AbstractGoopEmitter<T> noDeform() {
        deform = false;
        return this;
    }

    /**
     * Define how this emitter's particles should react on contact with water.
     * The Default is WaterHandling.REPLACE_WITH_CLOUD_PARTICLE
     */
    public AbstractGoopEmitter<T> setWaterHandling(WaterHandling handling) {
        this.waterHandling = handling;
        return this;
    }

    @ApiStatus.Internal
    protected void emitInternal(T entity, int color, Vector4f velocity, int amount, float scale, WaterHandling waterHandling) {
        //TODO: fetch whether the target client even has dev emitters enabled; if not, don't send any data.
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        Vec3 position = entity.position().add(0, entity.getBbHeight() / 2, 0).subtract(entity.getDeltaMovement().scale(0.2f));
        Vec3 velocityVec = new Vec3(velocity.x, velocity.y, velocity.z);
        float randomness = velocity.w;
        int amountInt = Math.max(amount, 0);
        float size = Math.max(scale, 0);

        buf.writeVector3f(position.toVector3f());
        buf.writeInt(color);
        buf.writeVector3f(velocityVec.toVector3f());
        buf.writeFloat(randomness);
        buf.writeInt(amountInt);
        buf.writeFloat(size);
        buf.writeBoolean(mature);
        buf.writeBoolean(drip);
        buf.writeBoolean(deform);
        buf.writeBoolean(dev);
        buf.writeEnum(waterHandling);
        boolean isOverridden = particleEffectOverride != null;
        buf.writeBoolean(isOverridden);
        if (isOverridden) {
            buf.writeResourceLocation(particleEffectOverride);
            extraGoopData.write(buf);
        }
        entity.level().players().forEach(p -> {
            if (p instanceof ServerPlayer serverPlayer) {
                NetworkHandler.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new GoopPacketS2C(
                                position,
                                color,
                                velocityVec,
                                randomness,
                                amount,
                                size,
                                mature,
                                drip,
                                deform,
                                dev,
                                waterHandling,
                                isOverridden,
                                particleEffectOverride,
                                extraGoopData
                        )
                );
            }
        });
    }
}
