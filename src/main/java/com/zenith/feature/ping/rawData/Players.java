package com.zenith.feature.ping.rawData;

import java.util.List;

public record Players(
    int max,
    int online,
    List<Player> sample) { }
