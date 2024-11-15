package absolutelyaya.goop.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class SpriteAAParticle extends TextureSheetParticle
{
	protected final SpriteSet spriteProvider;
	
	protected Vec3 scale;
	
	protected SpriteAAParticle(ClientLevel world, double x, double y, double z, SpriteSet spriteProvider)
	{
		super(world, x, y, z);
		float s = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;
		this.scale = new Vec3(s, s, s);
		this.spriteProvider = spriteProvider;
		sprite = spriteProvider.get(random);
	}
	
	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta)
	{
		Vec3 camPos = camera.getPosition();
		Vec3 dir = new Vec3(x, y, z).subtract(camPos).normalize();
			float f = (float)(Mth.lerp(tickDelta, this.xo, this.x) - camPos.x());
		float g = (float)(Mth.lerp(tickDelta, this.yo, this.y) - camPos.y());
		float h = (float)(Mth.lerp(tickDelta, this.zo, this.z) - camPos.z());
		
		Vec3[] Vec3ds = new Vec3[]{
				new Vec3(-1.0F, -1.0F, 0.0F),
				new Vec3(-1.0F, 1.0F, 0.0F),
				new Vec3(1.0F, 1.0F, 0.0F),
				new Vec3(1.0F, -1.0F, 0.0F)};
		
		for(int k = 0; k < 4; ++k)
		{
			Vec3 Vec3d = Vec3ds[k];
			Vec3d = Vec3d.yRot((float)Math.atan2(dir.x, dir.z));
			Vec3d = Vec3d.multiply(scale.x(), scale.y(), scale.z());
			Vec3ds[k] = Vec3d.add(f, g, h);
		}
		
		int n = this.getLightColor(tickDelta);
		vertexConsumer.vertex(Vec3ds[0].x(), Vec3ds[0].y(), Vec3ds[0].z()).uv(getU1(), getV1()).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(n).endVertex();
		vertexConsumer.vertex(Vec3ds[1].x(), Vec3ds[1].y(), Vec3ds[1].z()).uv(getU1(), getV0()).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(n).endVertex();
		vertexConsumer.vertex(Vec3ds[2].x(), Vec3ds[2].y(), Vec3ds[2].z()).uv(getU0(), getV0()).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(n).endVertex();
		vertexConsumer.vertex(Vec3ds[3].x(), Vec3ds[3].y(), Vec3ds[3].z()).uv(getU0(), getV1()).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(n).endVertex();
	}
}
