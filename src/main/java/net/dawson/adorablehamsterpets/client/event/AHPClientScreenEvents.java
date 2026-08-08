package net.dawson.adorablehamsterpets.client.event;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.dawson.adorablehamsterpets.AdorableHamsterPetsClient;
import net.dawson.adorablehamsterpets.client.gui.widgets.AnnouncementIconWidget;
import net.dawson.adorablehamsterpets.config.Configs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Handles client-side screen events for dynamically injecting the announcement icon widget.
 */
public final class AHPClientScreenEvents {
    private static final int ICON_SIZE = 16;

    public static void register() {
        // Register a listener for the post-initialization event.
        ScreenEvents.AFTER_INIT.register(AHPClientScreenEvents::onScreenInitPost);
    }

    private static void onScreenInitPost(net.minecraft.client.Minecraft client, Screen screen, int width, int height) {
        // --- 1. Pre-condition Checks ---
        if (!Configs.AHP_UI.enableWidgetIcon.get() || Configs.AHP_UI.serverDisableAnnouncements) {
            return;
        }
        if (AdorableHamsterPetsClient.getPendingNotifications().isEmpty()) {
            return;
        }
        if (!(screen instanceof AbstractContainerScreen<?>)) {
            return;
        }

        // --- 2. Create and Add the Widget ---
        // The widget's initial position is temporary; its render method will calculate the true position.
        AnnouncementIconWidget icon = new AnnouncementIconWidget(
                0, 0, ICON_SIZE, ICON_SIZE,
                button -> { },
                screen // Pass the parent screen to the widget
        );

        Screens.getWidgets(screen).add(icon);
    }
}
