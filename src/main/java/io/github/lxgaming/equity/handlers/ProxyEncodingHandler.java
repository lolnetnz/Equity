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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.github.lxgaming.equity.entries.ProxyMessage;
import io.github.lxgaming.equity.util.PacketUtil;

public class ProxyEncodingHandler extends MessageToByteEncoder<ProxyMessage> {
    
    @Override
    protected void encode(ChannelHandlerContext ctx, ProxyMessage msg, ByteBuf out) throws Exception {
        int length = msg.getByteBuf().readableBytes();
        int varIntSize = PacketUtil.getVarIntSize(out, length);
        if (varIntSize > 3) {
            throw new IllegalArgumentException("Unable to fit " + length + " into " + 3);
        }
        
        out.ensureWritable(varIntSize + length);
        PacketUtil.writeVarInt(out, length);
        out.writeBytes(msg.getByteBuf());
        msg.getByteBuf().release();
    }
    
    public static String getName() {
        return "proxy_encoder";
    }
}