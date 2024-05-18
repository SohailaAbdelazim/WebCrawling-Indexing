package crawler;

/**
 *
 * @author ehab
 */
import invertedIndex.DictEntry;
import invertedIndex.Posting;
import invertedIndex.SourceRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.log10;
//==============================================================================

public class WebCrawlerWithDepth {

    private static final int MAX_DEPTH =2;
    private static final int MAX_PER_PAGE = 6;
    int max_docs = 20;
    private HashSet<String> links;
    Map<Integer, SourceRecord> sources;
    //   Map<Integer, String> doc_text;
    int fid = 0;
    int plinks = 0;
    //String storageName;
//==============================================================================

    public WebCrawlerWithDepth() {
        links = new HashSet<>();
        sources = null;
        fid = 0;
    }

    public WebCrawlerWithDepth(invertedIndex.Index5 in) {
        links = new HashSet<>();
        sources = in.sources;
        fid = 0;
    }

    public void setSources(invertedIndex.Index5 in) {
        sources = in.sources;
    }
//==============================================================================

    public String getText(Document document) {
        String pAcc = "";
        Elements p = document.body().getElementsByTag("p");

        for (Element e : p) {
            pAcc += e.text();
        }
        return pAcc;
    }
//==============================================================================
//    Recursive function on the URLs until reach the max depth
    public void getPageLinks(String URL, int depth, invertedIndex.Index5 index) {
       System.out.println("|| URL: [" + URL + "] --------  depth: " + depth + " fid=" + fid + " plinks=" + plinks + "\t|||| ");

        if ((!(links.contains(URL)))
                && (depth < MAX_DEPTH)
                && (fid < max_docs)
                        && ((depth == 1) || (plinks < (MAX_PER_PAGE * (depth + 1))))
                && ((depth == 0)
                || ((depth == 1) && (plinks < ((MAX_PER_PAGE) + 290)))
                || (plinks < ((MAX_PER_PAGE * (depth + 1)) - (plinks / 2))))
                && (!URL.contains("https://.m."))
                && (URL.contains("https://en.w"))
                && (!URL.contains("wiki/Wikipedia"))
                && (!URL.contains("searchInput"))
                && (!URL.contains("wiktionary"))
                && (!URL.contains("#"))
                && (!URL.contains(","))
                && (!URL.contains("Wikiquote"))
                && (!URL.contains("disambiguation"))
                && (!URL.contains("w/index.php"))
                && (!URL.contains("wikimedia"))
                && (!URL.contains("/Privacy_policy"))
                && (!URL.contains("Geographic_coordinate_system"))
                && (!URL.contains(".org/licenses/"))
                && ((!URL.substring(12).contains(":")) || (depth == 0)) // ignor sublink that contain : bu pass the "http:"
                && (!URL.isEmpty())
                && (!URL.contains("Main_Page"))
                && (!URL.contains("mw-head"))) {
            try {
                //??????------------------------------------------------------------------------                
                // *** 1-  add this URL to the  visited list
                                links.add(URL);
                // inititailiz the document element using the Jsoup library

                Document document = Jsoup.connect(URL).get();
                
                //??????------------------------------------------------------------------------                
                // *** 2-  get all links of the page         
                // use document select  with parameter "a[href]")
                Elements linksOnPage = document.select( "a[href]");
                
                //??????------------------------------------------------------------------------
                // *** 3-  get all  paragraphs <p></p>  elements from the page (document)
                Elements paragraphsOnPage = document.select("p");
                
                //??????------------------------------------------------------------------------
                //**** 4-  get the text inside those paragraphs inside the tags <p></p>
                //***       accumulate then into  to String docText
               String docText = "";
               docText += paragraphsOnPage.text();


              //****     build the sourses (given)
               SourceRecord sr = new SourceRecord(fid, URL, document.title(), docText.substring(0, 30));
                sr.length = docText.length();
                sources.put(fid, sr);
                
                //??????------------------------------------------------------------------------
                //**** 5-  pass the docText for the inverted index with the doc id

                index.buildIndexCrawler(fid, docText);
                
                plinks++;  // accumulator for the link in a sub-branch
                fid++;   // current document id
               

                for (Element page : linksOnPage) {
                //**** 6-  handle all the page hyperlinks "linksOnPage" you obtained from step 2 recursively with depth +1
                //             Hint ::    Use  page.attr("abs:href") for each page
                    String newURL = page.attr("abs:href");
                    getPageLinks(newURL,depth+1,index);
                 }
                 plinks--;
            } catch (IOException e) {
                System.err.println("For '" + URL + "': " + e.getMessage());
            }
        }
    }
//==============================================================================

    public void parsePageLinks(String URL, int depth, invertedIndex.Index5 index) {
        System.out.println("--------------- URL: " + URL + " --------  depth: " + depth + " - - - - - - --------- ");

        plinks = 0;
        getPageLinks(URL, depth, index);
    }
//==============================================================================

    public String getSourceName(int id) {
        return sources.get(id).getURL();
    }
//==============================================================================

