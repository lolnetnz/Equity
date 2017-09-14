/*
 * Copyright 2017 lolnet.co.nz
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

package nz.co.lolnet.equity.managers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Connection.ConnectionSide;
import nz.co.lolnet.equity.entries.Server;
import nz.co.lolnet.equity.handlers.ProxyChannelHandler;
import nz.co.lolnet.equity.util.EquityUtil;

public class ProxyManager {
	
	private ServerBootstrap serverBootstrap;
	
	public void startProxy() {
		try {
			EventLoopGroup eventLoopGroup;
			Class<? extends ServerSocketChannel> eventLoopGroupClass;
			if (Epoll.isAvailable() && Equity.getInstance().getConfig().isNativeTransport()) {
				eventLoopGroup = new EpollEventLoopGroup(Equity.getInstance().getConfig().getMaxThreads(), EquityUtil.getThreadFactory("Netty Epoll Thread #%d"));
				eventLoopGroupClass = EpollServerSocketChannel.class;
				Equity.getInstance().getLogger().info("Using Epoll Transport.");
			} else {
				eventLoopGroup = new NioEventLoopGroup(Equity.getInstance().getConfig().getMaxThreads(), EquityUtil.getThreadFactory("Netty IO Thread #%d"));
				eventLoopGroupClass = NioServerSocketChannel.class;
				Equity.getInstance().getLogger().info("Using NIO Transport.");
			}
			
			serverBootstrap = new ServerBootstrap()
					.group(eventLoopGroup)
					.channel(eventLoopGroupClass)
					.option(ChannelOption.SO_REUSEADDR, true)
					.childOption(ChannelOption.AUTO_READ, false)
					.childOption(ChannelOption.TCP_NODELAY, true)
					.childHandler(new ProxyChannelHandler(ConnectionSide.CLIENT));
			ChannelFuture channelFuture = getServerBootstrap().bind(Equity.getInstance().getConfig().getPort()).sync();
			Equity.getInstance().getLogger().info("Proxy listening on {}", EquityUtil.getAddress(channelFuture.channel().localAddress()));
			Equity.getInstance().setRunning(true);
			channelFuture.channel().closeFuture().sync();
		} catch (InterruptedException | RuntimeException ex) {
			Equity.getInstance().getLogger().error("Encountered an error processing {}::startProxy", getClass().getSimpleName(), ex);
		}
	}
	
	public void createServerConnection(Connection connection) {
		try {
			if (connection == null || connection.getClientChannel() == null || connection.getServerChannel() != null) {
				throw new IllegalArgumentException("Required arguments are invalid!");
			}
			
			if (!isProtocolSupported(connection.getProtocolVersion())) {
				Equity.getInstance().getLogger().warn("Failed to find server handling protocol {}", connection.getProtocolVersion());
				Equity.getInstance().getConnectionManager().disconnect(connection, Equity.getInstance().getMessages().getUnsupported());
				return;
			}
			
			Server server = getServer(connection.getProtocolVersion());
			if (server == null) {
				Equity.getInstance().getLogger().warn("Failed to find server handling protocol {}", connection.getProtocolVersion());
				Equity.getInstance().getConnectionManager().disconnect(connection, Equity.getInstance().getMessages().getUnavailable());
				return;
			}
			
			Equity.getInstance().getLogger().info("{} -> SERVER {}", connection.getIdentity(), server.getIdentity());
			Bootstrap bootstrap = new Bootstrap()
					.group(connection.getClientChannel().eventLoop())
					.channel(connection.getClientChannel().getClass())
					.option(ChannelOption.AUTO_READ, false)
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Equity.getInstance().getConfig().getConnectTimeout())
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ProxyChannelHandler(ConnectionSide.SERVER));
			
			ChannelFuture channelFuture = bootstrap.connect(server.getHost(), server.getPort());
			channelFuture.addListener((ChannelFuture future) -> {
				if (future.isSuccess()) {
					connection.setServerChannel(channelFuture.channel());
					for (Object object : connection.getPacketQueue()) {
						connection.getServerChannel().writeAndFlush(object).addListener(EquityUtil.getFutureListener(connection.getClientChannel()));
					}
					
					Equity.getInstance().getConnectionManager().clearPacketQueue(connection);
				} else {
					Equity.getInstance().getConnectionManager().disconnect(connection, Equity.getInstance().getMessages().getError());
				}
			});
		} catch (RuntimeException ex) {
			Equity.getInstance().getLogger().error("Encountered an error processing {}::createServerConnection", getClass().getSimpleName(), ex);
			Equity.getInstance().getConnectionManager().removeConnection(connection);
		}
	}
	
	private Server getServer(int protocolVersion) {
		List<Server> servers = Equity.getInstance().getConfig().getServers().stream()
				.filter(Objects::nonNull)
				.filter(server -> Objects.nonNull(server.getProtocolVersions()))
				.filter(server -> server.getProtocolVersions().contains(protocolVersion))
				.filter(server -> isAvailable(server.getIdentity(), server.getHost(), server.getPort()))
				.collect(Collectors.toCollection(ArrayList::new));
		
		if (!servers.isEmpty()) {
			return servers.get(new SecureRandom().nextInt(servers.size()));
		}
		
		return null;
	}
	
	private boolean isProtocolSupported(int protocolVersion) {
		return Equity.getInstance().getConfig().getServers().stream()
				.filter(Objects::nonNull)
				.map(server -> server.getProtocolVersions())
				.filter(Objects::nonNull)
				.anyMatch(supportedProtocols -> supportedProtocols.contains(protocolVersion));
	}
	
	private boolean isAvailable(String identity, String host, int port) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), Equity.getInstance().getConfig().getConnectTimeout());
			return true;
		} catch (IOException | RuntimeException ex) {
			Equity.getInstance().getLogger().warn("Server {} is not available!", identity);
			return false;
		}
	}
	
	public ServerBootstrap getServerBootstrap() {
		return serverBootstrap;
	}
}