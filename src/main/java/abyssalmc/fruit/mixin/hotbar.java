package abyssalmc.fruit.mixin;

import abyssalmc.GlobalDataHandler;
import abyssalmc.fruit.Fruit;
import abyssalmc.fruit.command.mmt;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static abyssalmc.fruit.Fruit.distance;
import static abyssalmc.fruit.Fruit.sessionpb;

@Mixin(InGameHud.class)

public class hotbar {

    @Inject(method = "renderHotbar", at = @At(value = "HEAD"), cancellable = true)
    private void modifyHotbarTexture(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Identifier indicator = Identifier.of(Fruit.MOD_ID, "textures/indicator.png");
        Identifier itas = Identifier.of(Fruit.MOD_ID, "textures/itas.png");

        RenderSystem.enableBlend();

        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity p = mc.player;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        if (p != null) {
            if (GlobalDataHandler.getUtils()) {
                if (GlobalDataHandler.getDist()) {
                    int guiy = 0;
                    if (MinecraftClient.getInstance().interactionManager.getCurrentGameMode() != GameMode.CREATIVE){
                        guiy = -14;
                    }
                    context.drawText(textRenderer, "" + distance, context.getScaledWindowWidth() / 2 - 3 * ((String.valueOf(distance).length())), context.getScaledWindowHeight() - 35 + guiy, 0xffffff, true);
                }
            }

            if (Fruit.isTas) {
                context.drawTexture(itas, context.getScaledWindowWidth() / 2 - 72 - 1 + p.getInventory().selectedSlot * 20, context.getScaledWindowHeight() - 2 - 1, 0, 0, 1, 1, 1, 1);
                context.drawTexture(indicator, context.getScaledWindowWidth() / 2 - 13, context.getScaledWindowHeight() - 4, 0, 0, 1, 1, 1, 1);
            }
            if (Fruit.isAhk) {
                context.drawTexture(itas, context.getScaledWindowWidth() / 2 - 71 - 1 + p.getInventory().selectedSlot * 20, context.getScaledWindowHeight() - 2 - 1, 0, 0, 1, 1, 1, 1);
                context.drawTexture(indicator, context.getScaledWindowWidth() / 2 - 12, context.getScaledWindowHeight() - 4, 0, 0, 1, 1, 1, 1);
            }
            if (Fruit.isMmt) {
                context.drawTexture(itas, context.getScaledWindowWidth() / 2 - 71 - 1 + p.getInventory().selectedSlot * 20, context.getScaledWindowHeight() - 3 - 1, 0, 0, 1, 1, 1, 1);
                context.drawTexture(indicator, context.getScaledWindowWidth() / 2 - 11, context.getScaledWindowHeight() - 4, 0, 0, 1, 1, 1, 1);
            }
        }
        RenderSystem.disableBlend();
    }
}