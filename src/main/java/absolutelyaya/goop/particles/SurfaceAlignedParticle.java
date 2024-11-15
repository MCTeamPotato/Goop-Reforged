package absolutelyaya.goop.particles;

import absolutelyaya.goop.client.GoopClient;
import absolutelyaya.goop.client.GoopConfig;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SurfaceAlignedParticle extends TextureSheetParticle
{
	static final GoopConfig config;
	
	private final List<Boolean> faceShouldRender = new ArrayList<>();
	private final List<Vec3> verts = new ArrayList<>();
	private final List<Vec2> uvs = new ArrayList<>();
	private final List<Float> maxDeform = new ArrayList<>();
	protected final SpriteSet spriteProvider;
	protected final Vec3 dir;
	
	protected float deformation;
	float targetSize;
	boolean isFancy, deforms;
	
	protected SurfaceAlignedParticle(ClientLevel world, double x, double y, double z, SpriteSet spriteProvider,
									 Vec3 color, float scale, Vec3 dir, boolean deforms)
	{
		super(world, x, y, z);
		this.targetSize = scale;
		this.quadSize = this.random.nextFloat() * scale * 0.5f + scale * 0.25f;
		this.spriteProvider = spriteProvider;
		sprite = spriteProvider.get(random);
		gravity = 0;
		roll = config.randomRot ? random.nextFloat() * 360 : 0;
			setColor((float)color.x(), (float)color.y(), (float)color.z());
		
		isFancy = config.fancyGoop;
		this.deforms = deforms;
		
		this.dir = new Vec3((float)Math.round(dir.x), (float)Math.round(dir.y), (float)Math.round(dir.z));
		boolean b = dir.x != 0;
		if(dir.y != 0)
		{
			if(b)
				remove();
			b = true;
		}
		if(dir.z != 0 && b)
			remove();
		
		if(removed)
		{
			this.quadSize = 0;
			return;
		}
		
		Vec3 modDir = new Vec3(dir.x() == 0 ? 1 : 0, dir.y() == 0 ? 1 : 0, dir.z() == 0 ? 1 : 0);
		float subdivisions = isFancy ? Math.max(targetSize, 1) : 1;
		for(int vy = 0; vy <= subdivisions; vy++) //vertexY
		{
			for (int vx = 0; vx <= subdivisions; vx++) //vertexX
			{
				Vec3 vert;
				if(dir.y != 0)
					vert = new Vec3(modDir.x() * vx / subdivisions, modDir.y(), modDir.z() * vy / subdivisions);
				else
					vert = new Vec3(modDir.x() * vx / subdivisions, modDir.y() * vy / subdivisions, modDir.z() * vx / subdivisions);
				vert = vert.add(dir.scale(random.nextFloat() / 50)); //fight Z-Fighting
				verts.add(vert);
				faceShouldRender.add(true);
				uvs.add(new Vec2(Mth.lerp(vx / subdivisions, getU0(), getU1()), Mth.lerp(vy / subdivisions, getV0(), getV1())));
				if(dir.y == 0)
					maxDeform.add(random.nextFloat());
				else
					maxDeform.add(random.nextBoolean() ? random.nextFloat() * 0.25f * targetSize : 0);
			}
		}
	}
	
	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta)
	{
		boolean debug = config.goopDebug && !Minecraft.getInstance().isPaused();
		if(verts.size() == 0)
			return;
		
		Vec3 camPos = camera.getPosition();
		float f = (float)(Mth.lerp(tickDelta, this.xo, this.x) - camPos.x());
		float g = (float)(Mth.lerp(tickDelta, this.yo, this.y) - camPos.y());
		float h = (float)(Mth.lerp(tickDelta, this.zo, this.z) - camPos.z());
		
		Vec3 dir = this.dir;
		
		List<Vec3> verts = new ArrayList<>();
		Vec3 modDir = new Vec3(dir.x() == 0 ? 1 : 0, dir.y() == 0 ? 1 : 0, dir.z() == 0 ? 1 : 0);
		this.verts.forEach(i ->
		{
			i = i.subtract(new Vec3((float)(modDir.x() * 0.5), (float)(modDir.y() * 0.5), (float)(modDir.z() * 0.5)));
			verts.add(i);
		});
		
		AtomicInteger atomicInt = new AtomicInteger();
		for (int i = 0; i < verts.size(); i++)
		{
			Vec3 v = verts.get(i);
			//random rotation
			v = v.xRot((float)(dir.x() * roll));
			v = v.yRot((float)(dir.y() * roll));
			v = v.zRot((float)(dir.z() * roll));
			//deformation
			if(deforms && !(this.dir.y() > 0) && isFancy)
				v = v.subtract(new Vec3(0, deformation * maxDeform.get(atomicInt.get()), 0));
			v = v.scale(quadSize);
			verts.set(i, v.add(f, g, h));
			atomicInt.getAndIncrement();
		}
		
		float targetSize = isFancy ? Math.max(this.targetSize, 1) : 1;
		
		for (int y = 1, vi = 0; y < (int)targetSize + 1; y++, vi++)
		{
			for (int x = 1; x < (int)targetSize + 1; x++, vi++)
			{
				Vec3[] faceVerts = new Vec3[] {verts.get(vi), verts.get((int)(vi + targetSize + 1)),
						verts.get((int)(vi + targetSize + 2)), verts.get(vi + 1)};
				
				boolean render = !isFancy || faceShouldRender.get(vi);
				
				if(isFancy && dir.y() > 0 && faceShouldRender.get(vi))
				{
					//calculate face center
					Vec3 faceCenter = faceVerts[0];
					faceCenter = faceCenter.add(faceVerts[1]);
					faceCenter = faceCenter.add(faceVerts[2]);
					faceCenter = faceCenter.add(faceVerts[3]);
					faceCenter = faceCenter.scale(0.25f);
					//check if position of this face is attached to a valid surface
					Vec3 v = camPos.add(faceCenter);
					render = isValidPos(v);
					if(!render)
						faceShouldRender.set(vi, false); //so faces don't reappear after being removed
					
					if(debug)
					{
						//face normal, emitted from center
						level.addParticle(ParticleTypes.FLAME,
								camPos.x + faceCenter.x(), camPos.y + faceCenter.y() + 0.1, camPos.z + faceCenter.z(),
								0, 0.05, 0);
					}
					
					if(config.wrapToEdges && this.targetSize >= 2)
					{
						for (int i = 0; i < faceVerts.length; i++)
						{
							Vec3 mv = faceVerts[i];
							mv = mv.add(camPos);
							if(!isValidPos(mv))
								mv = moveToBlockEdge(mv);
							faceVerts[i] = mv.subtract(camPos);
						}
					}
				}
				
				if(render)
				{
					int brightness = getLightColor(tickDelta);
					//face
					vertexConsumer.vertex(faceVerts[0].x(), faceVerts[0].y(), faceVerts[0].z()).uv(uvs.get(vi).x, uvs.get(vi).y).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(brightness).endVertex();
					vertexConsumer.vertex(faceVerts[1].x(), faceVerts[1].y(), faceVerts[1].z()).uv(uvs.get((int)(vi + targetSize + 1)).x, uvs.get((int)(vi + targetSize + 1)).y).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(brightness).endVertex();
					vertexConsumer.vertex(faceVerts[2].x(), faceVerts[2].y(), faceVerts[2].z()).uv(uvs.get((int)(vi + targetSize + 2)).x, uvs.get((int)(vi + targetSize + 2)).y).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(brightness).endVertex();
					vertexConsumer.vertex(faceVerts[3].x(), faceVerts[3].y(), faceVerts[3].z()).uv(uvs.get(vi + 1).x, uvs.get(vi + 1).y).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(brightness).endVertex();
					//backface
					vertexConsumer.vertex(faceVerts[3].x(), faceVerts[3].y(), faceVerts[3].z()).uv(uvs.get(vi + 1).x, uvs.get(vi + 1).y).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(brightness).endVertex();
					vertexConsumer.vertex(faceVerts[2].x(), faceVerts[2].y(), faceVerts[2].z()).uv(uvs.get((int)(vi + targetSize + 2)).x, uvs.get((int)(vi + targetSize + 2)).y).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(brightness).endVertex();
					vertexConsumer.vertex(faceVerts[1].x(), faceVerts[1].y(), faceVerts[1].z()).uv(uvs.get((int)(vi + targetSize + 1)).x, uvs.get((int)(vi + targetSize + 1)).y).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(brightness).endVertex();
					vertexConsumer.vertex(faceVerts[0].x(), faceVerts[0].y(), faceVerts[0].z()).uv(uvs.get(vi).x, uvs.get(vi).y).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(brightness).endVertex();
				}
				verts.set(vi, faceVerts[0]);
				verts.set((int)(vi + targetSize + 1), faceVerts[1]);
				verts.set((int)(vi + targetSize + 2), faceVerts[2]);
				verts.set(vi + 1, faceVerts[3]);
			}
		}
		
		if(debug)
		{
			//goop Vertices, colored based on UVs
			for (int i = 0; i < verts.size(); i++)
			{
				Vec3 vertex = verts.get(i);
				Vec2 uv = uvs.get(i);
				level.addParticle(new DustParticleOptions(new Vector3f(uv.x, uv.y, 0f), 0.5f),
						camPos.x + vertex.x(), camPos.y + vertex.y() + 0.1, camPos.z + vertex.z(),
						0, 0.05, 0);
			}
			//goop Center
			level.addParticle(new DustParticleOptions(new Vector3f(1f, 1f, 1f), 1f), this.x, this.y, this.z,
					0, 0.25, 0);
		}
	}
	
	boolean isValidPos(Vec3 pos)
	{
		BlockPos blockPos = BlockPos.containing(pos);
		VoxelShape shape = level.getBlockState(blockPos).getCollisionShape(level, blockPos);
		if(!shape.isEmpty() && shape.bounds().move(blockPos).contains(pos))
			return false;
		Vec3 attachedPos = new Vec3(pos.x - dir.x() * 0.065f, pos.y - dir.y() * 0.065f, pos.z - dir.z() * 0.065f);
		BlockPos attachedBlockPos = BlockPos.containing(attachedPos);
		VoxelShape attachedShape = level.getBlockState(attachedBlockPos).getCollisionShape(level, attachedBlockPos);
		return !attachedShape.isEmpty() && attachedShape.bounds().move(attachedBlockPos).contains(attachedPos);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(!isValidPos(new Vec3(x, y, z)))
			remove();
		if(deforms)
			deformation = (float)age / lifetime;
	}
	
	private Vec3 moveToBlockEdge(Vec3 vert)
	{
		Vec3 dir = vert.subtract(x, y, z).normalize().scale(0.33);
		return new Vec3(Math.round(vert.x() - dir.x), vert.y(), Math.round(vert.z() - dir.z));
	}
	
	@Override
	protected int getLightColor(float tint)
	{
		BlockPos blockPos = BlockPos.containing(this.x, this.y, this.z);
		return LevelRenderer.getLightColor(this.level, blockPos);
	}
	
	static {
		config = GoopClient.getConfig();
	}
}
