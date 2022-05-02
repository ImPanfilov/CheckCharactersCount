package company;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
	    String inputFileName="file.txt";
        int Buffer=1024;

        try(BufferedReader FileStreamReader=new BufferedReader(new FileReader(inputFileName),Buffer)) {
            Map<Character,Integer> mapp = FileStreamReader.lines().parallel().map(String::chars).flatMapToInt(x -> x).collect(HashMap::new, (map, value) -> {
            map.merge((char) value, 1, Integer::sum);
            }, HashMap::putAll);

        for(Map.Entry<Character, Integer> item:mapp.entrySet()){
            System.out.println(item.getKey()+":"+ item.getValue().toString());
            }


    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
    }
}
