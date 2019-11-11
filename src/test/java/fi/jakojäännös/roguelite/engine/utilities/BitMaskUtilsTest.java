package fi.jakojäännös.roguelite.engine.utilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class BitMaskUtilsTest {
    @ParameterizedTest
    @CsvSource({"0,0", "1,1", "2,1", "8,1", "9,2", "100,13"})
    void maskSizeIsCalculatedCorrectly(int n, int expected) {
        assertEquals(expected, BitMaskUtils.calculateMaskSize(n));
    }

    @ParameterizedTest
    @CsvSource({"0,1", "1,2", "4,16", "9,512", "30,1073741824"})
    void setNthBitSetsTheCorrectBitWhenInitialMaskIsEmpty(int n, int expected) {
        byte[] expectedBytes = intToBytes(expected);
        assertArrayEquals(expectedBytes, BitMaskUtils.setNthBit(new byte[4], n));
    }

    @ParameterizedTest
    @CsvSource({"1,0,1", "1,1,3", "8,4,24", "1342,7,1470", "4242,18,266386", "65535,15,65535", "2147483647,1,2147483647", "2147483647,8,2147483647", "2147483647,30,2147483647", "2147483647,31,-1"})
    void setNthBitSetsTheCorrectBitWhenInitialMaskIsDefined(int initial, int n, int expected) {
        byte[] expectedBytes = intToBytes(expected);
        byte[] initialBytes = intToBytes(initial);
        assertArrayEquals(expectedBytes, BitMaskUtils.setNthBit(initialBytes, n));
    }

    @ParameterizedTest
    @CsvSource({"1,0,0", "3,1,1", "24,4,8", "1470,7,1342", "266386,18,4242", "2147483647,1,2147483645"})
    void unsetNthBitSetsTheCorrectBitWhenInitialMaskIsDefined(int initial, int n, int expected) {
        byte[] expectedBytes = intToBytes(expected);
        byte[] initialBytes = intToBytes(initial);
        assertArrayEquals(expectedBytes, BitMaskUtils.unsetNthBit(initialBytes, n));
    }

    private byte[] intToBytes(int expected) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(expected).array();
        return new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]};
    }

    @Test
    void combineMasksProducesCorrectMask() {
        assertArrayEquals(new byte[]{1, 2, 4, 8},
                          BitMaskUtils.combineMasks(new byte[]{1, 2, 0, 0}, new byte[]{0, 0, 4, 8}));
    }

    @Test
    void compareMasksReturnsTrueForSameMask() {
        assertTrue(BitMaskUtils.compareMasks(new byte[]{1, 2, 3, 4}, new byte[]{1, 2, 3, 4}));
    }

    @Test
    void compareMasksReturnsFalseForDifferentMask() {
        assertFalse(BitMaskUtils.compareMasks(new byte[]{4, 3, 2, 1}, new byte[]{1, 2, 3, 4}));
    }

    @Test
    void compareMasksReturnsFalseForMasksOfDifferentLength() {
        assertFalse(BitMaskUtils.compareMasks(new byte[]{4, 3, 2, 0}, new byte[]{1, 2, 3}));
    }

    @ParameterizedTest
    @CsvSource({"1,0", "128,7", "257,8"})
    void isNthBitSetReturnsTrueForTrueBits(int mask, int n) {
        byte[] bytes = intToBytes(mask);
        assertTrue(BitMaskUtils.isNthBitSet(bytes, n));
    }

    @ParameterizedTest
    @CsvSource({"1,1", "127,8", "257,10"})
    void isNthBitSetReturnsFalseForFalseBits(int mask, int n) {
        byte[] bytes = intToBytes(mask);
        assertFalse(BitMaskUtils.isNthBitSet(bytes, n));
    }
}
