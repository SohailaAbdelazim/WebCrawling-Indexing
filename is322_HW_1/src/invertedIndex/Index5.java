/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.*;

import static java.lang.Math.log10;
import static java.lang.Math.sqrt;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


/**
 *
 * @author ehab
 */
public class Index5 {

    //--------------------------------------------
    int N = 0; // number of documents in the collection
    public Map<Integer, SourceRecord> sources;  // store the doc_id and the file name.

    public HashMap<String, DictEntry> index; // THe inverted index
    public HashMap<String, DictEntry> biWordIndex; // THe bi word index
    public HashMap<String, DictEntry> positionalIndex; // THe positional index

    //--------------------------------------------

    public Index5() {
        sources = new HashMap<Integer, SourceRecord>();
        index = new HashMap<String, DictEntry>();
        biWordIndex = new HashMap<String, DictEntry>();
        positionalIndex = new HashMap<String,DictEntry>();
    }

    public void setN(int n) {
        N = n;
    }


    //---------------------------------------------
    public void printPostingList(Posting p) {
        // Iterator<Integer> it2 = hset.iterator();
        System.out.print("[");
        while (p.next != null) {
            /// -4- **** complete here ****
            System.out.print("" + p.docId + "," );
            p = p.next;
        }
        // fix get rid of the last comma
        System.out.print(p.docId);
        System.out.println("]");
    }
    public void printPositionalList(Posting p) {
        // Iterator<Integer> it2 = hset.iterator();
        System.out.print("[");

        while (p.next != null) {
            /// -4- **** complete here ****
            System.out.print("" + p.docId +":" );
            System.out.print("(");

            for (int i =0 ; i <p.positions.size()-1; i++){

                System.out.print(p.positions.get(i)+ "," );
            }
            System.out.print(p.positions.get(p.positions.size()-1));
            System.out.println(")");

            p = p.next;
        }
        // fix get rid of the last comma
        System.out.print(p.docId + ":");
        System.out.print("(");

        for (int i =0 ; i <p.positions.size()-1; i++){

            System.out.print(p.positions.get(i)+ "," );
        }

        System.out.print(p.positions.get(p.positions.size()-1));
        System.out.print(")");

        System.out.println("]");
    }

