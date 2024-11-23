# Goop

最初作为 [ULTRACRAFT](https://github.com/absolutelyaya/ultracraft) 的一部分发布，Goop 现在是一个独立的库。它可以轻松实现史莱姆和飞溅视觉效果。自从它在
ULTRACRAFT 中首次亮相以来，已经进行了很多重要的改进。

## 未来计划的功能

我不确定什么时候会做这些，但这些是我一定想做的事情：

- [ ] 数据包支持
    - 使用数据包添加效果，而不必制作扩展模组
- [ ] 仅客户端支持
    - 使效果在不需要服务器的情况下也能工作
    - 添加一种客户端添加效果的方式；最好的情况是使用游戏内自定义 GUI
- [ ] Forge 移植
    - 如果能支持 *所有* 模组加载器，那就太酷了

# 这很酷，但我怎么使用它？

## 导入依赖

你可以自由地``include``这个库，这样用户就不需要单独下载它。

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation fg.deobf('com.github.MCTeamPotato:Goop-Reforged:1.20.1forge-1.0')
}
```

就是这么简单。现在你可以像使用其他粒子一样使用 Goop 粒子；或者我们做得更干净一些。

## 注册发射器
好，现在你已经导入了依赖，我们需要添加一个新的入口点。首先，创建一个实现 ``GoopInitalizer`` 接口的类。
然后将其注册到模组总线上，像这样：
```
@SubscribeEvent
public static void onCommonSetup(FMLCommonSetupEvent event) {
    new Examples().registerGoopEmitters();
}
```

 这就是该模组示例发射器注册的地方。 <br>
如果你只是想看看一些快速的示例发射器，可以查看 ``Examples`` 类。

既然我没有被一些快速且混乱的免费示例代码分散注意力，接着往下看：<br>
让我们把史莱姆做得更令人满意一些。为此，我们将首先注册一个 "伤害发射器"。每次史莱姆受到伤害时，这个发射器会喷出绿色的
Goop（有些例外）。

### 伤害发射器

前往你的 Goop 初始化器类。以下这段代码可能有点吓人，但一旦你理解它的工作原理，我相信你能轻松使用它。

```
GoopEmitterRegistry.registerEmitter(EntityType.SLIME, new DamageGoopEmitter<Slime>(
		(slime, data) -> 0x2caa3b,
		(slime, data) -> new Vector4f(0f, 0f, 0f, MathHelper.clamp(data.amount() / 8f, 0.25f, 2f)),
		(slime, data) -> data.source().isIn(TagRegistry.PHYSICAL) ? Math.round(MathHelper.clamp(data.amount() / 2f, 2f, 12f)) : 0,
		(slime, data) -> MathHelper.clamp(data.amount() / 4f, 0.25f, 1)
));
```

让我们一步一步地解释这段代码。这是调用 ``GoopEmitterRegistry#registerEmitter`` 方法。<br>
第一个参数是你要为其分配此发射器的 ``EntityType``。<br>
接下来是实际的发射器；由于我们需要一个伤害发射器，我们实例化了 ``DamageGoopEmitter<TargetEntityClass>``。<br>
最令人生畏的部分是发射器的参数；<br>
每个参数都是一个 ``BiFunction<>``。你将得到实体实例和 ``DamageData``，其中包含伤害量和来源。

1. 颜色<br>你可以连接一个方法，或者使用 Lambda 返回 RGB 颜色的 int 形式。
2. 速度<br>返回一个 ``Vector4f``；前 3 个值表示方向，第 4 个值用于添加随机性。<br>在这个示例中，Goop
   会完全随机地向四面八方飞散；伤害越高，速度越快。
3. 数量<br>返回一个整数，表示粒子的数量。在这个示例中，只有物理伤害会触发 Goop；也就是说，火焰或中毒伤害会被忽略。如果你想要这样，当然也可以。
4. 大小<br>最后，返回一个浮动数值，表示 Goop 的大小。在这个示例中，伤害越大，Goop 越大。

就这样，史莱姆在受到伤害时就会四散飞溅。太棒了！<br>
所有发射器类型的工作方式都非常相似，但我还是会解释其中的小区别。

### 死亡发射器

死亡发射器的 "data" 只有致命的伤害来源。

```
//这会导致雪傀儡在死亡时化成蓝色 Goop。
//由于粒子是模拟水的效果，它将在与实际水接触时消失。
GoopEmitterRegistry.registerEmitter(EntityType.SNOW_GOLEM, new DeathGoopEmitter<SnowGolem>(
		(snowGolem, data) -> 0x4690da,
		(snowGolem, data) -> new Vector4f(0f, 0f, 0f, 0.5f),
		(snowGolem, data) -> 2 + snowGolem.getRandom().nextInt(4),
		(snowGolem, data) -> 0.5f + snowGolem.getRandom().nextFloat() / 0.5f
).setWaterHandling(WaterHandling.REMOVE_PARTICLE));
```

水处理将在稍后进一步讨论；我只是不想从示例发射器中删除它，因为它适合这个效果。

### 降落发射器

回到史莱姆，这个发射器会让它们在降落时留下 Goop 飞溅。降落发射器的 "data" 是一个表示实体下落距离的浮动值。

```
//这会导致史莱姆在跳跃后降落时留下绿色 Goop 飞溅。
GoopEmitterRegistry.registerEmitter(EntityType.SLIME, new LandingGoopEmitter<Slime>(
		(slime, height) -> 0x2caa3b,
		(slime, height) -> new Vector4f(0f, -0f, 0f, 0.1f),
		(slime, height) -> 1,
		(slime, height) -> MathHelper.clamp(height / 4f, 0.25f, 1) * slime.getSize()
));
```

这个示例中唯一有趣的地方是，它使用实体的数据来决定 Goop 的大小。

### 投射物发射器

投射物发射器的数据是投射物的命中结果。

```
//当鸡蛋投掷到某物上时，它会留下...鸡蛋的 Goop。
GoopEmitterRegistry.registerProjectileEmitter(EntityType.EGG, new ProjectileHitGoopEmitter<ThrownEgg>(
		(egg, data) -> 0xffffff,
		(egg, data) -> {
			Vec3d vel = egg.getDeltaMovement();
			return new Vector4f((float)vel.x, (float)vel.y, (float)vel.z, 0f);
		},
		(egg, data) -> 1,
		(egg, data) -> 0.5f
).noDrip().setParticleEffectOverride(new ResourceLocation(Goop.MOD_ID, "egg_goop"), new ExtraGoopData()));
```

由于投射物实体不是生物实体，投射物发射器使用了不同的注册方法（``GoopEmitterRegistry#registerProjectileEmitter``）。<br>
"效果覆盖" 会在下面进一步说明。

## 高级功能

首先，我们不要从效果覆盖讲起。随着我们深入了解，内容会变得更复杂。

### "成人" 内容标记

此功能旨在为玩家提供禁用他们可能觉得令人不适或恶心的 VFX 的选择。在添加发射器时，请牢记这一点，并相应地标记它们。<br>
要将一个发射器标记为 "成人" 内容，请在实例化发射器后立即使用 ``.markMature()``。<br>
如果客户端更改设置，已经发射的粒子不会被回溯地审查或取消审查。不过，重新加入世界将立即移除所有现有的 Goop。

### "开发者" 内容标记

开发者发射器（例如所有示例发射器）只会为启用了 "显示开发者粒子" 客户端设置的玩家发射粒子。<br>
要将一个发射器标记为 "开发者" 内容，请在实例化发射器后立即使用 ``.markDev()``。
以下是将您提供的 Goop 文档翻译成中文并包含代码框的版本：

---

### 禁用滴水效果

要使一个发射器的 Goop 在覆盖天花板时不滴落，可以在实例化发射器后立即使用 `.noDrip()`。

### 禁用变形

要使一个发射器的 Goop 在覆盖墙壁或天花板时不变形，可以在实例化发射器后立即使用 `.noDeform()`。

### 水处理
目前，有三种方式可以处理 Goop 与水接触时的效果：
1. `REMOVE_PARTICLE`  
   默认行为：当 Goop 与水接触时，它会被移除。
2. `REPLACE_WITH_CLOUD_PARTICLE`  
   当 Goop 接触水时，会将其转换为同样颜色和大小的云粒子。
3. `IGNORE`  
   不做任何处理。

你可以在实例化发射器后使用 `.setWaterHandling(WaterHandling.X)` 来设置发射器的水处理类型。
### 效果覆盖
哦，这可能是该库中最复杂的功能，因此假设你已经有一定的自定义粒子效果经验。
首先，创建一个继承自 `GoopParticle` 的新粒子，并创建一个继承自 `GoopParticleEffect` 的新粒子效果。
**如果**你的自定义效果需要比普通 Goop 更多的数据，那么你还需要创建一个继承自 `ExtraGoopData`
的类。然后，你需要通过 `GoopEmitterRegistry#registerExtraDataType(ResourceLocation, Class)` 在 `GoopInitalizer` 中注册这个新的“额外数据类型”。
好了，现在你已经创建了自己的 Goop
粒子和一个计划使用它的发射器，在实例化发射器后加入 `.setParticleEffectOverride(new ResourceLocation("examplemod", "coolgoop"), new ExtraGoopData())`
。将 `ResourceLocation` 替换为你的粒子名称，并且 **如果**你需要为你的效果提供额外的参数，可以用你自己的 `ExtraGoopData`
类型实例来替换 `new ExtraGoopData()`。
**请记住，你的 Goop 粒子的构造函数必须与默认的 Goop 粒子构造函数完全相同。**
是的，这应该就是所有内容了！期待看到你用这些粒子 VFX 做的酷炫效果。

