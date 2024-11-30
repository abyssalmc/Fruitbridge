package abyssalmc.fruit.sound;

import abyssalmc.fruit.Fruit;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;

public class ModSounds {
    public static SoundEvent OSU = registerSoundEvent("place");


    private static SoundEvent registerSoundEvent(String name){
        Identifier id = Identifier.of(Fruit.MOD_ID, name);

        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
    public static void registerSounds(){
        Fruit.LOGGER.info("Registering sounds for " + Fruit.MOD_ID);
    }

}
