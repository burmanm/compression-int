/*
 * Copyright 2018 Michael Burman
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

    // 32 bits for value, 32 bits for bytearray offset (value is the offset for values), 8 bits for amount of bits and 8 bits for amount of items in the block
    public static int OVERHEAD = 80; 
    public static int MAX_AMOUNT_PER_BLOCK = 2*OVERHEAD;
    public static int SUB_BLOCK_OVERHEAD = 16;

    public static int bits(int value) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(value);
    }

    public void compress(int[] input) {
        int smallestOverHeadPerItem = OVERHEAD;
        int smallestOverHeadPosition = 0;
        int requiredBits = 0;
        int startPos = 0;
        int offset = input[0];

        for(int i = 0; i < input.length; i++) {
            int bits = bits(input[i] - offset); // The next item would require this many bits
            requiredBits = Math.max(bits, requiredBits); // Minimal required bits to store the next item
            int newTotalSpace = requiredBits * (i - startPos) + OVERHEAD; // Corresponds to c(j,i), first item only eats metadata's 80 bits
            int overHeadPerNewItem = newTotalSpace / (i+1 - startPos);

            if(overHeadPerNewItem < smallestOverHeadPerItem) {
                smallestOverHeadPosition = i;
                smallestOverHeadPerItem = overHeadPerNewItem;
            }

            // We check the results at the maximum cut point, which is 2 * OVERHEAD
            if((i - startPos) == (MAX_AMOUNT_PER_BLOCK - 1)) {
                // We have to cut here
                System.out.printf("Cut position, we selected %d items, bits per item: %d, total of %d bits\n", (smallestOverHeadPosition+1 - startPos), smallestOverHeadPerItem, (smallestOverHeadPerItem * (smallestOverHeadPosition-startPos)) + OVERHEAD);
                findSubBlockCount(input, startPos, smallestOverHeadPosition, requiredBits);
                i = smallestOverHeadPosition+1; // Start again from next item
                startPos = i;
                requiredBits = 0;
                overHeadPerNewItem = OVERHEAD;
                offset = input[i];
            }
        }
        // System.out.printf("Cut position, we selected position: %d, bits per item: %d, total of %d bits\n", smallestOverHeadPosition, smallestOverHeadPerItem, (smallestOverHeadPerItem * smallestOverHeadPosition+1));
        System.out.printf("Compressing remaining %d items to next block with %d bits per item, total of %d\n", (input.length - startPos), requiredBits, (requiredBits * (input.length - 1 - startPos) + OVERHEAD));
        findSubBlockCount(input, startPos, input.length, requiredBits);
    }

    /**
     * Find optimal static partitioning subblock count for compression purposes. Last subblock takes all the remaining items
     * if the size wasn't even
     */
    static int findSubBlockCount(int[] input, int startPos, int endPos, final int bits) {
        int itemCount = endPos - startPos;
        int maxBlockCount = itemCount / 4; // minCount = 2

        // Paper mentions that we should not compress less than 4 items per block, but does not mention
        // what if there's less than 2 items in the last block. What should be done in that case?
        if(itemCount < 4) {
            return 1;
        }

        int blocks = 2; // min count
        // The search for static block counts is from 2 to itemCount / 4
        int previousTotalBits = Integer.MAX_VALUE;
        int maxAmountOfBits = 0;
        for(int i = 2; i < maxBlockCount; i++) {
            // Divide the range to i parts and check how much space this complexity eats ( + i * 16)
            int itemsPerSubBlock = itemCount / i; // Round down 
            int offset = input[startPos]; // This is the first block's offset we use
            int subBlockBits = 0;

            for(int j = 0; j < itemCount; j++) {
                subBlockBits = Math.max(bits(input[startPos + j] - offset), subBlockBits);
                if(j % itemsPerSubBlock == 0) {
                    // Switch block..
                    if(j + 1 < itemCount) {
                        // There's still next round
                        offset = input[startPos + j + 1];
                    }
                }
            }

            int totalBits = subBlockBits * (itemCount - i);
            totalBits += i * bits; // mini skip pointers
            totalBits += SUB_BLOCK_OVERHEAD; // block overhead
            if(totalBits < previousTotalBits) {
                blocks = i;
                previousTotalBits = totalBits;
                maxAmountOfBits = subBlockBits;
            }
        }
        System.out.printf("Selected subBlockCount of %d with %d bits per item for a total of %d bits\n", blocks, maxAmountOfBits, previousTotalBits);

        return maxBlockCount;
    }

    public static void main(String[] args) {
        // This example is from the paper in their dynamic partition part, should result in two partitions, 108 and 148 items
        int[] paperExample = new int[256];
        for(int i = 108; i > 0; i--) {
            paperExample[i] = 120+ (int) (1.07*(double) (i-108));
        }
        for(int i = 108; i < paperExample.length; i++) {
            double val = 500D+2.7*(double) (i-108);
            paperExample[i] = (int) val;
        }

        // Alignments mentioned in the paper (middle values were not, they're approximations)
        paperExample[0] = 4;
        paperExample[107] = 120;
        paperExample[108] = 500;
        paperExample[255] = 900;

        // Print example array's values
        // for(int i = 0; i < paperExample.length; i++) {
        //     System.out.printf("%d ", paperExample[i]);
        // }
        // System.out.println();

        // From the paper, max limit for a block is 160 items (2 * OVERHEAD)
        int[] checkLimits = new int[322];
        for(int i = 0; i < checkLimits.length; i++) {
            checkLimits[i] = 1;
        }

        // Should result in 108 as the first block size
        MilcScalar milc = new MilcScalar();
        milc.compress(paperExample);
        milc.compress(checkLimits);
    }
}