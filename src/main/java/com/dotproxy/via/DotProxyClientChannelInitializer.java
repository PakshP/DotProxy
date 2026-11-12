package com.dotproxy.via;

import io.netty.channel.Channel;
import org.geysermc.mcprotocollib.network.tcp.TcpClientChannelInitializer;
import org.geysermc.mcprotocollib.network.tcp.TcpClientSession;

import static com.dotproxy.Globals.VIA_INITIALIZER;

public class DotProxyClientChannelInitializer extends TcpClientChannelInitializer {
    public static final Factory FACTORY = DotProxyClientChannelInitializer::new;
    private final TcpClientSession client;

    public DotProxyClientChannelInitializer(final TcpClientSession client, final boolean isTransferring) {
        super(client, isTransferring);
        this.client = client;
    }

    @Override
    public void initChannel(final Channel channel) throws Exception {
        super.initChannel(channel);
        VIA_INITIALIZER.clientViaChannelInitializer(channel);
    }
}
