package fi.iki.yak.compression.integer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import fi.iki.yak.compression.integer.integer.Simple8;
import fi.iki.yak.compression.integer.integer.ZipfDistribution;

/**
 * @author Michael Burman
 */
public class Simple8EncodeTests {

    @Test
    void testSmallNumbers() throws Exception {
        // This sequence overflows to the next word quite often
        long input[] = {1, 2, 3, 4, 5, 6, 7, 9, 1, 8,
                5, 32, 5, 214, 6, 1,
                123, 12, 0};

        verifyCompression(input);
    }

    @Test
    void testLargerNumbers() throws Exception {
        long input[] = {869, 4895, 415631, 13546, 15861, 12384, 1238744, 4, 2318621, 12321567864364654L};
        verifyCompression(input);
    }

    @Test
    void matchingBits() throws Exception {
        long input[] = {1,2,3,1,2,1,2,1,2,3,2,1,2,1,2,1,2,1,2,3,2,1,2,3,1,2,2,1,2,3,0,0,0,0,1,1,2,3,1,2,2,0,0,1,2,3,1};
        verifyCompression(input);
    }

    @Test
    void testCase0And1() throws Exception {
        assertTrue(false);
    }

    @Test
    void testManualEncodeDecodes() throws Exception {
        // Test encode8, encode9 and their decoding - these are not generated by the pack_generate.py

    }

    @Test
    void zipfSequences() throws Exception {
        ZipfDistribution zipf = new ZipfDistribution(0.5, 2000);

        long[] original = zipf.stream().limit(4000).asLongStream().toArray();
        verifyCompression(original);
    }

    @Test
    void correctnessTesting() throws Exception {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for(int i = 0; i < 50; i++) {
            ZipfDistribution zipf = new ZipfDistribution(random.nextDouble(), 1000);

            long[] original = zipf.stream().limit(2500).asLongStream().toArray();
            verifyCompression(original);
        }
    }

    void verifyCompression(long[] input) {
        long[] compressed = new long[input.length];
        long[] uncompressed = new long[input.length];

        int amount = Simple8.compress(input, compressed);

        // Pass amount to the decompressor
        Simple8.decompress(compressed, 0, input.length, uncompressed, 0);

        Assertions.assertArrayEquals(input, uncompressed);
    }
}
