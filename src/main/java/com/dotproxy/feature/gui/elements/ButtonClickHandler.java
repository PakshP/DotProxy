package com.dotproxy.feature.gui.elements;

import com.dotproxy.feature.gui.ContainerClick;
import com.dotproxy.feature.gui.Gui;
import com.dotproxy.feature.gui.Page;

@FunctionalInterface
public interface ButtonClickHandler {
    void accept(Button button, Gui gui, Page page, ContainerClick containerClick);
}
