package absolutelyaya.goop.network;

import absolutelyaya.goop.Goop;
import absolutelyaya.goop.network.s2c.GoopPacketS2C;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(Goop.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(packetId++, GoopPacketS2C.class, GoopPacketS2C::encode, GoopPacketS2C::decode, GoopPacketS2C::handle);
    }
}
