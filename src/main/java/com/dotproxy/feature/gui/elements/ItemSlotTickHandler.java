package com.dotproxy.feature.gui.elements;

import com.dotproxy.feature.gui.Gui;
import com.dotproxy.feature.gui.Page;

@FunctionalInterface
public interface ItemSlotTickHandler {
    void tick(ItemSlot slot, Gui gui, Page page, int index);
}