    //---------------------------------------------
    public void printDictionary(HashMap<String,DictEntry> index) {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry dd = (DictEntry) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPostingList(dd.pList);
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());

    }

    public void printPositionalDictionary(HashMap<String,DictEntry> index) {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry dd = (DictEntry) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPositionalList(dd.pList);
            System.out.print("\n");

        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());

    }




    //-----------------------------------------------

    /**
     * Build an index for the given files (documents) stored on disk by reading each file line by line.
     * For each line, the indexOneLine method is called to index each word.
     * During that information about each file is added to the sources hashmap.
     *
     */
    public void buildIndex(String[] files) {  // from disk not from the internet
        int fid = 1;
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                if (!sources.containsKey(fileName)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;
                int flen = 0;
                while ((ln = file.readLine()) != null) {
                    /// -2- **** complete here ****
                    ///**** hint   flen +=  ________________(ln, fid);
                    flen +=  indexOneLine(ln, fid);
                }
                sources.get(fid).length = flen;

            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++;
        }
//           printDictionary();
    }

    /**
     * Builds a biword index for the given files (documents) stored on disk by reading each file line by line.
     * For each line, the indexOneLineBiWords method is called to index pairs of consecutive words (biwords) as a single unit.
     * During this process, file-specific information such as file name and total length of all biwords indexed is tracked and
     * stored in the sources hashmap.
     */
    public void buildBiwordIndex(String[] files) {  // from disk not from the internet
        int fid = 1;
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                // If the file is not already indexed, create a record for it
                if (!sources.containsKey(fileName)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln; // Holds the current line from the file
                int flen = 0; // Total length of indexed biwords in this file
                while ((ln = file.readLine()) != null) {
                    /// -2- **** complete here ****
                    ///**** hint   flen +=  ________________(ln, fid);
                    // Index biwords in the line and accumulate their count
                    flen +=  indexOneLineBiWords(ln, fid);
                }
                // After processing the file, update the total length of indexed biwords
                sources.get(fid).length = flen;

            } catch (IOException e) {   // Handle files that cannot be found or read
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++;
        }
//           printDictionary();
    }

    /**
     * Builds a positional index for the given files (documents) stored on disk by reading each file line by line.
     * For each line, the indexOneLinePositional method is called to index each word along with its position within the file.
     * This allows the index to not only store the occurrence of words but also their specific positions.
     * Each file's specific information, such as its identifier and the total count of words indexed, is maintained in the sources hashmap.
     */
    public void buildPositionalIndex(String[] files) {  // from disk not from the internet
        int fid = 1;
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                // Check if the file is already indexed, if not, create a new record
                if (!sources.containsKey(fileName)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;  // Holds the current line from the file
                int flen = 0;  // Tracks the cumulative number of words indexed in this file
                while ((ln = file.readLine()) != null) {
                    /// -2- **** complete here ****
                    ///**** hint   flen +=  ________________(ln, fid);
                    // Index words in the line with their position and update the word count
                    flen +=  indexOneLinePositional(ln, fid,flen);
                }
                // Update the total number of words indexed for this file in the source record
                sources.get(fid).length = flen;

            } catch (IOException e) {  // If the file is not found, log the error and continue with the next file
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++;
        }
//        printDictionary();
    }
    //----------------------------------------------------------------------------
    /**
     * Index each word in a given line of a specific document by tokenizing the line, ignoring stop words,
     * stemming each word, and updating the index.
     * Return the number of words in the line.
     *
     */
    public int indexOneLine(String ln, int fid) {
        int flen = 0;

        String[] words = ln.split("\\W+");
      //   String[] words = ln.replaceAll("(?:[^a-zA-Z0-9 -]|(?<=\\w)-(?!\\S))", " ").toLowerCase().split("\\s+");
        flen += words.length;
        for (String word : words) {
            word = word.toLowerCase();
            if (stopWord(word)) {
                continue;
            }
            word = stemWord(word);
            // check to see if the word is not in the dictionary
            // if not add it
            if (!index.containsKey(word)) {
                index.put(word, new DictEntry());
            }
            // add document id to the posting list
            if (!index.get(word).postingListContains(fid)) {
                index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term 
                if (index.get(word).pList == null) {
                    index.get(word).pList = new Posting(fid);
                    index.get(word).last = index.get(word).pList;
                } else {
                    index.get(word).last.next = new Posting(fid);
                    index.get(word).last = index.get(word).last.next;
                }
            } else {
                index.get(word).last.dtf += 1;
            }
            //set the term_fteq in the collection
            index.get(word).term_freq += 1;
            if (word.equalsIgnoreCase("lattice")) {

                System.out.println("  <<" + index.get(word).getPosting(1) + ">> " + ln);
            }

        }
        return flen;
    }

    /**
     * Index each pair of consecutive words (biwords) in a given line of a specific document by tokenizing the line,
     * ignoring stop words, stemming each word, and updating the biword index. Biwords are formed by concatenating two
     * consecutive words separated by an underscore. Each new biword and individual word is added to the index if not
     * already present. This function also updates document frequency and term frequency for each biword and word.
     * Returns the count of words processed in the line.
     */
    public int indexOneLineBiWords(String ln, int fid) {
        int flen = 0;

        String[] words = ln.split("\\W+"); // Split line into words
        flen += words.length;
        String prevWord = ""; // Holds the previous word to form biwords
        boolean firstIteration = true; // Flag to check if it's the first word of the line
        for (String word : words) {
            word = word.toLowerCase();
            if (stopWord(word)) { // Ignore stop words
                continue;
            }
            word = stemWord(word); // Apply stemming to reduce word to its base form
            // check to see if the word is not in the dictionary
            // if not add it
            if (!biWordIndex.containsKey(word)) {
                biWordIndex.put(word, new DictEntry());
            }
            if(firstIteration){
                firstIteration = false; // Update flag after first word is processed
                prevWord = word;
            }
            else {
                String combination = prevWord + "_" + word;
                if(!biWordIndex.containsKey(combination)){
                    biWordIndex.put(combination, new DictEntry()); // Add biword to index if not present
                    // add document id to the posting list
                    if (!biWordIndex.get(combination).postingListContains(fid)) {
                        biWordIndex.get(combination).doc_freq += 1; //set doc freq to the number of doc that contain the term
                        if (biWordIndex.get(combination).pList == null) {
                            biWordIndex.get(combination).pList = new Posting(fid);
                            biWordIndex.get(combination).last = biWordIndex.get(combination).pList;
                        } else {
                            biWordIndex.get(combination).last.next = new Posting(fid);
                            biWordIndex.get(combination).last = biWordIndex.get(combination).last.next;
                        }
                    } else {
                        biWordIndex.get(word).last.dtf += 1; // Increment term frequency in document
                    }
                }
                prevWord = word; // Update previous word for next biword formation
            }
            // add document id to the posting list
            if (!biWordIndex.get(word).postingListContains(fid)) {
                biWordIndex.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term
                if (biWordIndex.get(word).pList == null) {
                    biWordIndex.get(word).pList = new Posting(fid);
                    biWordIndex.get(word).last = biWordIndex.get(word).pList;
                } else {
                    biWordIndex.get(word).last.next = new Posting(fid);
                    biWordIndex.get(word).last = biWordIndex.get(word).last.next;
                }
            } else {
                biWordIndex.get(word).last.dtf += 1;
            }
            biWordIndex.get(word).term_freq += 1;  // Increment overall term frequency

            if (word.equalsIgnoreCase("lattice")) { // Special case

                System.out.println("  <<" + biWordIndex.get(word).getPosting(1) + ">> " + ln);
            }
        }
        return flen;
    }
