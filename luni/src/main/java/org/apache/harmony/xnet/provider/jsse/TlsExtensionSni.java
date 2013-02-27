package org.apache.harmony.xnet.provider.jsse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import libcore.io.Streams;

public class TlsExtensionSni extends TlsExtension {
    public static class SniName {
        public final int type;
        final byte[] name;

        public SniName(int type, byte[] name) {
            this.type = type;
            this.name = name;
        }

        public String getNameString() {
            return new String(name, android.Charsets.UTF_8);
        }
    }

    final List<SniName> names;

    public List<SniName> getNames() {
        return names;
    }

    /**
     * Creates inbound message
     * 
     * @param in
     * @param length
     * @throws IOException
     */
    public TlsExtensionSni(HandshakeIODataStream in, int type, int length)
            throws IOException {
        super(type, length);

        if (type != TlsExtension.EXTENSION_SNI) {
            throw new IllegalArgumentException();
        }

        int remaining = in.readUint16();
        if (remaining != (length - 2)) {
            TlsExtensions.fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect SNI extension");
        }

        List<SniName> names = new ArrayList<SniName>();
        while (remaining > 0) {
            int nameType = in.readUint8();
            int nameLength = in.readUint16();

            remaining -= 3 + nameLength;

            if (remaining < 0) {
                break;
            }

            byte[] name = new byte[nameLength];
            Streams.readFully(in, name);

            names.add(new SniName(nameType, name));
        }
        this.names = Collections.unmodifiableList(names);

        if (remaining != 0) {
            TlsExtensions.fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect SNI extension");
        }
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

        out.writeUint16(length - 2);

        for (int i = 0; i < names.size(); i++) {
            SniName name = names.get(i);
            out.writeUint8(name.type);
            byte[] data = name.name;
            out.writeUint16(data.length);
            out.write(data);
        }
    }

}
