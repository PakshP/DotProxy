package com.dotproxy.via;

import io.netty.channel.Channel;
import org.geysermc.mcprotocollib.network.tcp.TcpServer;
import org.geysermc.mcprotocollib.network.tcp.TcpServerChannelInitializer;

import static com.dotproxy.Globals.VIA_INITIALIZER;

public class DotProxyServerChannelInitializer extends TcpServerChannelInitializer {
    public static final Factory FACTORY = DotProxyServerChannelInitializer::new;

    public DotProxyServerChannelInitializer(final TcpServer server) {
        super(server);
    }

    @Override
    protected void initChannel(final Channel channel) throws Exception {
        super.initChannel(channel);
        VIA_INITIALIZER.serverViaChannelInitializer(channel);
    }
}
