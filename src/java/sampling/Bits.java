package sampling;

public class Bits {

  public static int shiftLeft(int bits, int positions) {
    return bits << positions ;
  }

  public static int shiftRight(int bits, int positions) {
    return bits >> positions ;
  }

  public static int unsignedShiftRight(int bits, int positions) {
    return bits >>> positions ;
  }
  
  public static int xor(int bits1, int bits2) {
    return bits1 ^ bits2;
  }
}
