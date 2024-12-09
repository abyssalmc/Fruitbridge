package abyssalmc.fruit.mixin;

import abyssalmc.fruit.Fruit;
import abyssalmc.fruit.command.mmt;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)

public class hotbar {

    @Inject(method = "renderHotbar", at = @At(value = "HEAD"), cancellable = true)
    private void modifyHotbarTexture(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Identifier indicator = Identifier.of(Fruit.MOD_ID, "textures/indicator.png");

        RenderSystem.enableBlend();

        if (Fruit.isTas) { context.drawTexture(indicator, 227, 266, 0, 0, 1, 1, 1, 1); }
        if (Fruit.isAhk) { context.drawTexture(indicator, 227, 265, 0, 0, 1, 1, 1, 1); }
        if (Fruit.isMmt) { context.drawTexture(indicator, 227, 264, 0, 0, 1, 1, 1, 1); }

        RenderSystem.disableBlend();


    }
}