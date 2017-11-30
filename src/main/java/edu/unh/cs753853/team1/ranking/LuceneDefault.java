package edu.unh.cs753853.team1.ranking;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.FSDirectory;

public class LuceneDefault {
	// Lucene tools
    private IndexSearcher searcher;
    private QueryParser parser;

    // List of pages to query
    private ArrayList<String> pageList;

    // Number of documents to return
    private int numDocs;

    // Map of queries to map of Documents to scores for that query
    private HashMap<String, ArrayList<String>> queryResults;


    public LuceneDefault(ArrayList<String> pl, int n) throws ParseException, IOException
    {

        numDocs = n; // Get the (max) number of documents to return
        pageList = pl; // Each page title will be used as a query

        // Parse the parabody field using StandardAnalyzer
        String fields[] = {"posttitle", "postbody"};
        parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());

        // Create an index searcher
        String INDEX_DIRECTORY = "index";
        searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(INDEX_DIRECTORY).toPath()))));
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
        {
	        	Query q;
	    		TopDocs tds;
	    		ScoreDoc[] retDocs;
	    		ArrayList<String> runStrings = new ArrayList<String>();
	    		
    			String tmpQ = page;
    			try {
    				q = parser.parse(tmpQ);
    				tds = searcher.search(q, numDocs);
    				retDocs = tds.scoreDocs;
    				Document d;
    				
    				for (int i = 0; i < retDocs.length; i++) {
    					d = searcher.doc(retDocs[i].doc);
    					String runFileString = tmpQ + " Q0 "
    							+ d.get("postid") + " " + i + " "
    							+ tds.scoreDocs[i].score + " team1-" + "lucene";
    					runStrings.add(runFileString);
    				}
    				queryResults.put(tmpQ, runStrings);
    			} catch (ParseException e) {
    				e.printStackTrace();
    			}
    		
        
        }

        System.out.println("LuceneDefault writing results to: " + runfile);
        FileWriter runfileWriter = new FileWriter(new File(runfile));
        for(Map.Entry<String, ArrayList<String>> results: queryResults.entrySet())
        {
            String query = results.getKey();
            ArrayList<String> list = results.getValue();
            for(int i = 0; i < list.size(); i++)
            {
                runfileWriter.write(list.get(i) + "\n");
            }
        }
        runfileWriter.close();


    }

//    public HashMap<String, ArrayList<String>> getQueryResults()
//    {
//        return this.queryResults;
//    }
}
