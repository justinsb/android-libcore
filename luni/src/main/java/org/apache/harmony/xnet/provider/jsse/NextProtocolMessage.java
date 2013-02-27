/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.xnet.provider.jsse;

import java.io.IOException;

import libcore.io.Streams;

/**
 * 
 * Represents Next Protocol message (handshake message 67)
 * 
 */
public class NextProtocolMessage extends Message {

    final byte[] protocol;
    final int paddingLength;

    /**
     * Creates outbound message
     * 
     * @param bytes
     */
    public NextProtocolMessage(byte[] protocol) {
        this.protocol = protocol;
        this.paddingLength = 32 - ((protocol.length + 2) % 32);
        this.length = 1 + protocol.length + 1 + paddingLength;
    }

    /**
     * Creates inbound message
     * 
     * @param in
     * @param length
     * @throws IOException
     */
    public NextProtocolMessage(HandshakeIODataStream in, int length)
            throws IOException {
        int remaining = length;

        if (remaining < 1) {
            fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect NextProtocol");
        }

        int selectedProtocolLength = in.readUint8();
        remaining -= (1 + selectedProtocolLength);

        if (remaining < 0) {
            fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect NextProtocol");
        }

        byte[] selectedProtocol = new byte[selectedProtocolLength];
        Streams.readFully(in, selectedProtocol);

        // Padding length _should_ be 32 - ((len(selected_protocol) + 2) % 32)
        int paddingLength = in.readUint8();
        remaining -= 1 + paddingLength;

        if (remaining != 0) {
            fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect NextProtocol");
        }

        in.skip(paddingLength);

        this.protocol = selectedProtocol;
        this.paddingLength = paddingLength;
    }

    @Override
    public void send(HandshakeIODataStream out) {
        out.writeUint8(protocol.length);
        out.write(protocol);
        out.writeUint8(paddingLength);
        if (paddingLength != 0) {
            byte[] padding = new byte[paddingLength];
            for (int i = 0; i < paddingLength; i++) {
                // Arbitrary choice of padding byte
                padding[i] = 1;
            }
            out.write(padding);
        }
    }

    /**
     * Returns message type
     * 
     * @return
     */
    @Override
    public int getType() {
        return Handshake.NEXT_PROTOCOL;
    }

    public byte[] getProtocol() {
        return protocol;
    }

    public int getPaddingLength() {
        return paddingLength;
    }

}
