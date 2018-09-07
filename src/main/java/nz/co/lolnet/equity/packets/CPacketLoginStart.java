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

import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.ProxyMessage;
import nz.co.lolnet.equity.util.PacketUtil;
import nz.co.lolnet.equity.util.Toolbox;

public class CPacketLoginStart extends AbstractPacket {
    
    private String username;
    
    @Override
    public void read(ProxyMessage proxyMessage) {
        setUsername(PacketUtil.readString(proxyMessage.getByteBuf()));
        proxyMessage.getConnection().setUsername(getUsername());
        proxyMessage.getConnection().getAddress().ifPresent(address -> {
            Equity.getInstance().getLogger().info("{} -> LOGIN {}", Toolbox.getAddress(address), getUsername());
        });
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}