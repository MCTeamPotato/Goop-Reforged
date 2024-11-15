package absolutelyaya.goop.particles;

import absolutelyaya.goop.api.ExtraGoopData;
import absolutelyaya.goop.api.WaterHandling;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public abstract class AbstractGoopParticleEffect implements ParticleOptions
{
	protected final Vec3 color;
	protected final float scale;
	protected final boolean mature;
	protected final ExtraGoopData extraGoopData;
	protected final WaterHandling waterHandling;
	protected boolean drip = true, deform = true;
	
	public AbstractGoopParticleEffect(Vec3 color, float scale, boolean mature, ExtraGoopData extraGoopData, WaterHandling waterHandling)
	{
		this.color = color;
		this.scale = Mth.clamp(scale, 0.01f, 4f);
		this.mature = mature;
		this.extraGoopData = extraGoopData;
		this.waterHandling = waterHandling;
	}
	
	public static Vec3 readVec3(StringReader reader) throws CommandSyntaxException
	{
		float f = reader.readFloat();
		reader.expect(' ');
		float g = reader.readFloat();
		reader.expect(' ');
		float h = reader.readFloat();
		return new Vec3(f, g, h);
	}
	
	public static Vec3 readVec3(FriendlyByteBuf buf)
	{
		return new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
	}
	
	@Override
	public void writeToNetwork(FriendlyByteBuf buf)
	{
		buf.writeFloat((float)this.color.x);
		buf.writeFloat((float)this.color.y);
		buf.writeFloat((float)this.color.z);
		buf.writeFloat(this.scale);
		buf.writeBoolean(this.mature);
		if(extraGoopData != null)
			extraGoopData.write(buf);
	}

	@Override
	public String writeToString()
	{
		return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f, %s",
				BuiltInRegistries.PARTICLE_TYPE.getId(this.getType()), this.color.x, this.color.y, this.color.z, this.scale, this.mature);
	}
	
	public Vec3 getColor() {
		return this.color;
	}
	
	public float getScale() {
		return this.scale;
	}
	
	public boolean isMature()
	{
		return mature;
	}
	
	public ExtraGoopData getExtraData()
	{
		return extraGoopData;
	}
	
	public WaterHandling getWaterHandling()
	{
		return waterHandling;
	}
	
	public AbstractGoopParticleEffect setDrip(boolean drip)
	{
		this.drip = drip;
		return this;
	}
	
	public boolean isDrip()
	{
		return drip;
	}
	
	public AbstractGoopParticleEffect setDeform(boolean deform)
	{
		this.deform = deform;
		return this;
	}
	
	public boolean isDeform()
	{
		return deform;
	}
}
