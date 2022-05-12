package company;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;


public class Main {
    private static ExecutorService executor;
    private static ConcurrentHashMap<Character,Integer> results=new ConcurrentHashMap<>();
    private static HashMap<Character,Integer> resultsStream=new HashMap<>();
    private static int numberOfThreads;
    private static final int sizeReadBuffer=16384*64;
    private static volatile boolean endOfFile=false;

    public static void main(String[] args){
        String inputFileName="file.txt";
        int lengthFile = 102400000;
        writeFile(inputFileName, lengthFile);//заполнение файла символами
        numberOfThreads= Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(numberOfThreads);

        long startTime = System.currentTimeMillis();
        try(BufferedReader FileStreamReader=new BufferedReader(new FileReader(inputFileName))) {
            readParallelThread(FileStreamReader);//вычисление Parallel Thread
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime =System.currentTimeMillis();
        long durationThread= (endTime - startTime);

        startTime = System.currentTimeMillis();
        readStreamFile(inputFileName);//вычисление Stream
        endTime =System.currentTimeMillis();
        long durationStream= (endTime - startTime);

        System.out.println("Thread pool duration time : "+durationThread);
        System.out.println("Stream duration time : "+durationStream);
        System.out.println("Equals : "+resultsStream.equals(results));
    }



    public static void readParallelThread(BufferedReader fileStreamReader) throws IOException {
        List<Future<?>> futures = parallelJobs(fileStreamReader);
        awaitCompletion(futures);
        //printResults();
    }

    private static List<Future<?>> parallelJobs(BufferedReader fileStreamReader) {
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            futures.add(executor.submit(makeJob(fileStreamReader)));
        }
        return futures;
    }

    private static Runnable makeJob(BufferedReader fileStreamReader){
        return () -> {
            try {
                String stringTemp;
                System.out.println("Thread "+Thread.currentThread().getName()+" run");

                do {
                    stringTemp=readFile(fileStreamReader);
                        System.out.println("Thread " + Thread.currentThread().getName() + " is calculating ");
                        Map<Character, Integer> tempMap = stringTemp.chars().
                                collect(HashMap::new, (map, value) -> {
                            map.merge((char) value, 1, Integer::sum);
                        }, HashMap::putAll);
                        tempMap.forEach((k, v) -> results.merge(k, v, Integer::sum));
                }while (!endOfFile);

                System.out.println("Thread "+Thread.currentThread().getName()+" end");
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }


    public static String readFile(BufferedReader fileStreamReader) throws IOException {
        char[] buffer=new char[sizeReadBuffer];
        int countReadingChar=0;
        if (!endOfFile) {
            synchronized (fileStreamReader) {
                countReadingChar = fileStreamReader.read(buffer, 0, sizeReadBuffer);
                endOfFile = (countReadingChar < sizeReadBuffer);
            }
        }
        return new String(buffer).substring(0,countReadingChar==-1?0:countReadingChar);
    }

    private static void awaitCompletion(List<Future<?>>
                                         futures) {
        futures.forEach((future) -> {
            try {
                future.get();
            } catch (InterruptedException |
                    ExecutionException e) {
                e.printStackTrace();
            }
        });
        executor.shutdown();
        System.out.println("ExecutorService " + Thread.currentThread().getName() + " shutdown");
    }

    private static void printResults() {
        results.entrySet().forEach(System.out::println);
    }




    private static void readStreamFile(String inputFileName) {
        try(BufferedReader FileStreamReader=new BufferedReader(new FileReader(inputFileName))) {
            resultsStream = FileStreamReader.lines().parallel().map(String::chars).flatMapToInt(x -> x).collect(HashMap::new, (map, value) -> {
                map.merge((char) value, 1, Integer::sum);
                }, HashMap::putAll);
            //resultsStream.entrySet().forEach(System.out::println);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private static void writeFile(String inputFileName, int length) {
        Random random=new Random();

        try(BufferedWriter streamWriter=new BufferedWriter(new FileWriter(inputFileName))){
            streamWriter.write(random.ints(32, 126)
                .mapToObj(i -> (char) i)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString());
            streamWriter.flush();
            }
                catch(IOException ex) {
                    System.out.println(ex.getMessage());
                }
    }
}

