package net.craftingrebind.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.craftingrebind.mixin.RecipeScreenAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.resources.Identifier;

public class CraftingRebindClient implements ClientModInitializer {
    private static final KeyMapping key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.craftingrebind.toggleRecipeMenu",
            InputConstants.Type.KEYBOARD, InputConstants.KEY_N,
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("craftingrebind", "binds"))
    ));

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((_, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractRecipeBookScreen<?> recipeBookScreen) {
                ScreenKeyboardEvents.allowKeyPress(screen).register((_, event) -> {
                    if (key.matches(event)) {
                        RecipeBookComponent<?> component = ((RecipeScreenAccessor) recipeBookScreen).getRecipeBookComponent();
                        if (component.isFocused()) return true; component.toggleVisibility();
                        recipeBookScreen.init(scaledWidth, scaledHeight); return false;
                    } return true;
                });
            }
        });
    }
}
