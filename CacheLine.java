/* Author: @ItsYusufDemir
 * Date: 8.06.2023 20:57
 *
 * Description:
 */

public class CacheLine{
    byte[] data;
    boolean isValid;
    int time;
    long tag;

    public CacheLine(byte[] data, int time, long tag){
        isValid = true;
        this.time = time;
        this.tag = tag;

        this.data = data;
    }

}
