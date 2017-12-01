package edu.unh.cs753853.team1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs753853.team1.entities.Dump;
import edu.unh.cs753853.team1.entities.Post;
import edu.unh.cs753853.team1.parser.PostParser;
import edu.unh.cs753853.team1.parser.TagParser;
import edu.unh.cs753853.team1.ranking.DocumentResult;
import edu.unh.cs753853.team1.ranking.TFIDF_lnc_ltn;
import edu.unh.cs753853.team1.utils.ProjectConfig;
import edu.unh.cs753853.team1.utils.ProjectUtils;

public class QueryManager {
	// directory structure..
	private static String INDEX_DIRECTORY;
	private static String OUTPUT_DIR;
	private static QueryManager instance;
	private static Dump dmp;

	private QueryManager() {
		String dumpDirectory = ProjectConfig.STACK_DIRECTORY;
		INDEX_DIRECTORY = ProjectConfig.INDEX_DIRECTORY;
		OUTPUT_DIR = ProjectConfig.OUTPUT_DIRECTORY;
		try {
			indexDump(dumpDirectory);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public static QueryManager getInstance() {
		if (instance == null) {
			instance = new QueryManager();
		}
		return instance;
	}

	private void indexDump(String dumpDir) throws IOException {
		Dump dmp = new Dump();
		Directory indexdir = FSDirectory.open((new File(INDEX_DIRECTORY)).toPath());
		IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
		conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(indexdir, conf);

		// Create a Parser for our Post
		PostParser postParser = new PostParser();
		// Read post.xml file and parse it into a list of posts
		List<Post> postlist = postParser.readPosts(dumpDir + "/Posts.xml");
		HashMap<Integer, Post> postById = new HashMap<>();
		for (Post post : postlist) {
			// Indexes all the posts that are questions
			// postTypeId of 1 signifies the post is a question

			if (post.postTypeId == 1) {
				this.indexPost(writer, post);
				postById.put(post.postId, post);
			}
		}
		// add posts list to our dmp object
		dmp.addPosts(postById);

		// get our tags and add them to the dmp object
		TagParser tagParser = new TagParser();

		dmp.addTags(tagParser.readTags(dumpDir + "/Tags.xml"));

		writer.close();
		this.dmp = dmp;
		// return dmp;
	}

	private void indexPost(IndexWriter writer, Post postInfo) throws IOException {
		Document postdoc = new Document();
		FieldType indexType = new FieldType();
		indexType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		indexType.setStored(true);
		indexType.setStoreTermVectors(true);

		// count max tf for each doc
		HashMap<String, Integer> tf = new HashMap<>();
		int maxTF = 0;
		for (String cur : postInfo.postBody.split(" ")) {
			if (tf.containsKey(cur)) {
				tf.put(cur, tf.get(cur) + 1);
			} else {
				tf.put(cur, 1);
			}
			if (tf.get(cur) > maxTF) {
				maxTF = tf.get(cur);
			}
		}

		// Save post: Id, Score, AnswerCount, Title, Body
		postdoc.add(new StringField("postid", Integer.toString(postInfo.postId), Field.Store.YES));
		postdoc.add(new StringField("postscore", Integer.toString(postInfo.score), Field.Store.YES));
		postdoc.add(new StringField("postanswers", Integer.toString(postInfo.answerCount), Field.Store.YES));
		postdoc.add(new Field("posttitle", postInfo.postTitle, indexType));
		postdoc.add(new Field("postbody", postInfo.postBody, indexType));
		postdoc.add(new StringField("maxtf", Integer.toString(maxTF), Field.Store.YES));

		writer.addDocument(postdoc);
	}

	public void writeRunfile(String filename, ArrayList<String> runfileStrings) {
		String fullpath = filename;

		PrintWriter writer;
		try {
			writer = new PrintWriter(fullpath, "UTF-8");
			for (String runString : runfileStrings) {
				writer.write(runString + "\n");
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getResults(String query) {
		String json = "";
		Integer max_docs = 40;

		if (!query.isEmpty()) {

			try {

				ArrayList<String> queryList = new ArrayList<>();

				queryList.add(query);

				ArrayList<String> writeStringList = new ArrayList<String>();
				HashMap<String, ArrayList<DocumentResult>> result_lucene = new HashMap<>();

				HashMap<String, ArrayList<DocumentResult>> result_anc_apc = new HashMap<>();

				HashMap<String, ArrayList<DocumentResult>> result_bnn_bnn = new HashMap<>();

				HashMap<String, ArrayList<DocumentResult>> result_lnc_ltn = new HashMap<>();

				HashMap<String, ArrayList<DocumentResult>> result_BL = new HashMap<>();

				// Parse the .xml files from cs.stackexchange.com into a Dump
				// Object

				// Use our tags as test queries
				ArrayList<String> queries = dmp.getReadableTagNames();

				// ProjectUtils.status(1, 5, "TFIDF(anc.apc) ranking");
				// TFIDF_anc_apc tfidf_anc_apc = new TFIDF_anc_apc(queryList,
				// 30);
				// result_anc_apc = tfidf_anc_apc.getResults();
				// tfidf_anc_apc.write();

				// Limit returned posts to 30
				ProjectUtils.status(1, 1, "TFIDF(lnc.ltn) ranking");
				TFIDF_lnc_ltn tfidf_lnc_ltn = new TFIDF_lnc_ltn(queryList, max_docs);
				result_lnc_ltn = tfidf_lnc_ltn.getResult();

				ArrayList<DocumentResult> results = tfidf_lnc_ltn.getResultsForQuery(queryList.get(0));
				json = ProjectUtils.generateJSON(ProjectUtils.getPostsFromResults(results, dmp));

				// ProjectUtils.status(3, 5, "TFIDF(bnn.bnn) ranking");
				// TFIDF_bnn_bnn tfidf_bnn_bnn = new TFIDF_bnn_bnn(queryList,
				// 30);
				// result_bnn_bnn = tfidf_bnn_bnn.getResults();
				//
				// ProjectUtils.status(4, 5, "Language Model(BL) ranking");
				// LanguageModel_BL bigram = new LanguageModel_BL(queryList,
				// 30);
				// result_BL = bigram.getReulst();

			} catch (IOException |

			ParseException e)

			{
				e.printStackTrace();
			}
			return json;
		} else {
			System.out.println("Empty results.");
			return "";
		}

	}

	// public static ArrayList<Integer>
	// getAllUniqueDocumentId(ArrayList<DocumentResult> bnn_list,
	// ArrayList<DocumentResult> lnc_list, ArrayList<DocumentResult> bl_list,
	// ArrayList<DocumentResult> lucene_list) {
	//
	// ArrayList<Integer> total_documents = new ArrayList<Integer>();
	// ArrayList<DocumentResult> total_DocumentResult = new
	// ArrayList<DocumentResult>();
	//
	// total_DocumentResult.addAll(bnn_list);
	// total_DocumentResult.addAll(lnc_list);
	// total_DocumentResult.addAll(bl_list);
	// total_DocumentResult.addAll(lucene_list);
	//
	// for (DocumentResult rank : total_DocumentResult) {
	// // total_documents.add(rank.getParaId());
	// total_documents.add(rank.getId());
	// }
	//
	// Set<Integer> hs = new HashSet<>();
	//
	// hs.addAll(total_documents);
	// total_documents.clear();
	// total_documents.addAll(hs);
	//
	// return total_documents;
	// }

	// // Function to read run file and store in hashmap inside HashMap
	// public static HashMap<String, HashMap<String, String>>
	// read_dataFile(String file_name) {
	// HashMap<String, HashMap<String, String>> query = new HashMap<String,
	// HashMap<String, String>>();
	//
	// File f = new File(file_name);
	// BufferedReader br = null;
	// try {
	// br = new BufferedReader(new FileReader(f));
	// ArrayList<String> al = new ArrayList<>();
	// String text = null;
	// while ((text = br.readLine()) != null) {
	// String queryId = text.split(" ")[0];
	// String paraID = text.split(" ")[2];
	// String rank = text.split(" ")[3];
	//
	// if (al.contains(queryId))
	// query.get(queryId).put(paraID, rank);
	// else {
	// HashMap<String, String> docs = new HashMap<String, String>();
	// docs.put(paraID, rank);
	// query.put(queryId, docs);
	// al.add(queryId);
	// }
	// }
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// try {
	// if (br != null)
	// br.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// return query;
	// }

	// private static DocumentResult getDocumentResultById(Integer id,
	// ArrayList<DocumentResult> list) {
	// if (list == null || list.isEmpty()) {
	// return null;
	// }
	// for (DocumentResult rank : list) {
	// {
	// if (rank.getId() == id) {
	// return rank;
	// }
	// }
	// }
	// return null;
	// }

}