//----------------------------------------------------------------------------

    /**
     * Indexes each word in a given line of a specific document by tokenizing the line, converting words to lowercase,
     * applying stemming, and updating a positional index. Each word's entry in the index includes its positions within the document,
     * This function also checks and updates the dictionary for each word, adds to the posting list,
     * updates document frequency, term frequency, and specific positions for each occurrence of the word.
     */
    public int indexOneLinePositional(String ln, int fid, int position) {

        int flen = 0;

        String[] words = ln.split("\\W+"); // Split the line into words
        flen += words.length;
        int currentPosition = position; // Track the position of each word in the document
        boolean firstIteration = true;
        for (String word : words) {
            word = word.toLowerCase();

            word = stemWord(word); // Apply stemming to reduce the word to its base form

            // Ensure each word is in the positional index
            if (!positionalIndex.containsKey(word)) {
                positionalIndex.put(word, new DictEntry());
            }

            // Add document ID to the posting list and handle positions
            if (!positionalIndex.get(word).postingListContains(fid)) {
                positionalIndex.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term
                if (positionalIndex.get(word).pList == null) {
                    positionalIndex.get(word).pList = new Posting(fid);  // Create a new posting if none exists
                    positionalIndex.get(word).last = positionalIndex.get(word).pList;
                } else {
                    positionalIndex.get(word).last.next = new Posting(fid); // Otherwise, append to the existing posting list
                    positionalIndex.get(word).last = positionalIndex.get(word).last.next;
                }
            } else {
                positionalIndex.get(word).last.dtf += 1;
            }
            // Add the current word position to the posting
            positionalIndex.get(word).last.addPosition(currentPosition);
            //set the term_fteq in the collection
            positionalIndex.get(word).term_freq += 1;
            if (word.equalsIgnoreCase("lattice")) {

                System.out.println("  <<" + positionalIndex.get(word).getPosting(1) + ">> " + ln);
            }
            currentPosition++; // Move to the next word position
        }
        return flen;
    }
//----------------------------------------------------------------------------
    /**
     * Checks if a given word is one of the stop words specified.
     *
     */
    boolean stopWord(String word) {
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
            return true;
        }
        if (word.length() < 2) {
            return true;
        }
        return false;

    }
