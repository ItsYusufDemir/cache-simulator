/* Author: @ItsYusufDemir
 * Date: 8.06.2023 20:50
 * 
 * Description: A cache simulator is implemented.
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Scanner;

public class CacheSimulator {

    private static File traceInput;
    private static byte[] data;
    private static  Cache cache;
    private static int setBit;
    private static int associativy;
    private static int blockBit;
    private static int numberOfMatches = 0;

    public static void main(String args[]){

        setBit = 1;
        associativy = 1;
        blockBit =  3;

        //First, create the cache
        cache = new Cache(3,1,3);




        File ramFile = new File("RAM.dat");

        //Read RAM
        try (FileInputStream fileInputStream = new FileInputStream(ramFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
            data = new byte[(int) ramFile.length()];
            int bytesRead = bufferedInputStream.read(data);

        } catch (Exception e) {
            System.out.println(ramFile.getName() + " could not be found");
            System.exit(0);
        }

        //Read trace file
        try {
            traceInput = new File("test_medium.trace");
            Scanner scanner = new Scanner(traceInput);

            while (scanner.hasNext()) {

                char type = scanner.next().charAt(0);
                switch (type){
                    case 'M': {
                        String adress = scanner.next();
                        adress = adress.substring(0, adress.length()-1);
                        String size = scanner.next();
                        size = size.substring(0, size.length()-1);
                        String data = scanner.next();

                        modify(adress, Integer.parseInt(size), data);
                    } break;
                    case 'L':{
                        String adress = scanner.next();
                        adress = adress.substring(0, adress.length()-1);
                        String size = scanner.next();

                        load(adress, Integer.parseInt(size));
                    } break;
                    case 'S': {
                        String adress = scanner.next();
                        adress = adress.substring(0, adress.length()-1);
                        String size = scanner.next();
                        size = size.substring(0, size.length()-1);
                        String data = scanner.next();

                        store(adress, Integer.parseInt(size), data);
                    }break;
                    default: System.out.println("Trace file has a problem!"); System.exit(0);
                }

            }

            scanner.close();
        } catch (Exception e) {
            System.out.println( traceInput.getName() + " couldn't opened!");
            System.exit(0);
        }


        int b = 2;




    }



    public static void modify(String adress, int size, String data){

    }

    public static void load(String adress, int size){

        int setIndex = findSetBit(adress);
        String tag = findTag(adress);

        for(int i = 0; i < associativy; i++){
            if(cache.cacheLines[setIndex][i] != null){
                if(cache.cacheLines[setIndex][i].isValid){
                    if(cache.cacheLines[setIndex][i].tag.equals(tag)){
                        numberOfMatches++;
                    }
                }
            }

        }


    }

    public static void store(String adress, int size, String data){

    }


    //Bunların hepsini tek bi yerde bul
    public static int findSetBit(String adress){
        return 1;
    }

    public static String findTag(String adress){
        return "";
    }




}
