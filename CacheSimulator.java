/* Authors: Mehmet Said AltIok - 150117504
 *          Tayfun Ekentok 150120072
 *          Yusuf AYDIN - 150119014
 *          Yusuf Demir - 150120032
 *
 * Date: 8.06.2023 20:50
 * 
 * Description: A cache simulator is implemented. We have only one cache in this simulator. There are three operations:
 * 1) L: Load data
 * 2) S: Store data
 * 3) M: Modify data
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class CacheSimulator {

    private static File traceInput;
    private static byte[] ram;
    private static  Cache cache;
    private static int setBit;
    private static int associativity;
    private static int blockBit;
    private static int setNumber;
    private static int blockSize;
    private static int numberOfHits = 0;
    private static int numberOfMisses = 0;
    private static int numberOfEvictions = 0;
    private static int time = 0;
    private static long currentTag;
    private static int currentSetIndex;

    public static void main(String args[]){

        setBit = 4;
        associativity = 16;
        blockBit =  6;

        //First, create the cache
        cache = new Cache(blockBit,setBit, associativity);

        setNumber = (int)Math.pow(2,cache.setBit);
        blockSize = (int)Math.pow(2,cache.blockBit);




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
            traceInput = new File("test_large.trace");
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

                        System.out.println("M " + adress + ", " + size + ", " + data);

                        modify(adress, (int)Integer.parseInt(size), data);
                    } break;
                    case 'L':{
                        String adress = scanner.next();
                        adress = adress.substring(0, adress.length()-1);
                        //adress = padAddress(adress);
                        String size = scanner.next();

                        System.out.println("L " + adress + ", " + size);

                        load(adress, Integer.parseInt(size),false);
                    } break;
                    case 'S': {
                        String adress = scanner.next();
                        adress = adress.substring(0, adress.length()-1);
                        //adress = padAddress(adress);
                        String size = scanner.next();
                        size = size.substring(0, size.length()-1);
                        String data = scanner.next();

                        System.out.println("S " + adress + ", " + size + ", " + data);

                        store(adress, Integer.parseInt(size), data, false);
                    }break;
                    default: System.out.println("Trace file has a problem!"); System.exit(0);
                }

                System.out.println();
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println( traceInput.getName() + " couldn't opened!");
            System.exit(0);
        }
        catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Check that your cache specifications match with the RAM image and trace file!");
        }


        System.out.println("hits: " + numberOfHits + " misses: " + numberOfMisses + " evictions: " + numberOfEvictions);
    }



    public static void modify(String address, int size, String dataAsHex){
        load(address, size, true);
        store(address, size, dataAsHex, true);
    }

    public static void load(String address, int size, boolean fromModify){

        findSetAndTag(address);
        int setIndex = currentSetIndex;
        long tag = currentTag;

        for(int i = 0; i < associativity; i++){
            if(cache.cacheLines[setIndex][i] != null){
                if(cache.cacheLines[setIndex][i].isValid){
                    if(cache.cacheLines[setIndex][i].tag == tag){
                        System.out.println("Hit");
                        numberOfHits++;

                        if(!fromModify){
                            if (setNumber == 1)
                                System.out.println("Found in cache");
                            else
                                System.out.println("Found in cache set " + setIndex);
                        }
                        return;
                    }
                }
            }

        }

        //MISS
        System.out.println("Miss");
        numberOfMisses++;

        //Fetch the data from the RAM
        byte[] data = new byte[blockSize];
        for(int i = 0; i < blockSize; i++){
            data[i] = (ram[(int)hexToDecimal(address) + i]);
        }

        //Search for empty line in the set, if it is found, write it to that line
        for(int i = 0; i < associativity; i++){
            if(cache.cacheLines[setIndex][i] == null) {
                cache.cacheLines[setIndex][i] = new CacheLine(data, time, tag);
                time++;
                if(!fromModify) {
                    if(setNumber == 1)
                        System.out.println("place in cache");
                    else
                        System.out.println("Place in cache set " + setIndex);
                }
                return;
            }
        }

        //If there is no empty line, then find the victim line to be evicted
        numberOfEvictions++;
        int victimIndex = 0;
        for(int i = 0; i < associativity - 1; i++){
            if(cache.cacheLines[setIndex][i+1].time > cache.cacheLines[setIndex][i].time)
                victimIndex = i + 1;
        }

        cache.cacheLines[setIndex][victimIndex].isValid = true;
        cache.cacheLines[setIndex][victimIndex].tag = tag;
        cache.cacheLines[setIndex][victimIndex].data = data;
        cache.cacheLines[setIndex][victimIndex].time = time;
        time++;

        if(!fromModify) {
            if(setNumber == 1)
                System.out.println("Place in cache");
            else
                System.out.println("Place in cache set " + victimIndex);
        }
    }

    public static void store(String address, int size, String dataAsHex, boolean fromModify){

        findSetAndTag(address);
        int setIndex = currentSetIndex;
        long tag = currentTag;

        byte[] data = convertStringToByte(dataAsHex);

        //HIT
        for(int i = 0; i < associativity; i++){
            if(cache.cacheLines[setIndex][i] != null){
                if(cache.cacheLines[setIndex][i].isValid){
                    if(cache.cacheLines[setIndex][i].tag == tag){

                        if(!fromModify) {
                            System.out.println("Hit");
                            numberOfHits++;
                        }

                        //Change the data in cache
                        for(int j = 0; j < data.length; j++){
                            cache.cacheLines[setIndex][i].data[j] = data[j];
                        }
                        time++;

                        //Change the data in RAM
                        for(int j = 0; j < data.length; j++){
                            ram[(int)hexToDecimal(address) + j] = data[j];
                        }

                        if(!fromModify) {
                            if(setNumber == 1)
                                System.out.println("Store in cache and RAM");
                            else
                                System.out.println("Store in cache set " + setIndex + " and RAM");
                        }
                        else {
                            if(setNumber == 1)
                                System.out.println("Modify in cache and RAM");
                            else
                                System.out.println("Modify in cache set " + setIndex + " and RAM");
                        }
                        return;
                    }
                }

            }
        }


        //MISS
        System.out.println("Miss");
        numberOfMisses++;
        //Change the data in RAM
        for (int j = 0; j < data.length; j++) {
            ram[(int) hexToDecimal(address) + j] = data[j];
        }
        System.out.println("Store in RAM");
    }


    //Find the set index and tag, then update the global variables
    public static void findSetAndTag(String adress){

        String adressInBinary = hexToBinary(adress);
        adressInBinary = padBinary(adressInBinary);

        String setIndexInBinary = adressInBinary.substring(64-blockBit-setBit,64-blockBit);
        String tag = adressInBinary.substring(0,64-blockBit-setBit);

        currentSetIndex = (int)binaryToDecimal(setIndexInBinary);
        currentTag = binaryToDecimal(tag);
    }

    //Convert binary to decimal
    public static long binaryToDecimal(String binary){

        long decimal = 0;
        long factor = 1;
        for(int i = binary.length() - 1; i >= 0; i--){
            if(binary.charAt(i) == '1')
                decimal += factor;
            factor = factor * 2;
        }

        return decimal;
    }

    //Convert hex to decimal
    public static long hexToDecimal(String hex){
        return Long.parseLong(hex, 16);
    }

    //Convert hex to binary
    public static String hexToBinary(String hex){

        long decimal = hexToDecimal(hex);
        return Long.toBinaryString(decimal);
    }

    //Convert hex value to bytes
    public static byte[] convertStringToByte(String hex){

        int numberOfBytes = hex.length() / 2; //Each two digit means 1 byte in hex

        byte[] temp = new byte[numberOfBytes];

        int index = 0;
        for(int i = 0; i < numberOfBytes; i++){
            temp[i] = (byte) hexToDecimal(hex.substring(index, index + 2));
            index += 2;
        }

        return temp;
    }

    //During the conversion, we have to do padding for binary
    public static String padBinary(String binary){
        if(binary.length() == 64)
            return binary;

        int numberOfZeros = 64 - binary.length();
        for(int i = 0; i < numberOfZeros; i++)
            binary = "0" + binary;

        return binary;
    }














}
