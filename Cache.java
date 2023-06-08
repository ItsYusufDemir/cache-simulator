/* Author: @ItsYusufDemir
 * Date: 8.06.2023 20:50
 *
 * Description:
 */

public class Cache {
    int blockBit;
    int setBit;
    int tagBit;
    int associativity;
    CacheLine cacheLines[][];

    public Cache(int blockBit, int setBit, int associativity) {
        this.blockBit = blockBit;
        this.setBit = setBit;
        this.associativity = associativity;

        tagBit = 64 - blockBit  - setBit;
        cacheLines = new CacheLine[(int)Math.pow(2,setBit)][associativity];
    }

}


