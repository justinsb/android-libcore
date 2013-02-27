package org.apache.harmony.xnet.provider.jsse;

import java.io.IOException;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import org.apache.harmony.xnet.provider.jsse.TlsExtension;

import com.google.common.collect.Lists;

public class TlsExtensions {
    public static final TlsExtensions EMPTY = new TlsExtensions(
            TlsExtension.EMPTY_ARRAY);

    final int length;
    final TlsExtension[] extensions;

    /**
     * Creates outbound message
     * 
     * @param in
     * @param length
     * @throws IOException
     */
    public TlsExtensions(TlsExtension[] extensions) {
        this.extensions = extensions;
        int length = 0;
        if (extensions.length != 0) {
            length = 2;
            for (int i = 0; i < extensions.length; i++) {
                length += 4 + extensions[i].length;
            }
        }
        this.length = length;
    }

    /**
     * Creates inbound message
     * 
     * @param in
     * @param length
     * @throws IOException
     */
    public TlsExtensions(HandshakeIODataStream in, int length)
            throws IOException {
        this.length = in.readUint16();

        int remaining = this.length;
        if (remaining > length) {
            fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect TlsExtensions");
        }

        if (remaining == 0) {
            this.extensions = TlsExtension.EMPTY_ARRAY;
        } else {
            List<TlsExtension> extensions = Lists.newArrayList();

            while (remaining > 0) {
                int extensionType = in.readUint16();
                int extensionDataLength = in.readUint16();

                remaining -= 4;
                remaining -= extensionDataLength;

                if (remaining < 0) {
                    break;
                }

                TlsExtension extension;

                switch (extensionType) {
                case TlsExtension.EXTENSION_SNI:
                    extension = new TlsExtensionSni(in, extensionType,
                            extensionDataLength);
                    break;

                case TlsExtension.EXTENSION_NPN:
                    extension = new TlsExtensionNpn(in, extensionType,
                            extensionDataLength);
                    break;

                default:
                    extension = new TlsExtensionGeneric(in, extensionType,
                            extensionDataLength);
                    break;
                }
                extensions.add(extension);
            }

            this.extensions = (TlsExtension[]) extensions
                    .toArray(new TlsExtension[extensions.size()]);
        }

        if (remaining != 0) {
            fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect TlsExtensions");
        }
    }

    /**
     * Sends fatal alert
     * 
     * @param description
     * @param reason
     */
    static void fatalAlert(byte description, String reason) {
        throw new AlertException(description, new SSLHandshakeException(reason));
    }

    public TlsExtension findExtension(int type) {
        for (TlsExtension extension : extensions) {
            if (extension.type == type) {
                return extension;
            }
        }
        return null;
    }

    public TlsExtensionSni findExtensionSni() {
        return (TlsExtensionSni) findExtension(TlsExtension.EXTENSION_SNI);
    }

    public TlsExtensionNpn findExtensionNpn() {
        return (TlsExtensionNpn) findExtension(TlsExtension.EXTENSION_NPN);
    }

    /**
     * Sends message
     * 
     * @param out
     */
    public void send(HandshakeIODataStream out) {
        if (length == 0) {
            return;
        }

        out.writeUint16(length - 2);
        for (int i = 0; i < extensions.length; i++) {
            extensions[i].send(out);
        }
    }

    public TlsExtensions add(TlsExtension add) {
        if (this.extensions.length == 0) {
            return new TlsExtensions(new TlsExtension[] { add });
        }

        // Inefficient, but we don't expect to join many extensions
        TlsExtension[] joined = new TlsExtension[this.extensions.length + 1];
        System.arraycopy(this.extensions, 0, joined, 0, this.extensions.length);
        joined[this.extensions.length] = add;
        return new TlsExtensions(joined);
    }

}
