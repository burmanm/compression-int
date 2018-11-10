/*
 * Copyright 2017-2018 Michael Burman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.iki.yak.compression.integer;

/**
 * This implementation follows the paper "MILC: Inverted List Compression in Memory", but uses a scalar
 * implementation instead of the described SIMD implementation.
 * 
 * @author Michael Burman
*/
public class MilcScalar {

    // 32 bits for value, 32 bits for offset, 8 bits for amount of bits and 8 bits for amount of items in the block
    public static int OVERHEAD = 80; 
    public static int MAX_AMOUNT_PER_BLOCK = 2*OVERHEAD;

    public static int bits(int value) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(value);
    }

    public void compress(int[] input) {
        // TODO Should we return two long[] or append them..?

        // TODO Find dynamic block size - max size is 160
        // TODO Find best offset value instead of first value for the offset table?
        long total = 0;
        double prevOverheadPerItem = (double) OVERHEAD;
        int requiredBits = 0;
        int startPos = 0;
        int offset = input[0];
        int bits = 0;
        for(int i = 0; i < input.length; i++) {
            bits = bits(input[i] - offset); // The next item would require this many bits
            requiredBits = Math.max(bits, requiredBits); // Minimal required bits to store the next item
            int newTotalSpace = requiredBits * (i - startPos) + OVERHEAD; // Corresponds to c(j,i), first item requires 80 bits

            double overHeadPerNewItem = newTotalSpace / (i+1 - startPos); // First item must be calculated here

            // If next overhead is larger than prev overhead per item, then we split
            if(overHeadPerNewItem > prevOverheadPerItem) {
                // Compress here
                System.out.printf("Compressing %d items to next block with %f bits per item, adding next item would result in %f bits per item\n", (i - startPos), prevOverheadPerItem, overHeadPerNewItem);
                startPos = i;
                overHeadPerNewItem = (double) OVERHEAD;
                offset = input[i];
            }
            prevOverheadPerItem = overHeadPerNewItem;

        }
        System.out.printf("Compressing remaining %d items to next block with  %f bits per item\n", (256 - startPos), prevOverheadPerItem, bits);
    }

    public static void main(String[] args) {
        // This example is from the paper in their dynamic partition part, should result in two partitions, 108 and 148 items
        int[] paperExample = new int[256];
        for(int i = 108; i > 0; i--) {
            paperExample[i] = 120-(4+i);
        }
        for(int i = 108; i < paperExample.length; i++) {
            double val = 3.5*(double) i;
            paperExample[i] = (int) val;
        }

        // Alignments mentioned in the paper (middle values were not, they're approximations)
        paperExample[0] = 4;
        paperExample[107] = 120;
        paperExample[108] = 500;
        paperExample[255] = 900;

        // Should result in 108 as the first block size
        MilcScalar milc = new MilcScalar();
        milc.compress(paperExample);
    }
}