package company;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException {
	    String inputFileName="file.txt";
        int Buffer=1024*2;

        int length = 102400000;
        Random random=new Random();

        try(BufferedWriter streamWriter=new BufferedWriter(new FileWriter(inputFileName),Buffer)){
            streamWriter.write(random.ints(48, 122)
                .filter(i -> (i < 58 || i > 64) && (i < 91 || i > 96))
                .mapToObj(i -> (char) i)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString());
            streamWriter.flush();
            }
                catch(IOException ex){
                System.out.println(ex.getMessage());
            }


        long startTime = System.currentTimeMillis();
        try(BufferedReader FileStreamReader=new BufferedReader(new FileReader(inputFileName),Buffer)) {
            Map<Character,Integer> mapp = FileStreamReader.lines().parallel().map(String::chars).flatMapToInt(x -> x).collect(HashMap::new, (map, value) -> {
            map.merge((char) value, 1, Integer::sum);
            }, HashMap::putAll);
/*
            for(Map.Entry<Character, Integer> item:mapp.entrySet()){
                System.out.println(item.getKey()+":"+ item.getValue().toString());
            }
*/
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        long endTime =System.currentTimeMillis();
        long durationParallel= (endTime - startTime);


        startTime = System.currentTimeMillis();
        try(BufferedReader FileStreamReader=new BufferedReader(new FileReader(inputFileName),Buffer)) {
            Map<Character,Integer> mapp1 = FileStreamReader.lines().map(String::chars).flatMapToInt(x -> x).collect(HashMap::new, (map, value) -> {
            map.merge((char) value, 1, Integer::sum);
            }, HashMap::putAll);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        endTime =System.currentTimeMillis();
        long duration= (endTime - startTime);

        System.out.println("Duration time : "+duration);
        System.out.println("Parallel duration time : "+durationParallel);

    }
}
