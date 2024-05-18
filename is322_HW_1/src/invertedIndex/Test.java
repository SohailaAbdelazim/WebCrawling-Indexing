/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import crawler.WebCrawlerWithDepth;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author ehab
 */
public class Test {

    public static void main(String args[]) throws IOException {
        Index5 index = new Index5();
        //|**  change it to your collection directory 
        //|**  in windows "C:\\tmp11\\rl\\collection\\"       
       // String files = "/home/ehab/tmp11/rl/collection/";
        String separator = File.separator;
        String files = "tmp11" + separator + "rl" + separator + "collection" + separator;

        File file = new File(files);
        //|** String[] 	list()
        //|**  Returns an array of strings naming the files and directories in the directory denoted by this abstract pathname.
        String[] fileList = file.list();

        fileList = index.sort(fileList);
        index.N = fileList.length;

        for (int i = 0; i < fileList.length; i++) {
            fileList[i] = files + fileList[i];
        }

        while(true){
            System.out.println("Welcome! Which type of index you want to process your query?");
            System.out.println("1_ Inverted index");
            System.out.println("2_ Biword index/mix ");
            System.out.println("3_ Positional index");
            System.out.println("4_ WebCrawler");
            System.out.println("Press any number to Exit");
            int choice = 0;
            Scanner scanner = new Scanner(System.in);
            try {
                choice = scanner.nextInt();

            }
            catch (Exception e)
            {
                System.out.println("Invalid input!");
                continue;
            }

            if(choice == 1){
                index.buildIndex(fileList);
                index.store("index",index.index);
                index.printDictionary(index.index);

                String phrase = "";
                System.out.println("Print search phrase: \n");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                phrase = in.readLine();

                System.out.println(index.find_24_01(phrase));
            } else if(choice == 2){
                index.buildBiwordIndex(fileList);
                index.store("index",index.biWordIndex);
                index.printDictionary(index.biWordIndex);

                String phrase = "";
                System.out.println("Print search phrase: \nplease enter words separated by space or within \" \" for Biword search:");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                phrase = in.readLine();

                System.out.println(index.findBiWord(phrase));
            } else if (choice == 3){
                index.buildPositionalIndex(fileList);
                index.store("index",index.positionalIndex);
                index.printPositionalDictionary(index.positionalIndex);

                String phrase = "";
                System.out.println("Enter search phrase: \n");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                phrase = in.readLine();

                System.out.println(index.findPositional(phrase));
            } else if (choice == 4){
                WebCrawlerWithDepth wc = new WebCrawlerWithDepth();

                index = wc.initialize("https://en.wikipedia.org/wiki/List_of_pharaohs"); //   ukraine
                index.printDictionary(index.index);

                String phrase = "";
                System.out.println("Enter search phrase: \n");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                phrase = in.readLine();

                System.out.println(index.find_24_01(phrase)); //"narmer giza pyramid"

                List<Map.Entry<Integer, Double>> top10 = wc.computeScores(phrase, index);
                System.out.println("Top " + 10 + " entries:");
                for (Map.Entry<Integer, Double> entry : top10) {
                    System.out.println("Page Title: " + index.sources.get(entry.getKey()).title + ", score: " + entry.getValue());
                }
                System.out.println();
            } else {
                break;
            }
        }

    }
}
