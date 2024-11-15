package absolutelyaya.goop.event;

import absolutelyaya.goop.Goop;
import absolutelyaya.goop.api.WaterHandling;
import absolutelyaya.goop.particles.GoopDropParticleEffect;
import absolutelyaya.goop.particles.GoopParticle;
import absolutelyaya.goop.particles.GoopParticleEffect;
import absolutelyaya.goop.registries.KeybindRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.logging.Level;

@Mod.EventBusSubscriber(modid = Goop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvent {
    static boolean clearPressed;

    @SubscribeEvent
    public static void cleanParticle(TickEvent.ClientTickEvent event) {
        while (KeybindRegistry.CLEAR_GOOP.consumeClick() && !clearPressed) {
            GoopParticle.removeAll();
            clearPressed = true;
        }
        while (KeybindRegistry.CLEAR_GOOP.consumeClick()) {} //remove stored presses
        clearPressed = KeybindRegistry.CLEAR_GOOP.isDown();
    }

//    @SubscribeEvent
//    public static void summonParticle(PlayerInteractEvent.RightClickBlock event){
//        if (event.getLevel() instanceof ClientLevel level){
//            Minecraft mc = Minecraft.getInstance();
//            Vec3 vel = Vec3.ZERO.offsetRandom(mc.level.random, 1);
//            BlockPos blockPos = event.getPos();
//            level.addParticle(new GoopDropParticleEffect(
//                        Vec3.fromRGB24(2927163),
//                        1,
//                        false,
//                        WaterHandling.REPLACE_WITH_CLOUD_PARTICLE),
//                    blockPos.getX(),
//                    blockPos.getY(),
//                    blockPos.getZ(),
//                    vel.x,
//                    vel.y,
//                    vel.z
//            );
//        }
//    }
}
