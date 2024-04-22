/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
            System.out.println("2_ Biword index");
            System.out.println("3_ Positional index");
            System.out.println("Press any number to Exit");

            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();


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
                System.out.println("Print search phrase: \n");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                phrase = in.readLine();

                System.out.println(index.findBiWord(phrase));
            } else if (choice == 3){
                index.buildPositionalIndex(fileList);
                index.store("index",index.positionalIndex);
                index.printPositionalDictionary(index.positionalIndex);

                String phrase = "";
                System.out.println("Print search phrase: \n");
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                phrase = in.readLine();

                System.out.println(index.findPositional(phrase));
            } else {
                break;
            }


        }

    }
}
