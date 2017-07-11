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

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Connection.ConnectionSide;
import nz.co.lolnet.equity.entries.Packet;
import nz.co.lolnet.equity.packets.SPacketDisconnect;
import nz.co.lolnet.equity.util.EquityUtil;
import nz.co.lolnet.equity.util.LogHelper;

public class ConnectionManager {
	
	private final List<Connection> connections;
	
	public ConnectionManager() {
		this.connections = new ArrayList<Connection>();
	}
	
	public void addConnection(Connection connection) {
		if (getConnections() == null || connection == null) {
			return;
		}
		
		getConnections().add(connection);
		LogHelper.info(EquityUtil.getAddress(connection.getAddress()) + " -> Connected.");
	}
	
	public void setSocketAddress(Connection connection, SocketAddress socketAddress) {
		if (connection == null || socketAddress == null) {
			return;
		}
		
		connection.setSocketAddress(socketAddress);
		LogHelper.info(String.join(" ", EquityUtil.getAddress(connection.getClientChannel().localAddress()), "->", EquityUtil.getAddress(connection.getAddress())));
	}
	
	public void kickConnection(Connection connection, String reason) {
		if (connection == null) {
			return;
		}
		
		SPacketDisconnect disconnect = new SPacketDisconnect();
		disconnect.setReason(reason);
		disconnect.write(connection, new Packet(Unpooled.buffer()));
		LogHelper.info(String.join(" ", EquityUtil.getAddress(connection.getAddress()), "-> Kicked:", reason));
	}
	
	public void removeConnection(Connection connection) {
		if (getConnections() == null || connection == null) {
			return;
		}
		
		getConnections().remove(connection);
		LogHelper.info(String.join(" ", EquityUtil.getAddress(connection.getAddress()), "-> Disconnected."));
	}
	
	public Connection getConnection(Channel channel, ConnectionSide connectionSide) {
		for (Iterator<Connection> iterator = getConnections().iterator(); iterator.hasNext();) {
			Connection connection = iterator.next();
			if (connection != null && connection.isChannel(channel, connectionSide)) {
				return connection;
			}
		}
		return null;
	}
	
	public List<Connection> getConnections() {
		return connections;
	}
}