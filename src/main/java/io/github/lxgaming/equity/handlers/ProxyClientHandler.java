/*
 * Copyright 2017 Alex Thomson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lxgaming.equity.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.github.lxgaming.equity.Equity;
import io.github.lxgaming.equity.entries.Connection;
import io.github.lxgaming.equity.entries.Protocol;
import io.github.lxgaming.equity.entries.ProxyMessage;
import io.github.lxgaming.equity.managers.ConnectionManager;
import io.github.lxgaming.equity.managers.PacketManager;
import io.github.lxgaming.equity.util.Toolbox;

@ChannelHandler.Sharable
public class ProxyClientHandler extends ChannelInboundHandlerAdapter {
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = new Connection();
        connection.setClientChannel(ctx.channel());
        connection.setState(Protocol.State.HANDSHAKE);
        connection.setActive(true);
        connection.setServer("Unknown");
        ConnectionManager.addConnection(connection);
        ctx.channel().attr(Toolbox.getConnectionKey()).set(connection);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Connection connection = ctx.channel().attr(Toolbox.getConnectionKey()).get();
        if (connection == null || connection.getState() == null || !connection.isActive()) {
            return;
        }
        
        if (msg instanceof ByteBuf) {
            connection.getServerChannel().writeAndFlush(msg);
            return;
        }
        
        if (msg instanceof ProxyMessage) {
            ProxyMessage proxyMessage = (ProxyMessage) msg;
            proxyMessage.setDirection(Protocol.Direction.SERVERBOUND);
            PacketManager.process(proxyMessage);
            if (connection.getServerChannel() == null) {
                ConnectionManager.addPacketQueue(connection, proxyMessage);
                return;
            }
            
            connection.getServerChannel().writeAndFlush(proxyMessage);
            return;
        }
        
        throw new UnsupportedOperationException("Unsupported message received!");
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = ctx.channel().attr(Toolbox.getConnectionKey()).get();
        if (connection == null) {
            return;
        }
        
        ConnectionManager.removeConnection(connection);
    }
    
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Connection connection = ctx.channel().attr(Toolbox.getConnectionKey()).get();
        if (connection == null || connection.getClientChannel() == null || connection.getServerChannel() == null) {
            return;
        }
        
        if (connection.getClientChannel().isWritable()) {
            connection.getServerChannel().config().setAutoRead(true);
        } else {
            connection.getServerChannel().config().setAutoRead(false);
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) throws Exception {
        Equity.getInstance().getLogger().error("Exception caught in {}", getClass().getSimpleName(), throwable);
    }
    
    public static String getName() {
        return "proxy_client";
    }
}