package absolutelyaya.goop.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class ModMenu implements ModMenuApi
{
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return parent -> AutoConfig.getConfigScreen(GoopConfig.class, parent).get();
	}
}
