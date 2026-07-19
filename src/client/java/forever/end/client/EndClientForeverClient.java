package forever.end.client;

import com.mojang.blaze3d.platform.InputConstants;
import forever.end.client.ecf.ClientState;
import forever.end.client.ecf.CommandManager;
import forever.end.client.ecf.ConfigManager;
import forever.end.client.ecf.EcfConfig;
import forever.end.client.ecf.fx.InterfaceFx;
import forever.end.client.ecf.module.ModuleManager;
import forever.end.client.ecf.screen.AuthScreen;
import forever.end.client.ecf.screen.ClickGuiScreen;
import forever.end.client.ecf.screen.HudEditorScreen;
import forever.end.client.ecf.screen.MainMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.lwjgl.glfw.GLFW;

public class EndClientForeverClient implements ClientModInitializer {
    private static KeyMapping clickGuiKey;
    private static KeyMapping hudEditorKey;

    @Override
    public void onInitializeClient() {
        EcfConfig.load();
        ModuleManager.init();
        ConfigManager.init();
        CommandManager.init();
        // Гарантированное сохранение конфига при выходе из игры.
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.saveLocal());

        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.forever-endclient.clickgui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.forever-endclient"));

        hudEditorKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.forever-endclient.hudeditor",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.forever-endclient"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Screen s = client.screen;
            // Обязательный гейт авторизации + наше главное меню вместо ванильного TitleScreen.
            if (s instanceof TitleScreen) {
                client.setScreen(ClientState.authed ? new MainMenuScreen() : new AuthScreen());
                return;
            }
            while (clickGuiKey.consumeClick()) {
                if (!ClientState.authed) continue;
                if (!InterfaceFx.clickGuiKeyEnabled()) continue;
                Screen cur = client.screen;
                if (cur == null) {
                    client.setScreen(new ClickGuiScreen(null));
                } else if (cur instanceof ClickGuiScreen) {
                    cur.onClose();
                } else if (cur instanceof MainMenuScreen) {
                    client.setScreen(new ClickGuiScreen(cur));
                }
            }
            while (hudEditorKey.consumeClick()) {
                if (!ClientState.authed) continue;
                Screen cur = client.screen;
                if (cur instanceof HudEditorScreen) {
                    cur.onClose();
                } else {
                    client.setScreen(new HudEditorScreen(cur));
                }
            }
        });
    }
}
