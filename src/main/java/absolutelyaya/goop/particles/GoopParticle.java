package absolutelyaya.goop.particles;

import absolutelyaya.goop.api.WaterHandling;
import absolutelyaya.goop.client.GoopClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class GoopParticle extends SurfaceAlignedParticle
{
	static Queue<GoopParticle> GOOP_QUEUE = new ArrayDeque<>();
	
	private final Vec3 color;
	private final float size;
	private final float normalAlpha;
	private final int appearTicks;
	final boolean mature, drip;
	final WaterHandling waterHandling;
	float rain;
	
	protected GoopParticle(ClientLevel world, double x, double y, double z, SpriteSet spriteProvider, Vec3 color, float scale, Vec3 dir, boolean mature, boolean drip, boolean deform, WaterHandling waterHandling)
	{
		super(world, x, y, z, spriteProvider, color, scale, dir, deform);
		this.lifetime = config.permanent ? Integer.MAX_VALUE : 200 + random.nextInt(100);
		this.alpha = Math.min(random.nextFloat() + 0.5f, 1);
		this.color = mature && GoopClient.recolorMature() ? Vec3.fromRGB24(config.censorColor) : color;
		this.quadSize = 0;
		this.size = scale;
		this.normalAlpha = alpha;
		this.appearTicks = random.nextInt(4) + 3;
		this.mature = mature;
		this.drip = drip;
		this.waterHandling = waterHandling;
		GOOP_QUEUE.add(this);
		if(GOOP_QUEUE.size() > config.goopCap)
			GOOP_QUEUE.remove().remove();
	}
	
	@Override
	public ParticleRenderType getRenderType()
	{
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		//scale / alpha animations
		if(age <= appearTicks)
			quadSize = Mth.clampedLerp(0f, size, ((float)age / appearTicks));
		else if(age >= lifetime - 60 && !config.permanent)
		{
			quadSize = Mth.clampedLerp(size, size * 0.5f, (age - (lifetime - 60)) / 60f);
			alpha = Mth.clampedLerp(normalAlpha, 0f, (age - (lifetime - 60)) / 60f);
		}
		else
		{
			//Rain cleaning
			if(GoopClient.getConfig().rainCleaning  && waterHandling != WaterHandling.IGNORE &&
					level.isRaining() && level.canSeeSky(new BlockPos((int)x, (int)y, (int)z)))
			{
				rain = rain + 1f / 100f;
				quadSize = Mth.clampedLerp(size, size * 1.25f, rain / 10f);
				alpha = Mth.clampedLerp(normalAlpha, 0f, rain / 10f);
				if(rain > 10f)
					remove();
			}
		}
		//Ceiling Drips
		if(drip && dir.y() < 0 && random.nextInt(120) == 0)
			level.addParticle(new GoopStringParticleEffect(color, 0.25f, mature),
					x + random.nextFloat() * quadSize - quadSize / 2f, y, z + random.nextFloat() * quadSize - quadSize / 2f,
					0, 0, 0);
		//Fluid handling
		if(level.getFluidState(new BlockPos((int)x, (int)y, (int)z)).is(FluidTags.LAVA))
			remove();
		if(waterHandling == WaterHandling.IGNORE)
			return;
		if(level.isWaterAt(new BlockPos((int)x, (int)y, (int)z)))
		{
			switch(waterHandling)
			{
				case REMOVE_PARTICLE -> remove();
				case REPLACE_WITH_CLOUD_PARTICLE ->
				{
					level.addParticle(new DustParticleOptions(color.toVector3f(), quadSize), x, y, z,
							random.nextFloat() * 0.1f, random.nextFloat() * 0.1f, random.nextFloat() * 0.1f);
					remove();
				}
			}
		}
	}
	
	@Override
	public void remove()
	{
		super.remove();
		GOOP_QUEUE.remove(this);
	}
	
	public static void removeAll()
	{
		new ArrayList<>(GOOP_QUEUE).forEach(GoopParticle::remove);
	}
	
	public static class Factory implements ParticleProvider<GoopParticleEffect>
	{
		protected final SpriteSet spriteProvider;
		
		public Factory(SpriteSet spriteProvider)
		{
			this.spriteProvider = spriteProvider;
		}
		
		@Nullable
		@Override
		public Particle createParticle(GoopParticleEffect parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
		{
			return new GoopParticle(world, x, y, z, spriteProvider, parameters.getColor(), parameters.getScale(), parameters.getDir(), parameters.isMature(),
					parameters.isDrip(), parameters.isDeform(), parameters.getWaterHandling());
		}
	}
}
