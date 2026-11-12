package com.dotproxy.mc.enchantment;

import com.dotproxy.mc.RegistryData;

public record EnchantmentData(
    int id,
    String name,
    int maxLevel
) implements RegistryData { }
