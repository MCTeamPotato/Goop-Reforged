package absolutelyaya.goop.registries;

import absolutelyaya.goop.Goop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;

public class TagRegistry
{
	public static final TagKey<DamageType> PHYSICAL = TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Goop.MOD_ID, "physical"));
}
