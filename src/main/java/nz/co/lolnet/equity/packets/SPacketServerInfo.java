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

package nz.co.lolnet.equity.packets;

import java.util.Objects;

import nz.co.lolnet.equity.entries.AbstractPacket;
import nz.co.lolnet.equity.entries.ProxyMessage;
import nz.co.lolnet.equity.entries.ServerMessage;

public class SPacketServerInfo extends AbstractPacket {
	
	private ServerMessage serverPing;
	
	@Override
	public void read(ProxyMessage proxyMessage) {
	}
	
	@Override
	public void write(ProxyMessage proxyMessage) throws RuntimeException {
		Objects.requireNonNull(getServerPing());
		writePacketId(proxyMessage);
		proxyMessage.getPacket().writeString(getServerPing().toString());
		proxyMessage.getConnection().getClientChannel().writeAndFlush(proxyMessage);
	}
	
	public ServerMessage getServerPing() {
		return serverPing;
	}
	
	public void setServerPing(ServerMessage serverPing) {
		this.serverPing = serverPing;
	}
}