package absolutelyaya.goop.mixin;

import absolutelyaya.goop.api.emitter.GoopEmitterRegistry;
import absolutelyaya.goop.api.emitter.ProjectileHitGoopEmitter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(Projectile.class)
public abstract class ProjectileEntityMixin extends Entity
{
	public ProjectileEntityMixin(EntityType<?> type, Level world)
	{
		super(type, world);
	}
	
	@Inject(method = "onHitEntity", at = @At(value = "RETURN"))
	void onEntityHit(EntityHitResult hit, CallbackInfo ci)
	{
		if(level().isClientSide)
			return;
		onHit(hit);
	}
	@Inject(method = "onHitBlock", at = @At(value = "RETURN"))
	void onBlockHit(BlockHitResult hit, CallbackInfo ci)
	{
		if(level().isClientSide)
			return;
		onHit(hit);
	}
	
	void onHit(HitResult hit)
	{
		Optional<List<ProjectileHitGoopEmitter<?>>> emitters = GoopEmitterRegistry.getProjectileHitEmitters((EntityType<? extends LivingEntity>)getType());
		if(emitters.isEmpty())
			return;
		for (ProjectileHitGoopEmitter emitter : emitters.get())
			emitter.emit((Projectile)(Object)this, hit);
	}
}
