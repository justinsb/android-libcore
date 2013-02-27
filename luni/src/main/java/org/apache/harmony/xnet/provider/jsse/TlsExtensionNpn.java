package org.apache.harmony.xnet.provider.jsse;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.harmony.xnet.provider.jsse.TlsExtensionNpn.NpnProtocol;

import libcore.io.Streams;

public class TlsExtensionNpn extends TlsExtension {
    public static class NpnProtocol {
        static final NpnProtocol[] EMPTY_ARRAY = new NpnProtocol[0];

        public static final NpnProtocol HTTP_1_1 = new NpnProtocol("http/1.1");
        public static final NpnProtocol SPDY_2 = new NpnProtocol("spdy/2");
        public static final NpnProtocol SPDY_3 = new NpnProtocol("spdy/3");

        final byte[] name;

        public NpnProtocol(byte[] name) {
            this.name = name;
        }

        public NpnProtocol(String name) {
            this(name.getBytes(android.Charsets.US_ASCII));
        }

        public String getNameString() {
            return new String(name, android.Charsets.US_ASCII);
        }
    }

    final NpnProtocol[] protocols;

    /**
     * Creates outbound message
     */
    public TlsExtensionNpn(NpnProtocol[] protocols) {
        super(TlsExtension.EXTENSION_NPN, computeLength(protocols));
        this.protocols = protocols;
    }

    private static int computeLength(NpnProtocol[] protocols) {
        int length = 0;
        if (protocols != null) {
            for (NpnProtocol protocol : protocols) {
                length += 1 + protocol.name.length;
            }
        }
        return length;
    }

    /**
     * Creates inbound message
     * 
     * @param in
     * @param length
     * @throws IOException
     */
    public TlsExtensionNpn(HandshakeIODataStream in, int type, int length)
            throws IOException {
        super(type, length);

        if (type != TlsExtension.EXTENSION_NPN) {
            throw new IllegalArgumentException();
        }

        if (length == 0) {
            this.protocols = NpnProtocol.EMPTY_ARRAY;
        } else {
            int remaining = length;
            List<NpnProtocol> protocols = new ArrayList<NpnProtocol>();
            while (remaining > 0) {
                int nameLength = in.readUint8();

                remaining -= 1 + nameLength;

                if (remaining < 0) {
                    break;
                }

                byte[] name = new byte[nameLength];
                Streams.readFully(in, name);

                protocols.add(new NpnProtocol(name));
            }
            this.protocols = protocols
                    .toArray(new NpnProtocol[protocols.size()]);

            if (remaining != 0) {
                TlsExtensions.fatalAlert(AlertProtocol.DECODE_ERROR,
                        "DECODE ERROR: incorrect NPN extension");
            }
        }
    }

    public NpnProtocol[] getProtocols() {
        // TODO: Defensive copy?
        return protocols;
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

        for (int i = 0; i < protocols.length; i++) {
            byte[] data = protocols[i].name;
            out.writeUint8(data.length);
            out.write(data);
        }
    }

}
