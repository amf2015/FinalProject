package edu.unh.cs753853.team1.ranking;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs753853.team1.utils.ProjectConfig;
import edu.unh.cs753853.team1.utils.ProjectUtils;

//Bigram Language Model with Laplace smoothing.
public class LanguageModel_BL {

	final private String INDEX_DIRECTORY = ProjectConfig.INDEX_DIRECTORY;
	private QueryParser parser = null;
	private IndexSearcher searcher = null;
	private int numdocs = 100;
	private ArrayList<String> queryPageList;
	private HashMap<String, ArrayList<DocumentResult>> queryResults;

	// private String TEAM_METHOD = "Team1-Bigram";

	public LanguageModel_BL(ArrayList<String> pl, int n) throws ParseException, IOException {

		numdocs = n;
		queryPageList = pl;

		String fields[] = { "posttitle", "postbody" };
		parser = new MultiFieldQueryParser(fields, new StandardAnalyzer());

		searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(INDEX_DIRECTORY).toPath()))));

		SimilarityBase bl_sim = new SimilarityBase() {

			@Override
			protected float score(BasicStats stats, float freq, float docLen) {
				return freq;
			}

			@Override
			public String toString() {
				return null;
			}
		};
		searcher.setSimilarity(bl_sim);
	}

	public HashMap<String, ArrayList<DocumentResult>> getReulst() {
		queryResults = new HashMap<>();

		for (String pageQuery : queryPageList) {
			ArrayList<DocumentResult> rankedDocs = new ArrayList<DocumentResult>();
			HashMap<Integer, Float> result_map = getRankedDocuments(pageQuery);

			int rankCount = 0;
			for (Map.Entry<Integer, Float> entry : result_map.entrySet()) {
				int docId = entry.getKey();
				Float score = entry.getValue();

				DocumentResult docResult = new DocumentResult(docId, score);
				docResult.setRank(rankCount);
				rankCount++;

				rankedDocs.add(docResult);
			}

			queryResults.put(pageQuery, rankedDocs);
		}
		return queryResults;

	}

	public void generateResults(String runfile) throws IOException, ParseException {

		queryResults = new HashMap<>();

		for (String pageQuery : queryPageList) {
			ArrayList<DocumentResult> rankedDocs = new ArrayList<DocumentResult>();
			HashMap<Integer, Float> result_map = getRankedDocuments(pageQuery);

			int rankCount = 0;
			for (Map.Entry<Integer, Float> entry : result_map.entrySet()) {
				int docId = entry.getKey();
				Float score = entry.getValue();

				DocumentResult docResult = new DocumentResult(docId, score);
				docResult.setRank(rankCount);
				rankCount++;

				rankedDocs.add(docResult);
			}

			queryResults.put(pageQuery, rankedDocs);
		}

		ProjectUtils.substatus("Writing results to: " + runfile);
		FileWriter runfileWriter = new FileWriter(new File(runfile));
		for (Map.Entry<String, ArrayList<DocumentResult>> results : queryResults.entrySet()) {
			String query = results.getKey();
			ArrayList<DocumentResult> list = results.getValue();
			for (int i = 0; i < list.size(); i++) {
				DocumentResult dr = list.get(i);
				runfileWriter.write(query.replace(" ", "-") + " Q0 " + dr.getId() + " " + dr.getRank() + " "
						+ dr.getScore() + " team1-LM_BL\n");
			}
		}
		runfileWriter.close();

	}

	public HashMap<Integer, Float> getRankedDocuments(String queryStr) {
		HashMap<Integer, Float> doc_score = new HashMap<Integer, Float>();

		try {

			IndexReader ir = searcher.getIndexReader();
			TermQuery postq = new TermQuery(new Term("postbody", queryStr));
			TermQuery titleq = new TermQuery(new Term("posttitle", queryStr));

			// QueryScore of post body (postbody)
			TopDocs topDocs_body = searcher.search(postq, numdocs);
			ScoreDoc[] hits_body = topDocs_body.scoreDocs;

			for (int i = 0; i < hits_body.length; i++) {
				Document doc = searcher.doc(hits_body[i].doc);

				int docId = Integer.parseInt(doc.get("postid"));
				String docBody = doc.get("postbody");
				ArrayList<Float> p_wt = new ArrayList<Float>();

				ArrayList<String> bigram_list = analyzeByBigram(docBody);
				ArrayList<String> unigram_list = analyzeByUnigram(docBody);
				ArrayList<String> query_list = analyzeByUnigram(queryStr);

				// Size of vocabulary
				int size_of_voc = getSizeOfVocabulary(unigram_list);
				int size_of_doc = unigram_list.size();

				String pre_term = "";
				for (String term : query_list) {
					if (pre_term == "") {
						int tf = countExactStrFreqInList(term, unigram_list);
						float p = laplaceSmoothingWith1(tf, size_of_doc, size_of_voc);
						p_wt.add(p);
					} else {
						// Get total occurrences with given term.
						String wildcard = pre_term + " ";
						int tf_given_term = countStrFreqInList(wildcard, bigram_list);

						// Get occurrences of term with given term.
						String str = pre_term + " " + term;
						int tf = countExactStrFreqInList(str, bigram_list);
						float p = laplaceSmoothingWith1(tf_given_term, tf, size_of_voc);
						p_wt.add(p);
					}
					pre_term = term;
				}

				// Caculate score with log;
				// System.out.println(p_wt);
				float score = getScoreByPListWithLog(p_wt);
				doc_score.put(docId, score);

			}

			// QueryScore of post title (posttitle)
			TopDocs topDocs_title = searcher.search(titleq, numdocs);
			ScoreDoc[] hits_title = topDocs_title.scoreDocs;

			for (int i = 0; i < hits_title.length; i++) {
				Document doc = searcher.doc(hits_title[i].doc);

				int docId = Integer.parseInt(doc.get("postid"));
				String docBody = doc.get("posttitle");
				ArrayList<Float> p_wt = new ArrayList<Float>();

				ArrayList<String> bigram_list = analyzeByBigram(docBody);
				ArrayList<String> unigram_list = analyzeByUnigram(docBody);
				ArrayList<String> query_list = analyzeByUnigram(queryStr);

				// Size of vocabulary
				int size_of_voc = getSizeOfVocabulary(unigram_list);
				int size_of_doc = unigram_list.size();

				String pre_term = "";
				for (String term : query_list) {
					if (pre_term == "") {
						int tf = countExactStrFreqInList(term, unigram_list);
						float p = laplaceSmoothingWith1(tf, size_of_doc, size_of_voc);
						p_wt.add(p);
					} else {
						// Get total occurrences with given term.
						String wildcard = pre_term + " ";
						int tf_given_term = countStrFreqInList(wildcard, bigram_list);

						// Get occurrences of term with given term.
						String str = pre_term + " " + term;
						int tf = countExactStrFreqInList(str, bigram_list);
						float p = laplaceSmoothingWith1(tf_given_term, tf, size_of_voc);
						p_wt.add(p);
					}
					pre_term = term;
				}

				// Caculate score with log;
				float score = getScoreByPListWithLog(p_wt);

				if (doc_score.get(docId) != null) {
					float total_score = doc_score.get(docId) + score;
					doc_score.put(docId, total_score);
				} else {
					doc_score.put(docId, score);
				}

			}

		} catch (Throwable e) {

			e.printStackTrace();
		}

		return sortByValue(doc_score);
	}

	// Get score from list of p.
	private static float getScoreByPListWithLog(ArrayList<Float> p_list) {
		float score = 0;
		for (Float wt : p_list) {
			score = (float) ((float) score + Math.log(wt));
		}
		return score;

	}

	// Get exact count.
	private static int countExactStrFreqInList(String term, ArrayList<String> list) {
		int occurrences = Collections.frequency(list, term);
		return occurrences;
	}

	// Get count with wildcard.
	private static int countStrFreqInList(String term, ArrayList<String> list) {
		int occurrences = 0;
		for (int i = 0; i < list.size(); i++) {
			String str = list.get(i);
			if (str.contains(term)) {
				occurrences++;
			}

		}
		return occurrences;
	}

	private static float laplaceSmoothingWith1(int tf_given_term, int tf, int size_of_v) {
		float p = (float) (tf_given_term + 1) / (float) (tf + size_of_v);
		return p;
	}

	private static int getSizeOfVocabulary(ArrayList<String> unigramList) {
		ArrayList<String> list = new ArrayList<String>();
		Set<String> hs = new HashSet<>();

		hs.addAll(unigramList);
		list.addAll(hs);
		return list.size();
	}

	private static ArrayList<String> analyzeByBigram(String inputStr) throws IOException {
		Reader reader = new StringReader(inputStr);
		// System.out.println("Input text: " + inputStr);
		ArrayList<String> strList = new ArrayList<String>();
		Analyzer analyzer = new BigramAnalyzer();
		TokenStream tokenizer = analyzer.tokenStream("content", inputStr);

		CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
		tokenizer.reset();
		while (tokenizer.incrementToken()) {
			String token = charTermAttribute.toString();
			strList.add(token);
			// System.out.println(token);
		}
		tokenizer.end();
		tokenizer.close();
		return strList;
	}

	private static ArrayList<String> analyzeByUnigram(String inputStr) throws IOException {
		Reader reader = new StringReader(inputStr);
		// System.out.println("Input text: " + inputStr);
		ArrayList<String> strList = new ArrayList<String>();
		Analyzer analyzer = new UnigramAnalyzer();
		TokenStream tokenizer = analyzer.tokenStream("content", inputStr);

		CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
		tokenizer.reset();
		while (tokenizer.incrementToken()) {
			String token = charTermAttribute.toString();
			strList.add(token);
			// System.out.println(token);
		}
		tokenizer.end();
		tokenizer.close();
		return strList;
	}

	// Sort Descending HashMap<String, float>Map by its value
	private static HashMap<Integer, Float> sortByValue(Map<Integer, Float> unsortMap) {

		List<Map.Entry<Integer, Float>> list = new LinkedList<Map.Entry<Integer, Float>>(unsortMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<Integer, Float>>() {

			public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		HashMap<Integer, Float> sortedMap = new LinkedHashMap<Integer, Float>();
		for (Map.Entry<Integer, Float> entry : list)

		{
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public static void writeStrListToRunFile(ArrayList<String> strList, String path) {
		// write to run file.

		BufferedWriter bWriter = null;
		FileWriter fWriter = null;

		try {
			fWriter = new FileWriter(path);
			bWriter = new BufferedWriter(fWriter);

			for (String line : strList) {

				bWriter.write(line);
				bWriter.newLine();
			}

			ProjectUtils.substatus("Write all ranking result to run file: " + path);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bWriter != null) {
					bWriter.close();
				}
				if (fWriter != null) {
					fWriter.close();
				}
			} catch (IOException ee) {
				ee.printStackTrace();
			}
		}

	}

	public ArrayList<DocumentResult> getResultsForQuery(String query) {
		return this.queryResults.get(query);
	}

}