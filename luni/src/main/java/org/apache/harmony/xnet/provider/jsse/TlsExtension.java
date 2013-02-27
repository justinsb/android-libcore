package org.apache.harmony.xnet.provider.jsse;

public abstract class TlsExtension {
    public static final TlsExtension[] EMPTY_ARRAY = new TlsExtension[0];

    public static final int EXTENSION_SNI = 0;
    public static final int EXTENSION_NPN = 13172;

    public final int type;
    public final int length;

    public TlsExtension(int type, int length) {
        this.type = type;
        this.length = length;
    }

    /**
     * Sends message
     * 
     * @param out
     */
    public abstract void send(HandshakeIODataStream out);


}
