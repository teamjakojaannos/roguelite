package fi.jakojaannos.roguelite.engine.utilities;

public class BitMaskUtils {
    public static int calculateMaskSize(int n) {
        // fast divide by 8 and ceil
        return n / 8 + ((n % 8 == 0) ? 0 : 1);
    }

    public static byte[] setNthBit(byte[] mask, int n) {
        mask[n / 8] |= (1 << (n % 8));
        return mask;
    }

    public static byte[] unsetNthBit(byte[] mask, int n) {
        mask[n / 8] &= ~(1 << (n % 8));
        return mask;
    }

    public static byte[] combineMasks(byte[] a, byte[] b) {
        for (int n = 0; n < Math.min(a.length, b.length); ++n) {
            a[n] |= b[n];
        }
        return a;
    }

    public static boolean isNthBitSet(byte[] mask, int n) {
        return (mask[n / 8] & (1 << (n % 8))) != 0;
    }

    /**
     * Checks that all bits set on mask b are also set on mask a
     *
     * @param a mask to validate
     * @param b mask to validate against
     *
     * @return true if condition is met, false otherwise
     */
    public static boolean hasAllBitsOf(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        for (int i = 0; i < a.length; ++i) {
            if ((a[i] & b[i]) != b[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks that none of the bits set on mask b are set on mask a
     *
     * @param a mask to validate
     * @param b mask to validate against
     *
     * @return true if condition is met, false otherwise
     */
    public static boolean hasNoneOfTheBitsOf(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        for (int i = 0; i < a.length; ++i) {
            if ((a[i] & b[i]) != 0) {
                return false;
            }
        }

        return true;
    }
}
