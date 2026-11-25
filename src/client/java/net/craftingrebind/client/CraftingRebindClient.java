package net.craftingrebind.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.craftingrebind.mixin.RecipeBookComponentInvoker;
import net.craftingrebind.mixin.RecipeScreenAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.resources.Identifier;

public class CraftingRebindClient implements ClientModInitializer {
    private static final KeyMapping.Category bindCategory = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("craftingrebind", "binds"));

    private static final KeyMapping recipeBookKey = registerAndGetKey("toggleRecipeBook", InputConstants.KEY_N);
    private static final KeyMapping craftableOnlyKey = registerAndGetKey("toggleCraftableOnly", InputConstants.KEY_V);
    private static final KeyMapping cycleCategoryKey = registerAndGetKey("cycleCategoryTabs", InputConstants.KEY_B);
    private static final KeyMapping focusSearchKey = registerAndGetKey("focusSearchBar", InputConstants.KEY_SLASH);

    private static KeyMapping registerAndGetKey(String name, int key) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.craftingrebind." + name, InputConstants.Type.KEYBOARD, key, bindCategory));
    }

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((_, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractRecipeBookScreen<?> recipeBookScreen) {
                ScreenKeyboardEvents.allowKeyPress(screen).register((_, event) -> {
                    RecipeBookComponent<?> component = ((RecipeScreenAccessor) recipeBookScreen).getRecipeBookComponent();
                    for (GuiEventListener child : component.children()) {
                        if (component.isVisible() && child instanceof EditBox box && box.isFocused()) return true;
                    }

                    if (recipeBookKey.matches(event)) {
                        component.toggleVisibility(); recipeBookScreen.init(scaledWidth, scaledHeight); return false;
                    }

                    if (!component.isVisible()) return true;

                    if (craftableOnlyKey.matches(event)) {
                        ((RecipeBookComponentInvoker) component).invokeToggleFiltering();
                        recipeBookScreen.init(scaledWidth, scaledHeight); return false;
                    }

                    if (cycleCategoryKey.matches(event)) {
                        for (GuiEventListener child : component.children()) {
                            if (child instanceof TabNavigationBar bar) {
                                int nextTab = bar.children().indexOf(bar.currentTabButton()) + 1;
                                bar.selectTab(nextTab % bar.getTabs().size(), true); break;
                            }
                        } return false;
                    }

                    if (focusSearchKey.matches(event)) {
                        for (GuiEventListener child : component.children()) {
                            if (child instanceof EditBox box) {
                                component.setFocused(box); recipeBookScreen.setFocused(component); break;
                            }
                        } return false;
                    }

                    return true;
                });
            }
        });
    }
}
