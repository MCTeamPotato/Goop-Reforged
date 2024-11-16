package absolutelyaya.goop.particles;

import absolutelyaya.goop.client.GoopClient;
import absolutelyaya.goop.client.GoopConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class GoopStringParticle extends SpriteAAParticle {
    protected GoopStringParticle(ClientLevel world, Vec3 pos, SpriteSet spriteProvider, Vec3 color, float scale, boolean mature) {
        super(world, pos.x, pos.y - 0.25, pos.z, spriteProvider);
        gravity = random.nextFloat() * 0.25f + 0.1f;
        lifetime = random.nextInt(15) + 20;
        GoopConfig config = GoopClient.getConfig();
        color = mature && GoopClient.recolorMature() ? Vec3.fromRGB24(config.censorColor) : color;
        setColor((float) color.x(), (float) color.y(), (float) color.z());
        this.scale = this.scale.scale(scale);
        hasPhysics = true;
        alpha = 0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void move(double dx, double dy, double dz) {
        super.move(dx, dy, dz);
        if (!onGround)
            scale = scale.add(-0.001f, (float) -dy, -0.001f);
    }

    @Override
    public void tick() {
        super.tick();
        alpha = Mth.lerp((float) age / lifetime, 1.2f, 0f);
    }

    public static class Factory implements ParticleProvider<GoopStringParticleEffect> {
        protected final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(GoopStringParticleEffect parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new GoopStringParticle(world, new Vec3(x, y, z), spriteProvider, parameters.getColor(), parameters.getScale(), parameters.isMature());
        }
    }
}
