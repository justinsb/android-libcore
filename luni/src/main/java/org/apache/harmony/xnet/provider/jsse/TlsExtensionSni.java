package org.apache.harmony.xnet.provider.jsse;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLEngine;

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

    /**
     * Creates inbound message
     * 
     * @param in
     * @param length
     * @throws IOException
     */
    public TlsExtensionSni(HandshakeIODataStream in, int type, int length) throws IOException {
        super(type, length);

        if (type != TlsExtension.EXTENSION_SNI) {
            throw new IllegalArgumentException();
        }

        int remaining = in.readUint16();
        if (remaining > length) {
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

    public List<SniName> getNames() {
        return names;
    }
}