//----------------------------------------------------------------------------  
   /**
    * Return a word in the root form.
    *
    */
    String stemWord(String word) { //skip for now
        return word;
//        Stemmer s = new Stemmer();
//        s.addString(word);
//        s.stem();
//        return s.toString();
    }

    //----------------------------------------------------------------------------

    /**
     * Find the common document ids between two posting lists and return them as another posting list.
     * Assuming that both posting lists are sorted.
     *
     */
    Posting intersect(Posting pL1, Posting pL2) {
///****  -1-   complete after each comment ****
//   INTERSECT ( p1 , p2 )
//          1  answer ←      {}
        Posting answer = null ; //new Posting(-1) ;
        Posting last = null;
//      2 while p1  != NIL and p2  != NIL
        while(pL1 != null && pL2 !=null){
            //  3 do if docID ( p 1 ) = docID ( p2 )
            if(pL1.docId == pL2.docId){
                //  4   then ADD ( answer, docID ( p1 ))
                // need to be edited! -------------------- answer.add(pL1.docId);
                if(answer == null){
                    answer = new Posting(pL1.docId);
                    last = answer;
                }else{
                    last.next = new Posting(pL1.docId);
                    last = last.next;
                }
                //          5       p1 ← next ( p1 )
                //          6       p2 ← next ( p2 )
                pL1 = pL1.next;
                pL2 = pL2.next;
                //          7   else if docID ( p1 ) < docID ( p2 )
            }else if(pL1.docId < pL2.docId) {
                //          8        then p1 ← next ( p1 )
                pL1 = pL1.next;
            }else{
                //          9        else p2 ← next ( p2 )
                pL2 = pL2.next;
            }

        }
//      10 return answer
        return answer;
    }
    //---------------------------------------------------------------------------------

    /**
     * Find common document IDs between two posting lists where the terms appear within a specified distance `k` of each other.
     * Both posting lists are assumed to be sorted. The function also checks for the exact position of the terms relative to
     * each other, only including those that meet the specified positional criteria in the returned posting list.
     */
    Posting positionalIntersect(Posting pL1, Posting pL2, int k) {
        Posting answer = null ;
        Posting last = null;
        // iterate through both given posting lists
        while(pL1 != null && pL2 !=null){
            // check the common document
            if(pL1.docId == pL2.docId){
                ArrayList<Integer> pp1 = pL1.positions;
                ArrayList<Integer> pp2 = pL2.positions;

                // iterate on positions of the common document
                int i = 0, j = 0;
                while(i < pp1.size() && j < pp2.size()){
                    // in case the terms are consequent and appear in the same order
                    if(pp2.get(j) - pp1.get(i) == k){
                        // answer is not initialized yet
                        if(answer == null){
                            answer = new Posting(pL2.docId);
                            answer.addPosition(pp2.get(j));
                            last = answer;
                        }
                        else {
                            // found another position in the same document
                            if(pL1.docId == last.docId){
                                last.addPosition(pp2.get(j));
                            }
                            // found the first position in new document
                            else {
                                last.next = new Posting(pL2.docId);
                                last.next.addPosition(pp2.get(j));
                                last = last.next;
                            }
                        }
                        i++;
                        j++;
                    }
                    // in case they appear in right order, but they are not consequent
                    else if(pp1.get(i) < pp2.get(j)){
                        i++;
                    }
                    // in case of reverse order
                    else {
                        j++;
                    }
                }
                pL1 = pL1.next;
                pL2 = pL2.next;
            } else if(pL1.docId < pL2.docId) {
                pL1 = pL1.next;
            } else{
                pL2 = pL2.next;
            }
        }
        return answer;
    }

    //---------------------------------------------------------------------------------

    /**
     * Retrieve which documents contain all the words in the given phrase by calling intersect method
     * on their posting lists.
     *
     */
    public String find_24_01(String phrase) { // any number of terms non-optimized search
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;

        //fix this if word is not in the hash table will crash...
        try{
        Posting posting = index.get(words[0].toLowerCase()).pList;
        int i = 1;
        while (i < len) {
            posting = intersect(posting, index.get(words[i].toLowerCase()).pList);
            i++;
        }
        while (posting != null) {
            //System.out.println("\t" + sources.get(num));
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
            posting = posting.next;
        }
        } catch (Exception e) {
            System.out.println("This phrase isn't in the collection! Please try another phrase.");
        }
        return result;
    }

    /**
     * Retrieve which documents contain all biwords and individual words in the given phrase by calling the intersect method
     * on their posting lists. Biwords are specified in double quotes, and individual words are separated by whitespace.
     * This function first extracts all biwords and single words from the phrase, then uses the intersect method to find
     * documents containing all specified terms.
     * It handles cases where terms are not in the index to prevent crashes.
     */
     public String findBiWord(String phrase) { // any number of terms non-optimized search
        String result = "";

        ArrayList <String> words = new ArrayList<>();

        Pattern pattern = Pattern.compile("\"[A-Za-z]+\\s[A-Za-z]+\"");
        Matcher matcher = pattern.matcher(phrase);

         while(matcher.find()){
             String biWord = matcher.group();

             String [] singleWords = phrase.split(biWord);
             phrase = String.join(" " ,singleWords);

             biWord = biWord.substring(1);
             biWord = biWord.substring(0,biWord.length()-1);
             biWord = biWord.replace(" ", "_");
             words.add(biWord);
         }
         if(!phrase.isEmpty()){
             ArrayList<String> temp = new ArrayList<>(Arrays.asList(phrase.split("\\W+")));
             words.addAll(temp);
         }
        for(String word: words){
            if(word.isEmpty()){
                words.remove(word);
            }
        }
        int len = words.size();

        //fix this if word is not in the hash table will crash...
        try{
        Posting posting = biWordIndex.get(words.get(0).toLowerCase()).pList;
        int i = 1;
        while (i < len) {
            posting = intersect(posting, biWordIndex.get(words.get(i).toLowerCase()).pList);
            i++;
        }
        while (posting != null) {
            //System.out.println("\t" + sources.get(num));
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
            posting = posting.next;
        }
        } catch (Exception e) {
            System.out.println("This phrase isn't exist");
        }
        return result;
    }

    /**
     * Retrieves documents containing all the words in the given phrase with each word immediately following the previous word.
     * The function splits the phrase into individual words, then uses the positionalIntersect method to find intersections
     * of posting lists where words are positioned consecutively.
     */
    public String findPositional(String phrase) { // any number of terms non-optimized search
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;

        //fix this if word is not in the hash table will crash...
        try{
            Posting posting = positionalIndex.get(words[0].toLowerCase()).pList;
            int i = 1;
            while (i < len) {
                posting = positionalIntersect(posting, positionalIndex.get(words[i].toLowerCase()).pList, 1);
                i++;
            }
            while (posting != null) {
                //System.out.println("\t" + sources.get(num));
                result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
                posting = posting.next;
            }
        } catch (Exception e) {
            System.out.println("This phrase isn't in the collection! Please try another phrase.");
        }
        return result;
    }
    //---------------------------------
    /**
     * sort the given array of words using bubble sort by looping through the array and comparing
     * the adjacent words.
     * If they are out of order (left word > right word), swap them.
     * Repeat till array is sorted.
     *
     */
    String[] sort(String[] words) {  //bubble sort
        boolean sorted = false;
        String sTmp;
        //-------------------------------------------------------
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < words.length - 1; i++) {
                int compare = words[i].compareTo(words[i + 1]);
                if (compare > 0) {
                    sTmp = words[i];
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    sorted = false;
                }
            }
        }
        return words;
    }

     //---------------------------------
    /**
     * Store the information about the documents used to create the index and the index itself
     * from memory into disk.
     *
     */
    public void store(String storageName,HashMap<String,DictEntry> index) {
        try {
            String separator = File.separator;
            String pathToStorage = "tmp11" + separator + "rl" + separator + "storageName";
            Writer wr = new FileWriter(pathToStorage);
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = " + entry.getValue().title + ", Value = " + entry.getValue().text);
                wr.write(entry.getKey().toString() + ",");
                wr.write(entry.getValue().URL.toString() + ",");
                wr.write(entry.getValue().title.replace(',', '~') + ",");
                wr.write(entry.getValue().length + ","); //String formattedDouble = String.format("%.2f", fee );
                wr.write(String.format("%4.4f", entry.getValue().norm) + ",");
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n");
            }
            wr.write("section2" + "\n");

            Iterator it = index.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                DictEntry dd = (DictEntry) pair.getValue();
                //  System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");
                Posting p = dd.pList;
                while (p != null) {
                    //    System.out.print( p.docId + "," + p.dtf + ":");
                    wr.write(p.docId + "," + p.dtf + ":");
                    p = p.next;
                }
                wr.write("\n");
            }
            wr.write("end" + "\n");
            wr.close();
            System.out.println("=============EBD STORE=============");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//=========================================    
    public boolean storageFileExists(String storageName){
        java.io.File f = new java.io.File("/home/ehab/tmp11/rl/"+storageName);
        if (f.exists() && !f.isDirectory())
            return true;
        return false;
            
    }
