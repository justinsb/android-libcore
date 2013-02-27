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
        if (extensions.length == 0) {
            this.length = 0;
        } else {
            int length = 2;
            for (TlsExtension extension : extensions) {
                length += 4 + extension.length;
            }
            this.length = length;
        }
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
}
