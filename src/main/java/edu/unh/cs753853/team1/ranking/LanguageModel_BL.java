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
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs753853.team1.DocumentResults;
import edu.unh.cs753853.team1.utils.ProjectConfig;

//Bigram Language Model with Laplace smoothing. 
public class LanguageModel_BL {

	final private String INDEX_DIRECTORY = ProjectConfig.INDEX_DIRECTORY;
	private QueryParser parser = null;
	private IndexSearcher searcher = null;
	private int numdocs;
	private ArrayList<String> queryPageList;
	private HashMap<String, ArrayList<DocumentResults>> queryResults;

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

	public void generateResults(String runfiles) throws IOException, ParseException {

		queryResults = new HashMap<>();

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
	private static HashMap<String, Float> sortByValue(Map<String, Float> unsortMap) {

		List<Map.Entry<String, Float>> list = new LinkedList<Map.Entry<String, Float>>(unsortMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {

			public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		HashMap<String, Float> sortedMap = new LinkedHashMap<String, Float>();
		for (Map.Entry<String, Float> entry : list)

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

			System.out.println("Write all ranking result to run file: " + path);
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

}