    void printSources() {
        for (int i = 0; i < sources.size(); i++) {
            System.out.println(">>  " + i + " [" + getSourceName(i) + "]");
        }
    }

    public invertedIndex.Index5 initializeNew(String storageName) {
        invertedIndex.Index5 index = new invertedIndex.Index5();
        setSources(index);
        index.createStore(storageName);
        return index;
    }

    public invertedIndex.Index5 initialize(String storageName) {
        invertedIndex.Index5 index = new invertedIndex.Index5();
        setSources(index);
        setDomainKnowledge(index, storageName);
        index.setN(fid);
        index.store(storageName, index.index);

        return index;
    }

    void setDomainKnowledge(invertedIndex.Index5 index, String domain) {
        parsePageLinks(domain, 0, index);
    }
// compute cosine similarity
 public List<Map.Entry<Integer, Double>> computeScores(String phrase, invertedIndex.Index5 index) {
    int N = sources.size();

    String[] words = phrase.split("\\W+");
    //1 float Scores[N] = 0
    Map<Integer, Double> scores = new HashMap<>(N); // N= collection size (10 files N =10)
    for (int i = 0; i < N; i++) {
        scores.put(i, 0.0);
    }

    //2 Initialize Length[N]
    // double length[] = new double[N];

    //3 for each query term t
    for (String term : words) {
        if (index.index.containsKey(term)) {
        //4 do calculate w t, q and fetch postings list for t
        term = term.toLowerCase();
        int tdf = index.index.get(term).doc_freq; // number of documents that contains the term
        int ttf = index.index.get(term).term_freq; //

        //4.a compute idf
        double idf = log10(N / (double) tdf); // can be computed earlier
        //5 for each pair(doc_id, dtf ) in postings list
        Posting p = index.index.get(term).pList;
        //6 add the term score for (term/doc) to score of each doc
        while (p != null) {
            Double temp = scores.get(p.docId);
            scores.put(p.docId, temp + (1 + log10((double) p.dtf)) * idf);
            p = p.next;
        }
        }
    }

    //Normalize for the length of the doc
    //7 Read the array Length[d]
    //8 for each d
    for (Map.Entry<Integer, Double> entry : scores.entrySet()) {
        Integer docId = entry.getKey();
        Double score = entry.getValue();
        scores.put(docId, score/sources.get(docId).length);
    }

    List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(scores.entrySet());
    entryList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

    // Get the top k entries
    List<Map.Entry<Integer, Double>> topKEntries = entryList.subList(0, Math.min(10, entryList.size()));
    //10 return Top K components of Scores[]
    return topKEntries;
   }

////// Trial 2 --------------------------

    public List<Map.Entry<Integer, Double>> computeScoresV2(String phrase, invertedIndex.Index5 index) {
        // Tokenize the query phrase
        String[] words = phrase.split("\\W+");
        Map<String, Integer> queryTerms = new HashMap<>();
        for (String word : words) {
            word = word.toLowerCase();
            queryTerms.put(word, queryTerms.getOrDefault(word, 0) + 1);
        }

        // Collect all terms in the index
        Set<String> allTerms = new HashSet<>(index.index.keySet());

        // Convert query to a vector
        double[] queryVector = convertToVector(queryTerms, allTerms);

        // Initialize a map to store cosine similarity scores
        Map<Integer, Double> scores = new HashMap<>();

        // Iterate over each document in the sources
        for (int docId : sources.keySet()) {
            // Get document vector
            Map<String, Integer> docTerms = getDocumentVector(docId, index );
            double[] docVector = convertToVector(docTerms, allTerms);

            // Compute cosine similarity
            double score = computeCosineSimilarity(queryVector, docVector);

            // Store the score
            scores.put(docId, score);
        }

        // Convert the scores map to a list of entries and sort by score in descending order
        List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(scores.entrySet());
        entryList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        // Get the top K entries
        List<Map.Entry<Integer, Double>> topKEntries = entryList.subList(0, Math.min(10, entryList.size()));

        // Return the top K components of Scores[]
        return topKEntries;
    }

// Assuming the following methods are already defined in your class

    // Method to get the document vector
    public Map<String, Integer> getDocumentVector(int docId, invertedIndex.Index5 index ) {
        Map<String, Integer> docVector = new HashMap<>();

        for (Map.Entry<String, DictEntry> entry : index.index.entrySet()) {
            String term = entry.getKey();
            DictEntry dictEntry = entry.getValue();

            int termFreq = dictEntry.getPosting(docId);
            if (termFreq > 0) {
                docVector.put(term, termFreq);
            }
        }

        return docVector;
    }

    // Method to convert terms to vector
    private double[] convertToVector(Map<String, Integer> terms, Set<String> allTerms) {
        double[] vector = new double[allTerms.size()];
        int i = 0;
        for (String term : allTerms) {
            vector[i++] = terms.getOrDefault(term, 0);
        }
        return vector;
    }

    // Method to compute cosine similarity
    private double computeCosineSimilarity(double[] vec1, double[] vec2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            normA += Math.pow(vec1[i], 2);
            normB += Math.pow(vec2[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-6);
    }


}