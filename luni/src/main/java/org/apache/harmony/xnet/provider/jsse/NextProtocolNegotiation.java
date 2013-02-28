package org.apache.harmony.xnet.provider.jsse;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

public class NextProtocolNegotiation {
    public interface ServerProvider {
        /**
         * Determine list of protocols for which to advertise support
         */
        TlsExtensionNpn getSupportedProtocols(SSLSession session);

        // Jetty defines this...
        // /**
        // * Callback to notify of selected protocol
        // *
        // * @param protocol
        // */
        // void protocolSelected(String protocol);
        // void unsupported()
        // Callback invoked to let the application know that the client does not
        // support NPN.
    }

    public interface SupportsNextProtocolNegotiation {
        void setNpnServerProvider(ServerProvider serverProvider);

        byte[] getNpnSelectedProtocol();
        
        boolean isNpnNegotiationComplete();
    }

    public static void put(SSLEngine engine, ServerProvider serverProvider) {
        if (engine instanceof SupportsNextProtocolNegotiation) {
            ((SupportsNextProtocolNegotiation) engine)
                    .setNpnServerProvider(serverProvider);
        } else {
            throw new IllegalArgumentException(
                    "Engine does not support Next-Protocol negotiation");
        }
    }

    public static byte[] getSelectedProtocol(SSLEngine engine) {
        if (engine instanceof SupportsNextProtocolNegotiation) {
            return ((SupportsNextProtocolNegotiation) engine)
                    .getNpnSelectedProtocol();
        } else {
            // Return null => no NPN ??
            throw new IllegalArgumentException(
                    "Engine does not support Next-Protocol negotiation");
        }
    }

    public static boolean isNegotiationComplete(SSLEngine engine) {
        if (engine instanceof SupportsNextProtocolNegotiation) {
            return ((SupportsNextProtocolNegotiation) engine)
                    .isNpnNegotiationComplete();
        } else {
            // Return true ??
            throw new IllegalArgumentException(
                    "Engine does not support Next-Protocol negotiation");
        }
    }

    
}
