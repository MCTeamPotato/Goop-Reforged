package absolutelyaya.goop.api;

import absolutelyaya.goop.api.emitter.GoopEmitterRegistry;

@FunctionalInterface
public interface GoopInitializer
{
	/**
	 * Register your Goop Emitters in here!
	 * @see absolutelyaya.goop.api.Examples
	 * @see GoopEmitterRegistry
	 */
	void registerGoopEmitters();
}
