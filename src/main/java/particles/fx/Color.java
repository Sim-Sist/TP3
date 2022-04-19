package particles.fx;

public class Color {
    private byte r, g, b;

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String byteToHex(byte b) {
        char[] hexChars = new char[2];
        int v = b & 0xFF;
        hexChars[1] = HEX_ARRAY[v >>> 4];
        hexChars[0] = HEX_ARRAY[v & 0x0F];

        return new String(hexChars);
    }

    public Color(int r, int g, int b) {
        if (!validRGBRange(r) || !validRGBRange(g) || !validRGBRange(b)) {
            throw new RuntimeException("rgb values not in valid range");
        }
        this.r = (byte) r;
        this.g = (byte) g;
        this.b = (byte) b;
    }

    private boolean validRGBRange(int v) {
        return 0 <= v && v <= 255;
    }

    @Override
    public String toString() {
        return "#" + byteToHex(this.r) + byteToHex(this.g) + byteToHex(this.b);
    }

}
