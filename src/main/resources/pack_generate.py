import sys

"""
Helper tool to generate some repeating code in the Simple8b compression implementation. Requires some cleaning after generation, such as
python generate_code.py | sed -e 's/ << 0//g' | sed -e 's/ + 0//g'
"""

"""
Selector         & 0   & 1   & 2  & 3  & 4  & 5  & 6  & 7  & 8 & 9 & 10 & 11 & 12 & 13 & 14 & 15 \\ \hline
Integers         & 240 & 120 & 60 & 30 & 20 & 15 & 12 & 10 & 8 & 7 & 6  & 5  & 4  & 3  & 2  & 1  \\ \hline
Bits per integer & 0   & 0   & 1  & 2  & 3  & 4  & 5  & 6  & 7 & 8 & 10 & 12 & 15 & 20 & 30 & 60 \\ \hline
Wasted bits      & 60  & 60  & 0  & 0  & 0  & 0  & 0  & 0  & 4 & 4 & 0  & 0  & 0  & 0  & 0  & 0
"""

bits = [0,0,1,2,3,4,5,6,7,8,10,12,15,20,30,60]
integers = [240,120,60,30,20,15,12,10,8,7,6,5,4,3,2,1]

masks = [0x00, 0x00, 0x01, 0x03, 0x07, 0x0F, 0x1F, 0x3F, 0x7F, 0xFF, 0x3FF, 0xFFF, 0x7FFF, 0xFFFFF, 0x3FFFFFFF, 0xFFFFFFFFFFFFFFF]

def canPackGeneration():
    for i in range(15):
        print('boolean canPack{}() {{'.format(i))
        print('return largestSetBit{}() <= {};'.format(i, bits[i]))
        print('}')

def maxBitsGeneration():
    for i in range(15):
        print('int largestSetBit{}() {{'.format(i))
        print('long mask |= iA[i];')
        for b in range(1, integers[i]):
            # This is something that the Java can probably do with SIMD?
            # Or do I need to make it happen with SIMD for some cases?
            print('mask |= iA[i+{}];'.format(b))
        print('return bitsRequired(mask);')
        print('}')

def packGeneration(mask = True):
    for i in range(2,16):
        if i == 8 or i == 9:
            continue

        print('static void encode{}(final long[] input, int startPos, final long[] output, int outputPos) {{'
                         ''.format(i))
        bitsLeft = 60 - bits[i]
        print('output[outputPos] |= {}L << 60;'.format(i))
        # First line should not be printed (again << 60)
        for b in range(0, integers[i]):
            if mask is True:
                print('output[outputPos] |= (input[startPos + {}] & {}) << {};'.format(b, masks[i], bitsLeft))
            else:
                print('output[outputPos] |= (input[startPos + {}]) << {};'.format(b, bitsLeft))
            bitsLeft = bitsLeft - bits[i]
        # Print last one here without shift
        print('}')
        print('')

def unpackGeneration():
    for i in range(2,16):
        if i == 8 or i == 9:
            continue

        print('static void decode{}(final long[] input, int startPos, final long[] output, int outputPos) {{'.format(i))
        b = 60 - bits[i]
        j = 0
        while b > 0:
            print('output[outputPos + {}] = (input[startPos] >>> {}) & {};'.format(j, b, masks[i]))
            b -= bits[i]
            j += 1
        print('output[outputPos++] = input[startPos] & {}L;'.format(masks[i]))
        print(' }')
        print('')

def switchGeneration():
    b = 2
    for i in range(61):
        print('case {}:'.format(i))
        if bits[b] == i:
            print('encode{}(input, inputPos, output, outputPos);'.format(b))
            print('inputPos += {};'.format(integers[b]))
            print('break;')
            b += 1
    print('default:')


def switchDecodeGeneration():
    for i in range(0,16):
        print('case {}:'.format(i))
        print('decode{}(input, inputPos, output, outputPos);'.format(i))
        print('outputPos += {};'.format(integers[i]))
        print('break;')

def testGeneration():
    print('@Test')
    print('void testCorrectSizes() throws Exception {')

    for i in range(2, 16):
        arr = '{}, '.format(masks[i] - 1) * integers[i]
        print('long[] input{} = {{ {} }};'.format(integers[i], arr[:-2]))
        print('verifyCompression(input{}, 1);'.format(integers[i]))

    print('}')

# bits = [0,0,1,2,3,4,5,6,7,8,10,12,15,20,30,60]

def printSpecialities():
    print("""

int bitsRequired(long mask) {
    return Long.SIZE - Long.numberOfLeadingZeros(mask);
}

// These are special cases for packing, use maskless version in Simple8..

static void encode8(long[] input, int startPos, long[] output, int outputPos) {
    output[outputPos] |= 8L << 60;
    output[outputPos] |= (input[startPos++] & 0x7F) << 49;
    output[outputPos] |= (input[startPos++] & 0x7F) << 42;
    output[outputPos] |= (input[startPos++] & 0x7F) << 35;
    output[outputPos] |= (input[startPos++] & 0x7F) << 28;
    output[outputPos] |= (input[startPos++] & 0x7F) << 21;
    output[outputPos] |= (input[startPos++] & 0x7F) << 14;
    output[outputPos] |= (input[startPos++] & 0x7F) << 7;
    output[outputPos] |= (input[startPos++] & 0x7F);
}

static void encode9(long[] input, int startPos, long[] output, int outputPos) {
    output[outputPos] |= 9L << 60;
    output[outputPos] |= (input[startPos++] & 0xFF) << 48;
    output[outputPos] |= (input[startPos++] & 0xFF) << 40;
    output[outputPos] |= (input[startPos++] & 0xFF) << 32;
    output[outputPos] |= (input[startPos++] & 0xFF) << 24;
    output[outputPos] |= (input[startPos++] & 0xFF) << 16;
    output[outputPos] |= (input[startPos++] & 0xFF) << 8;
    output[outputPos] |= (input[startPos++] & 0xFF);
}

// These are special cases for unpacking

    static void decode8(final long[] input, int startPos, final long[] output, int outputPos) {
        output[outputPos++] = (input[startPos] >>> 49) & 127;
        output[outputPos++] = (input[startPos] >>> 42) & 127;
        output[outputPos++] = (input[startPos] >>> 35) & 127;
        output[outputPos++] = (input[startPos] >>> 28) & 127;
        output[outputPos++] = (input[startPos] >>> 21) & 127;
        output[outputPos++] = (input[startPos] >>> 14) & 127;
        output[outputPos++] = (input[startPos] >>> 7) & 127;
        output[outputPos++] = input[startPos] & 127;
    }

    static void decode9(final long[] input, int startPos, final long[] output, int outputPos) {
        output[outputPos++] = (input[startPos] >>> 48) & 255;
        output[outputPos++] = (input[startPos] >>> 40) & 255;
        output[outputPos++] = (input[startPos] >>> 32) & 255;
        output[outputPos++] = (input[startPos] >>> 24) & 255;
        output[outputPos++] = (input[startPos] >>> 16) & 255;
        output[outputPos++] = (input[startPos] >>> 8) & 255;
        output[outputPos++] = input[startPos] & 255;
    }

    """)
    
printSpecialities()
#canPackGeneration()
#maxBitsGeneration()
packGeneration(mask = False)
switchGeneration()
unpackGeneration()
switchDecodeGeneration()
testGeneration()
