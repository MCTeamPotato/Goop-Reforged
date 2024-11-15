package absolutelyaya.goop.particles;

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

public class GoopStringParticleEffect extends AbstractGoopParticleEffect
{
	public static final Codec<GoopStringParticleEffect> CODEC;

	static {
		CODEC = RecordCodecBuilder.create(
				(instance) -> instance.group(
								Vec3.CODEC.fieldOf("color").forGetter((effect) -> effect.color),
								Codec.FLOAT.fieldOf("scale").forGetter((effect) -> effect.scale),
								Codec.BOOL.fieldOf("mature").forGetter((effect) -> effect.mature))
						.apply(instance, GoopStringParticleEffect::new));
	}
	public GoopStringParticleEffect(Vec3 color, float scale, boolean mature)
	{
		super(color, scale, mature, null, WaterHandling.IGNORE);
	}
	
	@Override
	public ParticleType<?> getType()
	{
		return ParticleRegistry.GOOP_STRING.get();
	}
	
	public static class Factory implements ParticleOptions.Deserializer<GoopStringParticleEffect>
	{
		@Override
		public GoopStringParticleEffect fromCommand(ParticleType type, StringReader reader) throws CommandSyntaxException
		{
			reader.expect(' ');
			Vec3 color = AbstractGoopParticleEffect.readVec3(reader);
			reader.expect(' ');
			float size = reader.readFloat();
			reader.expect(' ');
			boolean mature = reader.readBoolean();
			return new GoopStringParticleEffect(color, size, mature);
		}
		
		@Override
		public GoopStringParticleEffect fromNetwork(ParticleType type, FriendlyByteBuf buf)
		{
			return new GoopStringParticleEffect(readVec3(buf), buf.readFloat(), buf.readBoolean());
		}
	}
}
