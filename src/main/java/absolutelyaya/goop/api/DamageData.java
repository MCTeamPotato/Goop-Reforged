package absolutelyaya.goop.api;

import absolutelyaya.goop.api.emitter.DamageGoopEmitter;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Contains Damage Source and amount for DamageGoopEmitters.
 * @see DamageGoopEmitter
 */
public record DamageData(DamageSource source, float amount) {}
