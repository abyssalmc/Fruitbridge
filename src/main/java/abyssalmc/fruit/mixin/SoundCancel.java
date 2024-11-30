package abyssalmc.fruit.mixin;

import abyssalmc.GlobalDataHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class SoundCancel {
    @Inject(at = @At("HEAD"), method = "playSound", cancellable = true)
    private void cancelPlaceSound(PlayerEntity source, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed, CallbackInfo ci) {
        if (GlobalDataHandler.getPlaceSounds() && (sound.value().getId().getPath().contains(".place") || sound.value().getId().getPath().contains(".bucket") || sound.value().getId().getPath().contains(".break") || sound.value().getId().getPath().contains(".broke") || sound.value().getId().getPath().contains(".destroy"))){
            ci.cancel();
        }
    }
}