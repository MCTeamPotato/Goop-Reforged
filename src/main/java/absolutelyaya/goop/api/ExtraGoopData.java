package absolutelyaya.goop.api;

import absolutelyaya.goop.Goop;
import absolutelyaya.goop.api.emitter.GoopEmitterRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * This Class purely exists for devs to extend and use for extra arguments needed for custom Particle Effects based on Goop.
 */
public class ExtraGoopData
{
	public static Codec<ExtraGoopData> getCodec()
	{
		return Codec.EMPTY.codec().comapFlatMap((coordinates) -> null, (data) -> null);
	}
	
	/**
	 * Reads this ExtraGoopData from the given PacketByteBuffer
	 */
	public static ExtraGoopData read(FriendlyByteBuf buf)
	{
		return null;
	}
	
	/**
	 * When creating a new ExtraGoopData Type, override this with your unique Type Identifier and register it in the GoopEmitterRegistry.<br>
	 * This will be used to get the correct packet reading method.
	 * @see GoopEmitterRegistry#registerExtraDataType(ResourceLocation, Class)
	 */
	public static ResourceLocation getExtraDataType()
	{
		return new ResourceLocation(Goop.MOD_ID, "default");
	}
	
	/**
	 * Writes this ExtraGoopData to the given PacketByteBuffer.
	 */
	public void write(FriendlyByteBuf buf)
	{
		buf.writeResourceLocation(getExtraDataType());
	}
}
