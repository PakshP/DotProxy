package com.dotproxy.feature.gui.elements;

import com.dotproxy.feature.gui.Gui;
import com.dotproxy.feature.gui.Page;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface Slot {
    @Nullable ItemStack item();

    default void tick(Gui gui, Page page, int index) {}
}
