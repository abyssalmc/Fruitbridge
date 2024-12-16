package abyssalmc.fruit.mixin;

import abyssalmc.GlobalDataHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class AutosaveDisabler {
    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void disableSave(boolean suppressLogs, boolean flush, boolean force, CallbackInfo ci) {
        if (!GlobalDataHandler.getAutosaves()){
            MinecraftServer server = (MinecraftServer) (Object) this;
            if (server.isDedicated() == false) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§aAutosave cancelled."));
                ci.cancel();
            }
        }
    }
}
