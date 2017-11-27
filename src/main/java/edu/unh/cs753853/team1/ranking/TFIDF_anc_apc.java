package edu.unh.cs753853.team1.ranking;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.util.*;

//class ResultComparator implements Comparator<DocumentResult>
//{
//    public int compare(DocumentResult d2, DocumentResult d1)
//    {
//        if(d1.getScore() < d2.getScore())
//            return -1;
//        if(d1.getScore() == d2.getScore())
//            return 0;
//        return 1;
//    }
//}

public class TFIDF_anc_apc {
    // Lucene tools
    private IndexSearcher searcher;
    private QueryParser parser;

    // List of pages to query
    private ArrayList<String> pageList;

    // Number of documents to return
    private int numDocs;

    // Map of queries to map of Documents to scores for that query
    private HashMap<String, ArrayList<DocumentResult>> queryResults;


    public TFIDF_anc_apc(ArrayList<String> pl, int n) throws ParseException, IOException
    {

        numDocs = n; // Get the (max) number of documents to return
        pageList = pl; // Each page title will be used as a query

        // Parse the parabody field using StandardAnalyzer
        String fields[] = {"posttitle", "postbody"};
        parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());

        // Create an index searcher
        String INDEX_DIRECTORY = "index";
        searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(INDEX_DIRECTORY).toPath()))));

        // Set our own similarity class which computes tf[t,d]
        SimilarityBase anc_apc = new SimilarityBase() {
            protected float score(BasicStats stats, float freq, float docLen) {
            		float num = 0.5f * freq;
            		return num;
//                return (float)(1 + Math.log10(freq));
            }

            @Override
            public String toString() {
                return null;
            }
        };
        searcher.setSimilarity(anc_apc);
    }

    /**
     *
     * @param runfile   The name of the runfile to output to
     * @throws IOException
     * @throws ParseException
     */
    public void dumpScoresTo(String runfile) throws IOException, ParseException
    {

        queryResults = new HashMap<>(); // Maps query to map of Documents with TF-IDF score

        for(String page:pageList)
        {   // For every page in .cbor.outline
            // We need...

            HashMap<Integer, Float> scores = new HashMap<>();          // Mapping of each Document to its score
            HashMap<Integer, DocumentResult> docMap = new HashMap<>();
            HashMap<TermQuery, Float> queryweights = new HashMap<>();   // Mapping of each term to its query tf
            ArrayList<TermQuery> terms = new ArrayList<>();             // List of every term in the query
            PriorityQueue<DocumentResult> docQueue = new PriorityQueue<>(new ResultComparator());
            ArrayList<DocumentResult> docResults = new ArrayList<>();

            float maxQueryTF = 0;
            for(String term: page.split(" "))
            {   // For every word in page name...
                // Take word as query term for parabody
                TermQuery postq = new TermQuery(new Term("postbody", term));
                TermQuery titleq = new TermQuery(new Term("posttitle", term));
                terms.add(postq);
                terms.add(titleq);

                // Add one to our term weighting every time it appears in the query
                queryweights.put(postq, queryweights.getOrDefault(postq, 0.0f)+1.0f);
                queryweights.put(titleq, queryweights.getOrDefault(titleq, 0.0f)+1.0f);
                if(queryweights.get(postq) > maxQueryTF) {
                		maxQueryTF = queryweights.get(postq);
                } 
                if (queryweights.get(titleq) > maxQueryTF) {
                		maxQueryTF = queryweights.get(titleq);
                }
            }
            for(TermQuery query: terms)
            {   // For every Term

                // Get our Index Reader for helpful statistics
                IndexReader reader = searcher.getIndexReader();

                // If document frequency is zero, set DF to 1; else, set DF to document frequency
                float DF = (reader.docFreq(query.getTerm()) == 0) ? 0 : reader.docFreq(query.getTerm());

                // Calculate TF-IDF for the query vector
//                float qTF_num = (float)(1 + Math.log10(queryweights.get(query)));   // Logarithmic term frequency
                float qTF_num = (float)(0.5 * queryweights.get(query));
                float qTF = 0.5f + (qTF_num / maxQueryTF);
                // Augmented term frequency
                float qProb = (float)(Math.log10((reader.numDocs() - DF)/DF));          // Prob idf document frequency
                float qWeight = qTF * qProb;                                     // Final calculation

                // Store query weight for later calculations
                queryweights.put(query, qWeight);

                // Get the top 100 documents that match our query
                TopDocs tpd = searcher.search(query, numDocs);
                // get max tf for each document
                for(int i = 0; i < tpd.scoreDocs.length; i++)
                {   // For every returned document...
                    Document doc = searcher.doc(tpd.scoreDocs[i].doc);                  // Get the document
                    int docId = Integer.parseInt(doc.get("postid"));
                    float numerator = Float.parseFloat(doc.get("maxtf"));
              
                    double score = (0.5f + (numerator / tpd.scoreDocs[i].score)) * queryweights.get(query);    // Calculate TF-IDF for document

                    DocumentResult dResults = docMap.get(docId);
                    if(dResults == null)
                    {
                        dResults = new DocumentResult(docId, (float)score);
                    }
                    float prevScore = dResults.getScore();
                    // Store score for later use
                    scores.put(Integer.parseInt(doc.get("postid")), (float)(prevScore+score));
                }
            }

            // Get cosine Length
            float cosineLength = 0.0f;
            for(Map.Entry<Integer, Float> entry: scores.entrySet())
            {
                Float score = entry.getValue();

                cosineLength = (float)(cosineLength + Math.pow(score, 2));
            }
            cosineLength = (float)(Math.sqrt(cosineLength));

            // Normalization of scores
            for(Map.Entry<Integer, Float> entry: scores.entrySet())
            {   // For every document and its corresponding score...
                int docId = entry.getKey();
                Float score = entry.getValue();

                // Normalize the score
                scores.put(docId, score/scores.size());

                DocumentResult docResult = new DocumentResult(docId, score);
                docQueue.add(docResult);
            }

            int rankCount = 0;
            DocumentResult current;
            while((current = docQueue.poll()) != null)
            {
                current.setRank(rankCount);
                docResults.add(current);
                rankCount++;
                if(rankCount >= numDocs)
                    break;
            }

            // Map our Documents and scores to the corresponding query
            queryResults.put(page, docResults);
        }


        System.out.println("TFIDF_anc_apc writing results to: " + runfile);
        FileWriter runfileWriter = new FileWriter(new File(runfile));
        for(Map.Entry<String, ArrayList<DocumentResult>> results: queryResults.entrySet())
        {
            String query = results.getKey();
            ArrayList<DocumentResult> list = results.getValue();
            for(int i = 0; i < list.size(); i++)
            {
                DocumentResult dr = list.get(i);
                runfileWriter.write(query.replace(" ", "-") + " Q0 " + dr.getId() + " " + dr.getRank() + " " + dr.getScore() + " team1-TFIDF_anc_apc\n");
            }
        }
        runfileWriter.close();


    }

    public HashMap<String, ArrayList<DocumentResult>> getQueryResults()
    {
        return this.queryResults;
    }



}
