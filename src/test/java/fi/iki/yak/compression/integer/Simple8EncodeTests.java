package fi.iki.yak.compression.integer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import fi.iki.yak.compression.integer.integer.Simple8;

/**
 * @author Michael Burman
 */
public class Simple8EncodeTests {

    @Test
    void testSmallNumbers() throws Exception {
        // This sequence overflows to the next word quite often
        long[] input = {1, 2, 3, 4, 5, 6, 7, 9, 1, 8,
                5, 32, 5, 214, 6, 1,
                123, 12, 0};

        verifyCompression(input, 3);
    }

    @Test
    void testLargerNumbers() throws Exception {
        long[] input = {869, 4895, 415631, 13546, 15861, 12384, 1238744, 4, 2318621, 12321567864364654L};
        verifyCompression(input, 5);
    }

    @Test
    void matchingBits() throws Exception {
        long[] input = {1,2,3,1,2,1,2,1,2,3,2,1,2,1,2,1,2,1,2,3,2,1,2,3,1,2,2,1,2,3,0,0,0,0,1,1,2,3,1,2,2,0,0,1,2,3,1};
        verifyCompression(input, 3);
    }

    @Test
    void testCase0And1() throws Exception {

        long[] output = new long[2];

        // We should get a single word back with just selector 1
        long[] input120 =
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        // We should get a single word back with just selector 0
        long[] input240 =
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0};

        // We should get two words back with selector 1 and 2
        long[] input180 =
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        long[] input121 =
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        1};

        assertEquals(1, Simple8.compress(input120, output), "Only one word was supposed to be used for compression");
        int selector = (int) (output[0] >>> 60);
        assertEquals(1, selector);
        Arrays.fill(output, 0);

        assertEquals(1, Simple8.compress(input240, output), "Only one word was supposed to be used for compression");
        selector = (int) (output[0] >>> 60);
        assertEquals(0, selector);
        Arrays.fill(output, 0);

        assertEquals(2, Simple8.compress(input180, output), "Two words are needed to compress");
        selector = (int) (output[0] >>> 60);
        assertEquals(1, selector);

        selector = (int) (output[1] >>> 60);
        assertEquals(2, selector);
        Arrays.fill(output, 0);

        assertEquals(2, Simple8.compress(input121, output), "Two words are needed to compress");
        selector = (int) (output[0] >>> 60);
        assertEquals(1, selector);

        selector = (int) (output[1] >>> 60);
        assertEquals(15, selector);
        Arrays.fill(output, 0);
    }

    @Test
    void testManualEncodeDecodes() throws Exception {
        // Test encode8, encode9 and their decoding - these are not generated by the pack_generate.py

    }

    @Test
    void zipfSequences() throws Exception {
        ZipfDistribution zipf = new ZipfDistribution(0.5, 2000);

        long[] original = zipf.stream().limit(4000).asLongStream().toArray();
        verifyCompression(original, -1);
    }

    @Test
    void correctnessTesting() throws Exception {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for(int i = 0; i < 50; i++) {
            ZipfDistribution zipf = new ZipfDistribution(random.nextDouble(), 1000);

            long[] original = zipf.stream().limit(2500).asLongStream().toArray();
            verifyCompression(original, -1);
        }
    }

    void verifyCompression(long[] input, int expectedAmount) {
        long[] compressed = new long[input.length];
        long[] uncompressed = new long[input.length];

        int amount = Simple8.compress(input, compressed);

        if(expectedAmount > 0) {
            assertEquals(expectedAmount, amount);
        }

        // Pass amount to the decompressor
        Simple8.decompress(compressed, 0, amount, uncompressed, 0);

        Assertions.assertArrayEquals(input, uncompressed);
    }
}
