package absolutelyaya.goop.particles;

import absolutelyaya.goop.Goop;
import absolutelyaya.goop.api.ExtraGoopData;
import absolutelyaya.goop.api.IGoopEffectFactory;
import absolutelyaya.goop.api.WaterHandling;
import absolutelyaya.goop.client.GoopClient;
import absolutelyaya.goop.client.GoopConfig;
import absolutelyaya.goop.registries.ParticleRegistry;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class GoopDropParticle extends TextureSheetParticle
{
	protected final SpriteSet spriteProvider;
	protected final Vec3 color;
	protected final boolean mature, drip, deform;
	final float rotSpeed;
	final float totalScale;
	final ResourceLocation effectOverride;
	final ExtraGoopData extraData;
	final WaterHandling waterHandling;
	
	protected GoopDropParticle(ClientLevel clientWorld, Vec3 pos, Vec3 vel, SpriteSet spriteProvider, Vec3 color, float scale, boolean mature, boolean drip, boolean deform, WaterHandling waterHandling, ResourceLocation effectOverride, ExtraGoopData extraData)
	{
		super(clientWorld, pos.x, pos.y, pos.z);
		GoopConfig config = GoopClient.getConfig();
		color = mature && GoopClient.recolorMature() ? Vec3.fromRGB24(config.censorColor) : color;
		setColor((float)color.x(), (float)color.y(), (float)color.z());
		this.color = color;
		this.quadSize = scale - (scale > 1 ? 1.25f * (scale / 2) : 0f);
		totalScale = scale;
		this.spriteProvider = spriteProvider;
		this.mature = mature;
		this.drip = drip;
		this.deform = deform;
		this.effectOverride = effectOverride;
		this.extraData = extraData;
		sprite = spriteProvider.get(random);
		gravity = 1 + scale / 2;
		lifetime = 300;
		setParticleSpeed(random.nextFloat() * 0.5 - 0.25, random.nextFloat() * 0.5, random.nextFloat() * 0.5 - 0.25);
		hasPhysics = true;
		this.waterHandling = waterHandling;
		
		rotSpeed = (random.nextFloat() - 0.5f) * 0.25f;
		
		if(vel.distanceTo(Vec3.ZERO) > 0)
			setParticleSpeed(vel.x, vel.y, vel.z);
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
		oRoll = roll;
		roll += rotSpeed;
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
					level.addParticle(new DustParticleOptions(color.toVector3f(), totalScale * 2.5f), x, y, z,
							random.nextFloat() * 0.1f, random.nextFloat() * 0.1f, random.nextFloat() * 0.1f);
					remove();
				}
			}
		}
	}
	
	ParticleType<GoopParticleEffect> getEffect(ResourceLocation override)
	{
		if(override == null)
			return ParticleRegistry.GOOP.get();
		ResourceKey<ParticleType<?>> registryKey = ResourceKey.create(Registries.PARTICLE_TYPE, override);
		Optional<Holder.Reference<ParticleType<?>>> output = BuiltInRegistries.PARTICLE_TYPE.asLookup().get(registryKey);
		return (ParticleType<GoopParticleEffect>)output.orElseThrow(() -> new ResourceLocationException(String.format("ResourceLocation '%s' is not a Valid Particle Type", override))).value();
	}
	
	Constructor<? extends AbstractGoopParticleEffect> getConstructor(ParticleType<?> type)
	{
		try
		{
			return ((IGoopEffectFactory)type.getDeserializer()).getParticleEffectClass().getConstructor(Vec3.class, float.class, Vec3.class, boolean.class, WaterHandling.class, ExtraGoopData.class);
		}
		catch (NoSuchMethodException e)
		{
			Goop.LOGGER.error("Required Goop Particle Effect Constructor not found:\n\b" + e.getMessage());
			return null;
		}
	}
	
	void nextParticle(BlockPos pos, Vec3 dir)
	{
		VoxelShape shape = level.getBlockState(pos.subtract(new Vec3i(0, 0, 0))).getCollisionShape(level, pos);
		if(!shape.isEmpty())
		{
			dir = dir.normalize();
			
			float height = (float)shape.max(Direction.Axis.Y);
			Vec3 offset = new Vec3(0.01, 0.01, 0.01);
			offset = offset.add(dir.x < 0 ? 0 : 1, dir.y < 0 ? 0 : height, dir.z < 0 ? 0 : 1);
			
			try
			{
				if(dir.y != 0)
					level.addParticle(getConstructor(getEffect(effectOverride))
											  .newInstance(color, totalScale * 2.5f, dir, mature, waterHandling, extraData).setDrip(drip).setDeform(deform),
							x, pos.getY() + dir.y * offset.y, z,
							0, 0, 0);
				else if(dir.x != 0)
					level.addParticle(getConstructor(getEffect(effectOverride))
											  .newInstance(color, totalScale * 2.5f, dir, mature, waterHandling, extraData).setDrip(drip).setDeform(deform),
							pos.getX() + dir.x * offset.x, y, z,
							0, 0, 0);
				else if(dir.z != 0)
					level.addParticle(getConstructor(getEffect(effectOverride))
											  .newInstance(color, totalScale * 2.5f, dir, mature, waterHandling, extraData).setDrip(drip).setDeform(deform),
							x, y, pos.getZ() + dir.z * offset.z,
							0, 0, 0);
			}
			catch (InvocationTargetException | InstantiationException | IllegalAccessException e)
			{
				Goop.LOGGER.error(e.getMessage());
			}
		}
	}
	
	@Override
	public void move(double dx, double dy, double dz)
	{
		if (this.hasPhysics && (dx != 0.0 || dy != 0.0 || dz != 0.0) && dx * dx + dy * dy + dz * dz < Mth.square(100.0))
		{
			Iterator<VoxelShape> it = level.getBlockCollisions(null, getBoundingBox().expandTowards(new Vec3(dx, dy, dz))).iterator();
			VoxelShape closest = null;
			float closestDist = Float.MAX_VALUE;
			Vec3 pos = new Vec3(x, y, z);
			while(it.hasNext())
			{
				VoxelShape shape = it.next();
				Optional<Vec3> closestPoint = shape.closestPointTo(pos);
				if(closestPoint.isEmpty())
					continue;
				float dist = (float)pos.distanceTo(closestPoint.get());
				if(dist < closestDist)
				{
					closest = shape;
					closestDist = dist;
				}
			}
			if(closest != null)
			{
				Vec3 point = closest.bounds().getCenter();
				Vec3 vec3d = Entity.collideBoundingBox(null, new Vec3(dx, dy, dz), this.getBoundingBox(), this.level, List.of());
				Vec3 diff = vec3d.subtract(new Vec3(dx, dy, dz)).normalize();
				
				nextParticle(BlockPos.containing(point.x, point.y, point.z), diff);
				remove();
			}
		}
		super.move(dx, dy, dz);
	}
	
	public static class Factory implements ParticleProvider<GoopDropParticleEffect>
	{
		protected final SpriteSet spriteProvider;
		
		public Factory(SpriteSet spriteProvider)
		{
			this.spriteProvider = spriteProvider;
		}
		
		@Nullable
		@Override
		public Particle createParticle(GoopDropParticleEffect parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ)
		{
			return new GoopDropParticle(world, new Vec3(x, y, z), new Vec3(velocityX, velocityY, velocityZ),
					spriteProvider, parameters.getColor(), parameters.getScale(), parameters.isMature(), parameters.isDrip(), parameters.isDeform(), parameters.getWaterHandling(),
					parameters.getEffectOverride(), parameters.getExtraData());
		}
	}
}
