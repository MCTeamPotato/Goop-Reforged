package absolutelyaya.goop.particles;

import absolutelyaya.goop.api.WaterHandling;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EggGoopParticle extends GoopParticle
{
	protected EggGoopParticle(ClientLevel world, double x, double y, double z, SpriteSet spriteProvider, Vec3 color, float scale, Vec3 dir, boolean mature, boolean drip, boolean deform, WaterHandling waterHandling)
	{
		super(world, x, y, z, spriteProvider, color, scale, dir, mature, drip, deform, waterHandling);
	}
	
	public static class Factory implements ParticleProvider<EggGoopParticleEffect>
	{
		protected final SpriteSet spriteProvider;
		
		public Factory(SpriteSet spriteProvider)
		{
			this.spriteProvider = spriteProvider;
		}
		
		@Nullable
		@Override
		public Particle createParticle(EggGoopParticleEffect parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
		{
			return new EggGoopParticle(world, x, y, z, spriteProvider, parameters.getColor(), parameters.getScale(), parameters.getDir(),
					parameters.isMature(), parameters.isDrip(), parameters.isDeform(), parameters.getWaterHandling());
		}
	}
}
