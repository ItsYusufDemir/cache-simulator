/* Author: @ItsYusufDemir
 * Date: 8.06.2023 20:50
 * 
 * Description: A cache simulator is implemented.
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

public class CacheSimulator {

    private static File traceInput;
    private static byte[] ram;
    private static  Cache cache;
    private static int setBit;
    private static int associativy;
    private static int blockBit;
    private static int numberOfHits = 0;
    private static int numberOfMisses = 0;
    private static int numberOfEvictions = 0;
    private static int time = 0;
    private static String currentTag;
    private static int currentSetIndex;

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
            ram = new byte[(int) ramFile.length()];
            int bytesRead = bufferedInputStream.read(ram);

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

        findSetAndTag(adress);
        int setIndex = currentSetIndex;
        String tag = currentTag;

        for(int i = 0; i < associativy; i++){
            if(cache.cacheLines[setIndex][i] != null){
                if(cache.cacheLines[setIndex][i].isValid){
                    if(cache.cacheLines[setIndex][i].tag.equals(tag)){
                        System.out.println("Hit");
                        numberOfHits++;
                        return;
                    }
                }
            }

        }

        //MISS
        System.out.println("Miss");
        numberOfMisses++;
        int blockSize = (int)Math.pow(cache.blockBit, 2); //How many bytes does a block can store

        byte[] data = new byte[blockSize];
        for(int i = 0; i < blockSize; i++){
            data[i] = (ram[hexToDecimal(adress) + i]);
        }

        //Search for empty line, if it is found, write it to that line
        for(int i = 0; i < associativy; i++){
            if(cache.cacheLines[setIndex][i] == null) {
                cache.cacheLines[setIndex][i] = new CacheLine(data, time, tag, blockSize);
                System.out.println("Place in cache set " + setIndex);
                return;
            }
        }

        //If not empty line, then find the victim line to be evicted
        numberOfEvictions++;
        int victimIndex = 0;
        for(int i = 0; i < associativy - 1; i++){
            if(cache.cacheLines[setIndex][i+1].time > cache.cacheLines[setIndex][i].time)
                victimIndex = i + 1;
        }

        cache.cacheLines[setIndex][victimIndex].isValid = true;
        cache.cacheLines[setIndex][victimIndex].tag = tag;
        cache.cacheLines[setIndex][victimIndex].data = data;
        cache.cacheLines[setIndex][victimIndex].time = time;
    }

    public static void store(String adress, int size, String dataAsString){

        findSetAndTag(adress);
        int setIndex = currentSetIndex;
        String tag = currentTag;

        byte[] data = convertStringToByte(dataAsString);

        //HIT
        for(int i = 0; i < associativy; i++){
            if(cache.cacheLines[setIndex][i] != null){
                if(cache.cacheLines[setIndex][i].isValid){
                    if(cache.cacheLines[setIndex][i].tag.equals(tag)){
                        System.out.println("Hit");
                        numberOfHits++;


                        //Change the data in cache
                        for(int j = 0; j < data.length; j++){
                            cache.cacheLines[setIndex][i].data[j] = data[j];
                        }

                        //Change the data in RAM
                        for(int j = 0; j < data.length; j++){
                            ram[hexToDecimal(adress) + j] = data[j];
                        }

                        System.out.println("Store in cache set " + setIndex + " and RAM");
                        return;
                    }
                }

            }
        }

        //MISS
        System.out.println("Miss");
        numberOfMisses++;
        //Change the data in RAM
        for(int j = 0; j < data.length; j++){
            ram[hexToDecimal(adress) + j] = data[j];
        }
        System.out.println("Store in RAM");


    }


    public static void findSetAndTag(String adress){
        String adressInBinary = convertHexToBinary(adress);

        String setIndexInBinary = adressInBinary.substring(64-blockBit-setBit,64-blockBit);
        String tag = adressInBinary.substring(0,64-blockBit-setBit);

        currentSetIndex = binaryToDecimal = setIndexInBinary;
        currentTag = tag;
    }







}
