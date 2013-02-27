package android;

public class Byte {
	/**
	 * Returns a two-digit hex string. That is, -1 becomes "ff" or "FF" and 2 becomes "02".
	 * 
	 * @hide internal use only
	 */
	public static String toHexString(byte b, boolean upperCase) {
		return /* IntegralToString. */byteToHexString(b, upperCase);
	}

	private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	private static final char[] UPPER_CASE_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
			'D', 'E', 'F' };

	public static String byteToHexString(byte b, boolean upperCase) {
		char[] digits = upperCase ? UPPER_CASE_DIGITS : DIGITS;
		char[] buf = new char[2]; // We always want two digits.
		buf[0] = digits[(b >> 4) & 0xf];
		buf[1] = digits[b & 0xf];
		return new String(buf);
	}

}
