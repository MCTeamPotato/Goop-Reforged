package absolutelyaya.goop.network.s2c;

import absolutelyaya.goop.api.ExtraGoopData;
import absolutelyaya.goop.api.emitter.GoopEmitterRegistry;
import absolutelyaya.goop.api.WaterHandling;
import absolutelyaya.goop.client.GoopClient;
import absolutelyaya.goop.particles.GoopDropParticleEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public class GoopPacketS2C {
    private final Vec3 position;
    private final int color;
    private final Vec3 velocity;
    private final float randomness;
    private final int amount;
    private final float size;
    private final boolean mature;
    private final boolean drip;
    private final boolean deform;
    private final boolean dev;
    private final WaterHandling waterHandling;
    private final boolean isOverridden;
    private final ResourceLocation particleEffectOverride;
    private final ExtraGoopData extraGoopData;

    public GoopPacketS2C(Vec3 position, int color, Vec3 velocity, float randomness, int amount, float size, boolean mature, boolean drip, boolean dev, boolean deform, WaterHandling waterHandling, boolean isOverridden, ResourceLocation particleEffectOverride, ExtraGoopData extraGoopData) {
        this.position = position;
        this.color = color;
        this.velocity = velocity;
        this.randomness = randomness;
        this.amount = amount;
        this.size = size;
        this.mature = mature;
        this.drip = drip;
        this.deform = deform;
        this.dev = dev;
        this.waterHandling = waterHandling;
        this.isOverridden = isOverridden;
        this.particleEffectOverride = particleEffectOverride;
        this.extraGoopData = extraGoopData;
    }

    public static void encode(GoopPacketS2C msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.position.x);
        buf.writeDouble(msg.position.y);
        buf.writeDouble(msg.position.z);
        buf.writeInt(msg.color);
        buf.writeDouble(msg.velocity.x);
        buf.writeDouble(msg.velocity.y);
        buf.writeDouble(msg.velocity.z);
        buf.writeFloat(msg.randomness);
        buf.writeInt(msg.amount);
        buf.writeFloat(msg.size);
        buf.writeBoolean(msg.mature);
        buf.writeBoolean(msg.drip);
        buf.writeBoolean(msg.deform);
        buf.writeBoolean(msg.dev);
        buf.writeEnum(msg.waterHandling);
        buf.writeBoolean(msg.isOverridden);
        if (msg.isOverridden){
            buf.writeResourceLocation(msg.particleEffectOverride);
            msg.extraGoopData.write(buf);
        }
    }

    public static GoopPacketS2C decode(FriendlyByteBuf buf) {
        Vec3 position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        int color = buf.readInt();
        Vec3 velocity = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        float randomness = buf.readFloat();
        int amount = buf.readInt();
        float size = buf.readFloat();
        boolean mature = buf.readBoolean();
        boolean drip = buf.readBoolean();
        boolean deform = buf.readBoolean();
        boolean dev = buf.readBoolean();
        WaterHandling waterHandling = buf.readEnum(WaterHandling.class);
        boolean isOverridden = buf.readBoolean();
        ResourceLocation particleEffectOverride = isOverridden ? buf.readResourceLocation() : null;
        ExtraGoopData extraGoopData;
        if (isOverridden) {
            try {
                extraGoopData = (ExtraGoopData) GoopEmitterRegistry.getExtraDataType(buf.readResourceLocation()).getMethod("read", FriendlyByteBuf.class).invoke(null, buf);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else {
            extraGoopData = new ExtraGoopData();
        }
        return new GoopPacketS2C(position, color, velocity, randomness, amount, size, mature, drip, deform, dev, waterHandling, isOverridden, particleEffectOverride, extraGoopData);
    }

    public static void handle(GoopPacketS2C msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (msg.dev && !GoopClient.getConfig().showDev) return;
            if (msg.mature && GoopClient.hideMature()) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            // Iterate over the amount and spawn particles
            for (int i = 0; i < msg.amount; i++) {
                Vec3 vel = msg.velocity.offsetRandom(mc.level.random, msg.randomness);
                if (msg.isOverridden) {
                    mc.level.addParticle(new GoopDropParticleEffect(Vec3.fromRGB24(msg.color), msg.size, msg.mature, msg.waterHandling, msg.particleEffectOverride, msg.extraGoopData),
                            msg.position.x, msg.position.y, msg.position.z, vel.x, vel.y, vel.z);
                } else {
                    mc.level.addParticle(new GoopDropParticleEffect(Vec3.fromRGB24(msg.color), msg.size, msg.mature, msg.waterHandling),
                            msg.position.x, msg.position.y, msg.position.z, vel.x, vel.y, vel.z);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
