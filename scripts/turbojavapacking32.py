#!/usr/bin/env python

"""
This is a WIP port of Lemire's LittleIntPacker (github.com/lemire/LittleIntPacker) for Java 
Original code was not compatible with Python 3, this one is Python 3 only.

The code uses TurboPFor's (github.com/powturbo/TurboPFor) way of packing short array of integers by using 64 bit words and
a necessary amount of extra bytes to get an even number of bytes.

Supports only max 32 bit words for now
"""

# Abstract class for Packing
class Packer:
  inputType = ""
  outputType = ""

  def pack(self, bits):
    pass
  
  def unpack(self, bits):
    pass

class ArrayPacker(Packer):
  inputType = "int[]"
  outputType = "long[]"

  def pack(self, bits):
    print("")
    print("static void encode0({} in, int inputPos, {} out, int outputPos) {{".format(self.inputType, self.outputType))
    print("// TODO Special case, please define")
    print("}")
    print("")

    for bit in range(1,bits + 1):
      print("")
      print("static void encode{0}({1} in, int inputPos, {2} out, int outputPos) {{".format(bit, self.inputType, self.outputType))
      for k in range(howmanywords(bit)) :
        print("  long w{0};".format(k))
      for j in range(howmany(bit)):
        firstword = int(j * bit / 64)
        secondword = int((j * bit + bit - 1)/64)
        firstshift = int((j*bit) % 64)
        if( firstword == secondword):
            if(firstshift == 0):
              print("  w{0} = (long) in[{1}];".format(firstword,j))
            else:
              print("  w{0} |= (long)  in[{1}] << {2};".format(firstword,j,firstshift))
        else:
            print("  w{0} |= (long) in[{1}] << {2};".format(firstword,j,firstshift))
            secondshift = 64-firstshift
            print("  w{0} = (long) in[{1}] >> {2};".format(secondword,j,secondshift))
      for k in range(howmanywords(bit)) :
        print("  out[{0}] = w{0};".format(k))
      print("}")
      print("")

  def unpack(self, bits):
    print("static void decode0({} in, int inputPos, {} out, int outputPos) {{".format(self.outputType, self.inputType))
    print("// TODO Special case, create logic such as Arrays.fill")
    print("Arrays.fill(out, outputPos, outputPos+{}, 0);".format(bits))
    print("}")
    print("")

    for bit in range(1,bits + 1):
        print("")
        print("static void decode{}({} in, int inputPos, {} out, int outputPos) {{".format(bit, self.outputType, self.inputType))
        if(bit < 32):
          maskstr = " & {}L ".format((1<<bit)-1)
        if (bit == 32) : maskstr = "" # no need
        for k in range(howmanywords(bit)) :
          print("  long w{0} = in[{0}];".format(k))
        for j in range(howmany(bit)):
          firstword = int(j * bit / 64)
          secondword = int((j * bit + bit - 1)/64)
          firstshift = int((j*bit) % 64)
          firstshiftstr = ">> {0} ".format(firstshift)
          if(firstshift == 0):
              firstshiftstr ="" # no need
          if( firstword == secondword):
              if(firstshift + bit == 64):
                print("  out[{0}] = (int) ( w{1}  {2} );".format(j,firstword,firstshiftstr))
              else:
                print("  out[{0}] = (int)  ( ( w{1} {2}) {3} );".format(j,firstword,firstshiftstr,maskstr))
          else:
              secondshift = (64-firstshift)
              print("  out[{0}] = (int)  ( ( ( w{1} {2} ) | ( w{3} << {4} ) ) {5} );".format(j,firstword,firstshiftstr, firstword+1,secondshift,maskstr))
        print("}")
        print("")


# Default is to use ArrayPacker() logic
packer = ArrayPacker()

# Variable names for input and output
input = "input"
output = "output"

def howmany(bit):
    """ how many values are we going to pack? """
    return 32

bits = howmany(32)

def howmanywords(bit):
    return int((howmany(bit) * bit + 63)/64)

def howmanybytes(bit):
    if(howmany(bit) * bit % 8 != 0): raise "error"
    return int((howmany(bit) * bit + 7)/8)

def createCompressCall():
  print("""
  public void encode() {
    int inputPos = 0;
    int outputPos = 0;
    int compressBits = 0;

    // TODO Add compression logic here

    switch(compressBits) {
      """
  )

  for i in range(1, bits + 1):
    print("case {}:".format(i))
    print("  encode{}({}, inputPos, {}, outputPos);".format(i, input, output))
    print("  outputPos += {};".format(howmanybytes(i))) # Move output byte array Y-amount
    print("  break;")

  print("""    
    }""")
  print("inputPos += {};".format(howmany(i))) # Move input array X-amount
  print("""
  }
  """)

def createDecompressCall():
  print("""
  public void decode() {
    int inputPos = 0;
    int outputPos = 0;
    int decompressBits = 0;

    // TODO Add decompression logic here

    switch(decompressBits) {
  """)
  for i in range(1, bits + 1):
    print("case {}:".format(i))
    print("  decode{}({}, inputPos, {}, outputPos);".format(i, input, output))
    print("  inputPos += {};".format(howmanybytes(i))) # Decompressed Y-amount bytes
    print("  break;")

  print("""    
    }
    """)
  print("outputPos += {};".format(howmany(i))) # Move output array X-amount
  print("""
  }
  """)

# Create actual output
createCompressCall()
createDecompressCall()
packer.pack(bits)
packer.unpack(bits)
