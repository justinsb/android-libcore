package org.apache.harmony.xnet.provider.jsse;

import java.io.IOException;

import libcore.io.Streams;

import org.apache.harmony.xnet.provider.jsse.TlsExtension;

public class TlsExtensionGeneric extends TlsExtension {
    final byte[] data;

    public TlsExtensionGeneric(HandshakeIODataStream in, int type, int length)
            throws IOException {
        super(type, length);

        byte[] data = new byte[length];
        Streams.readFully(in, data);
        this.data = data;
    }

    public byte[] getData() {
        // TODO: Defensive copy?
        return data;
    }

    /**
     * Sends message
     * 
     * @param out
     */
    @Override
    public void send(HandshakeIODataStream out) {
        out.writeUint16(type);
        out.writeUint16(length);

        out.write(data);
    }

}
