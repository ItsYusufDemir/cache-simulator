/* Author: @ItsYusufDemir
 * Date: 8.06.2023 20:57
 *
 * Description:
 */

public class CacheLine{
    String data;
    boolean isValid;
    int time;
    String tag;

    public CacheLine(String data, int time, String tag){
        this.data = data;
        isValid = false;
        this.time = time;
        this.tag = tag;
    }

}
