package edu.unh.cs753853.team1.ranking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs753853.team1.utils.ProjectConfig;

public class LuceneDefault {

	final private String INDEX_DIRECTORY = ProjectConfig.INDEX_DIRECTORY;
	private QueryParser parser = null;
	private IndexSearcher searcher = null;
	private int numdocs = 100;
	private ArrayList<String> queryList;
	private HashMap<String, ArrayList<DocumentResult>> queryResults;

	public LuceneDefault(ArrayList<String> pl, int n) throws ParseException, IOException {

		numdocs = n;
		queryList = pl;
		parser = new QueryParser("postbody", new StandardAnalyzer());
		searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(INDEX_DIRECTORY).toPath()))));

	}

	public HashMap<String, ArrayList<DocumentResult>> getResults() throws IOException {
		try {
			for (String queryStr : queryList) {
				Query q = parser.parse(queryStr);
				TopDocs tds = searcher.search(q, numdocs);
				ScoreDoc[] retDocs = tds.scoreDocs;

				ArrayList<DocumentResult> rankedList = new ArrayList<>();

				for (int i = 0; i < retDocs.length; i++) {
					Document doc = searcher.doc(retDocs[i].doc);
					int docId = Integer.parseInt(doc.get("postid"));
					// String docBody = doc.get("postbody");
					System.out.println(docId + " === " + retDocs[i].score);
					DocumentResult ranked = new DocumentResult(docId, retDocs[i].score);
					ranked.setRank(i);
					rankedList.add(ranked);
				}
				if (rankedList.size() > 0) {
					queryResults.put(queryStr, rankedList);
				} else {
					System.out.println("Empty result for " + queryStr);
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return queryResults;

	}
}
