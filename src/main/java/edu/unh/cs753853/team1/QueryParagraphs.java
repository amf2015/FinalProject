package edu.unh.cs753853.team1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class QueryParagraphs {

	private IndexSearcher is = null;
	private QueryParser qp = null;
	private boolean customScore = false;

	// directory structure..
	static final String INDEX_DIRECTORY = "index";
	static final private String Cbor_FILE = "test200.cbor/train.test200.cbor.paragraphs";
	static final private String Cbor_OUTLINE = "test200.cbor/train.test200.cbor.outlines";
	static final private String OUTPUT_DIR = "output";

	private void indexAllParagraphs() throws IOException {
		Directory indexdir = FSDirectory.open((new File(INDEX_DIRECTORY))
				.toPath());
		IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
		conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter iw = new IndexWriter(indexdir, conf);
		PostParser postParser = new PostParser();
		List<Post> postlist = postParser.readPosts("stackoverflow/Posts.xml");
		for(Post post: postlist)
		{
			// postTypeId of 1 signifies the post is a question
			if(post.postTypeId == 1) {
				this.indexPara(iw, post);
			}
		}
		iw.close();
	}
	

	private void indexPara(IndexWriter iw, Post postInfo) throws IOException {
		Document postdoc = new Document();
		FieldType indexType = new FieldType();
		indexType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		indexType.setStored(true);
		indexType.setStoreTermVectors(true);

		postdoc.add(new StringField("postid", Integer.toString(postInfo.postId), Field.Store.YES));
		postdoc.add(new Field("posttitle", postInfo.postTitle, indexType));
		postdoc.add(new Field("postbody", postInfo.postBody, indexType));
		postdoc.add(new StringField("postscore", Integer.toString(postInfo.score), Field.Store.YES));

		System.out.println(postInfo.postId + ": " + postInfo.postTitle);
		System.out.println("\tScore: " + postInfo.score);
		System.out.println();

		iw.addDocument(postdoc);
	}

	/*
	private void rankParas(Data.Page page, int n, String filename)
			throws IOException, ParseException {
		if (is == null) {
			is = new IndexSearcher(DirectoryReader.open(FSDirectory
					.open((new File(INDEX_DIRECTORY).toPath()))));
		}

		if (customScore) {
			SimilarityBase mySimiliarity = new SimilarityBase() {
				protected float score(BasicStats stats, float freq, float docLen) {
					return freq;
				}

				@Override
				public String toString() {
					return null;
				}
			};
			is.setSimilarity(mySimiliarity);
		}

		if (qp == null) {
			qp = new QueryParser("parabody", new StandardAnalyzer());
		}

		Query q;
		TopDocs tds;
		ScoreDoc[] retDocs;

	//	System.out.println("Query: " + page.getPageName());
		q = qp.parse(page.getPageName());

		tds = is.search(q, n);
		retDocs = tds.scoreDocs;
		Document d;
		ArrayList<String> runStringsForPage = new ArrayList<String>();
		String method = "lucene-score";
		if (customScore)
			method = "custom-score";
		for (int i = 0; i < retDocs.length; i++) {
			d = is.doc(retDocs[i].doc);
		

			// runFile string format $queryId Q0 $paragraphId $rank $score
			// $teamname-$methodname
			String runFileString = page.getPageId() + " Q0 "
					+ d.getField("paraid").stringValue() + " " + i + " "
					+ tds.scoreDocs[i].score + " team1-" + method;
			runStringsForPage.add(runFileString);
		}

		FileWriter fw = new FileWriter(QueryParagraphs.OUTPUT_DIR + "/"
				+ filename, true);
		for (String runString : runStringsForPage)
			fw.write(runString + "\n");
		fw.close();
	}
	*/

	/*
	private ArrayList<Data.Page> getPageListFromPath(String path) {
		ArrayList<Data.Page> pageList = new ArrayList<Data.Page>();
		try {
			FileInputStream fis = new FileInputStream(new File(path));
			for (Data.Page page : DeserializeData.iterableAnnotations(fis)) {
				pageList.add(page);
				//System.out.println(page.toString());

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeCborException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pageList;
	}

	// Function to read run file and store in hashmap inside HashMap
	public static HashMap<String, HashMap<String, String>> read_dataFile(
			String file_name) {
		HashMap<String, HashMap<String, String>> query = new HashMap<String, HashMap<String, String>>();

		File f = new File(file_name);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			ArrayList<String> al = new ArrayList<>();
			String text = null;
			while ((text = br.readLine()) != null) {
				String queryId = text.split(" ")[0];
				String paraID = text.split(" ")[2];
				String rank = text.split(" ")[3];

				if (al.contains(queryId))
					query.get(queryId).put(paraID, rank);
				else {
					HashMap<String, String> docs = new HashMap<String, String>();
					docs.put(paraID, rank);
					query.put(queryId, docs);
					al.add(queryId);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (br != null)
				br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return query;
	}

	*/

	public void writeRunfile(String filename, ArrayList<String> runfileStrings) {
		String fullpath = OUTPUT_DIR + "/" + filename;
		try (FileWriter runfile = new FileWriter(new File(fullpath))) {
			for (String line : runfileStrings) {
				runfile.write(line + "\n");
			}

			runfile.close();
		} catch (IOException e) {
			System.out.println("Could not open " + fullpath);
		}
	}

	
	

	public static void main(String[] args) {
		QueryParagraphs q = new QueryParagraphs();
		try {
			q.indexAllParagraphs();

			System.out.println("main: need to reimplement ranking functions to take \n\t\t parsed xml objects");
			/*
			TFIDF_bnn_bnn tfidf_bnn_bnn = new TFIDF_bnn_bnn(pagelist, 100);
			tfidf_bnn_bnn.doScoring();

			TFIDF_lnc_ltn tfidf_lnc_ltn = new TFIDF_lnc_ltn(pagelist, 100);
			tfidf_lnc_ltn.dumpScoresTo(OUTPUT_DIR + "/tfidf_lnc_ltn.run");

			System.out.println("Run LanguageMode_UL...");
			UnigramLanguageModel UL_ranking = new UnigramLanguageModel(pagelist, 100);
			q.writeRunfile("U-L.run", UL_ranking.getResults());

			// UJM
			System.out.println("Run LanguageMode_UJM...");
			LanguageModel_UJM UJM_ranking = new LanguageModel_UJM(pagelist, 100);
			q.writeRunfile("UJM.run", UJM_ranking.getResults());

			// UDS
			System.out.println("Run LanguageMode_UDS...");
			LanguageModel_UDS UDS_ranking = new LanguageModel_UDS(pagelist);
			*/

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
