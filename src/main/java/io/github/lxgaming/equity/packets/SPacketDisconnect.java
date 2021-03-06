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

package io.github.lxgaming.equity.packets;

import io.github.lxgaming.equity.Equity;
import io.github.lxgaming.equity.entries.ProxyMessage;
import io.github.lxgaming.equity.text.Text;
import io.github.lxgaming.equity.util.PacketUtil;

import java.util.Objects;

public class SPacketDisconnect extends AbstractPacket {
    
    private Text reason;
    
    @Override
    public void read(ProxyMessage proxyMessage) {
        setReason(Text.builder().append(Text.of(PacketUtil.readString(proxyMessage.getByteBuf()))).build());
        proxyMessage.getConnection().getIdentity().ifPresent(identity -> {
            Equity.getInstance().getLogger().info("{} -> DISCONNECT {}", identity, getReason());
        });
    }
    
    @Override
    public void write(ProxyMessage proxyMessage) throws RuntimeException {
        Objects.requireNonNull(getReason());
        writePacketId(proxyMessage);
        PacketUtil.writeString(proxyMessage.getByteBuf(), getReason().toString());
        proxyMessage.getConnection().getClientChannel().writeAndFlush(proxyMessage);
    }
    
    public Text getReason() {
        return reason;
    }
    
    public void setReason(Text reason) {
        this.reason = reason;
    }
}