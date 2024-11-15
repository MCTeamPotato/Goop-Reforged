package absolutelyaya.goop.mixin;

import absolutelyaya.goop.api.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity
{
	public LivingEntityMixin(EntityType<?> type, Level world)
	{
		super(type, world);
	}
	
	@Inject(method = "hurt", at = @At(value = "RETURN"))
	void onDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
	{
		if(level().isClientSide)
			return;
		Optional<List<DamageGoopEmitter<?>>> emitters = GoopEmitterRegistry.getDamageEmitters((EntityType<? extends LivingEntity>)getType());
		if(emitters.isEmpty())
			return;
		for (DamageGoopEmitter emitter : emitters.get())
			emitter.emit((LivingEntity)(Object)this, new DamageData(source, amount));
	}
	
	@Inject(method = "die", at = @At(value = "RETURN"))
	void onDeath(DamageSource damageSource, CallbackInfo ci)
	{
		if(level().isClientSide)
			return;
		Optional<List<DeathGoopEmitter<?>>> emitters = GoopEmitterRegistry.getDeathEmitters((EntityType<? extends LivingEntity>)getType());
		if(emitters.isEmpty())
			return;
		for (DeathGoopEmitter emitter : emitters.get())
			emitter.emit((LivingEntity)(Object)this, damageSource);
	}
	
	@Override
	public void resetFallDistance()
	{
		if (level().isClientSide || fallDistance < 0.05)
		{
			super.resetFallDistance();
			return;
		}
		Optional<List<LandingGoopEmitter<?>>> emitters = GoopEmitterRegistry.getLandingEmitters((EntityType<? extends LivingEntity>) getType());
		if (emitters.isEmpty())
		{
			super.resetFallDistance();
			return;
		}
		for (LandingGoopEmitter emitter : emitters.get())
			emitter.emit((LivingEntity) (Object) this, fallDistance);
		super.resetFallDistance();
	}
}
