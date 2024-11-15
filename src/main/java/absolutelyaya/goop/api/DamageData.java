package absolutelyaya.goop.api;

import net.minecraft.world.damagesource.DamageSource;

/**
 * Contains Damage Source and amount for DamageGoopEmitters.
 * @see absolutelyaya.goop.api.DamageGoopEmitter
 */
public record DamageData(DamageSource source, float amount) {}