//----------------------------------------------------    
    public void createStore(String storageName) {
        try {
            String pathToStorage = "/home/ehab/tmp11/"+storageName;
            Writer wr = new FileWriter(pathToStorage);
            wr.write("end" + "\n");
            wr.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//----------------------------------------------------      
     //load index from hard disk into memory
    public HashMap<String, DictEntry> load(String storageName) {
        try {
            String pathToStorage = "/home/ehab/tmp11/rl/"+storageName;         
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, DictEntry>();
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }
                String[] ss = ln.split(",");
                int fid = Integer.parseInt(ss[0]);
                try {
                    System.out.println("**>>" + fid + " " + ss[1] + " " + ss[2].replace('~', ',') + " " + ss[3] + " [" + ss[4] + "]   " + ss[5].replace('~', ','));

                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    //   System.out.println("**>>"+fid+" "+ ss[1]+" "+ ss[2]+" "+ ss[3]+" ["+ Double.parseDouble(ss[4])+ "]  \n"+ ss[5]);
                    sources.put(fid, sr);
                } catch (Exception e) {

                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            while ((ln = file.readLine()) != null) {
                //     System.out.println(ln);
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }
                String[] ss1 = ln.split(";");
                String[] ss1a = ss1[0].split(",");
                String[] ss1b = ss1[1].split(":");
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx;   //posting
                for (int i = 0; i < ss1b.length; i++) {
                    ss1bx = ss1b[i].split(",");
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            System.out.println("============= END LOAD =============");
            //    printDictionary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return index;
    }

    public void find_07a(String narmerGizaPyramid) {
    }

    public void searchLoop() {
    }
}

//=====================================================================
