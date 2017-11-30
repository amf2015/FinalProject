package edu.unh.cs753853.team1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import edu.unh.cs753853.team1.ranking.LanguageModel_BL;
import edu.unh.cs753853.team1.ranking.TFIDF_anc_apc;
import edu.unh.cs753853.team1.ranking.TFIDF_bnn_bnn;
import edu.unh.cs753853.team1.ranking.TFIDF_lnc_ltn;
import edu.unh.cs753853.team1.utils.ProjectConfig;
import edu.unh.cs753853.team1.utils.ProjectUtils;

public class QueryParagraphs {

	// directory structure..
	static final String INDEX_DIRECTORY = ProjectConfig.INDEX_DIRECTORY;
	static final private String OUTPUT_DIR = ProjectConfig.OUTPUT_DIRECTORY;

	private Dump indexDump(String dumpDir) throws IOException {
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

		return dmp;
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

	public static String getResults(String query) {
	    String json = "";
		QueryParagraphs q = new QueryParagraphs();
		try {
			// Default .xml dump directory ("stackoverflow/")
			String dumpDirectory = ProjectConfig.STACK_DIRECTORY;

			ArrayList<String> writeStringList = new ArrayList<String>();
			HashMap<String, ArrayList<DocumentResult>> result_lucene = new HashMap<>();

			HashMap<String, ArrayList<DocumentResult>> result_anc_apc = new HashMap<>();

			HashMap<String, ArrayList<DocumentResult>> result_bnn_bnn = new HashMap<>();

			HashMap<String, ArrayList<DocumentResult>> result_lnc_ltn = new HashMap<>();

			HashMap<String, ArrayList<DocumentResult>> result_BL = new HashMap<>();

			// Parse the .xml files from cs.stackexchange.com into a Dump Object
			ProjectUtils.status(0, 5, "Index .xml files");
			Dump dmp = q.indexDump(dumpDirectory);

			// Use our tags as test queries
			ArrayList<String> queries = dmp.getReadableTagNames();

			// ProjectUtils.status(1, 5, "Lucene Default ranking");
			// LuceneDefault lucene = new LuceneDefault(queries, 30);
			// result_lucene = lucene.getResults();

			ProjectUtils.status(1, 5, "TFIDF(anc.apc) ranking");
			TFIDF_anc_apc tfidf_anc_apc = new TFIDF_anc_apc(queries, 30);
			result_anc_apc = tfidf_anc_apc.getResults();
			tfidf_anc_apc.write();

			// Limit returned posts to 30
			ProjectUtils.status(2, 5, "TFIDF(lnc.ltn) ranking");
			TFIDF_lnc_ltn tfidf_lnc_ltn = new TFIDF_lnc_ltn(queries, 30);
			result_lnc_ltn = tfidf_lnc_ltn.getResult();

			ArrayList<DocumentResult> results = tfidf_lnc_ltn.getResultsForQuery(queries.get(0));
			json = ProjectUtils.generateJSON(ProjectUtils.getPostsFromResults(results, dmp));

			ProjectUtils.status(3, 5, "TFIDF(bnn.bnn) ranking");
			TFIDF_bnn_bnn tfidf_bnn_bnn = new TFIDF_bnn_bnn(queries, 30);
			result_bnn_bnn = tfidf_bnn_bnn.getResults();

			ProjectUtils.status(4, 5, "Language Model(BL) ranking");
			LanguageModel_BL bigram = new LanguageModel_BL(queries, 30);
			result_BL = bigram.getReulst();

			// Generate relevance information based on tags
			// all posts that have a specific tag should be marked as
			// relevant given a search query which is that tag
			ProjectUtils.status(5, 5, "Generate .qrels file (pseudo relevance)");
			ProjectUtils.writeQrelsFile(queries, dmp, ProjectConfig.OUTPUT_MODIFIER + "tags");

			for (String page : queries) {
				System.out.println("Page=" + page);
				ArrayList<DocumentResult> bnn_list = result_bnn_bnn.get(page);
				ArrayList<DocumentResult> lnc_list = result_lnc_ltn.get(page);
				ArrayList<DocumentResult> bl_list = result_BL.get(page);
				ArrayList<DocumentResult> anc_list = result_anc_apc.get(page);

				HashMap<String, HashMap<String, String>> relevance_data = read_dataFile(
						ProjectConfig.OUTPUT_DIRECTORY + "/" + ProjectConfig.OUTPUT_MODIFIER + "tags.qrels");
				HashMap<String, String> relevantDocs = relevance_data.get(page);

				ArrayList<Integer> total_unique_docs = getAllUniqueDocumentId(bnn_list, lnc_list, bl_list, anc_list);

				System.out.println("Total :" + total_unique_docs.size() + " docs for Query: " + page);
				System.out.println(bnn_list.size() + " = " + lnc_list.size());

				/*
				 * bnn_bnn:1, lnc_ltn:2, BL:3, anc_apc:4
				 */
				for (Integer id : total_unique_docs) {
					DocumentResult r1 = getDocumentResultById(id, bnn_list);
					DocumentResult r2 = getDocumentResultById(id, lnc_list);
					DocumentResult r3 = getDocumentResultById(id, bl_list);
					DocumentResult r4 = getDocumentResultById(id, anc_list);

					float f1 = (float) ((r1 == null) ? 0.0 : (float) 1 / r1.getRank());
					float f2 = (float) ((r2 == null) ? 0.0 : (float) 1 / r2.getRank());
					float f3 = (float) ((r3 == null) ? 0.0 : (float) 1 / r3.getRank());
					float f4 = (float) ((r4 == null) ? 0.0 : (float) 1 / r4.getRank());

					int relevant = 0;
					if (relevantDocs != null) {
						if (relevantDocs.get(id) != null) {
							if (Integer.parseInt(relevantDocs.get(id)) > 0) {
								relevant = 1;
							}
						}
					} else {
						System.out.println("There is no relevant data for Query " + id);
					}

					String line = relevant + " qid:" + page + " 1:" + f1 + " 2:" + f2 + " 3:" + f3 + " 4:" + f4
							+ " # DocId:" + id;
					writeStringList.add(line);

				}
				ProjectUtils.writeToFile("test_result.txt", writeStringList);

			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return json;
	}

	public static void main(String[] args) {
		QueryParagraphs q = new QueryParagraphs();
		try {
			// Default .xml dump directory ("stackoverflow/")
			String dumpDirectory = ProjectConfig.STACK_DIRECTORY;

			// Argument allows user to specify .xml dump directory, defaults to
			// ProjectConfig.STACK_DIRECTORY ("stackoverflow/")
			if (args.length == 1) {
				// If we have an argument, add it to the end of the default
				// directory
				// e.g. "stackoverflow/" + arg[0]
				dumpDirectory += args[0];
				// Set a modifier so that we can label files and keep track of
				// which directory they
				// were indexed from.
				ProjectConfig.set_OUTPUT_MODIFIER(args[0].replace("/", "") + "-");
			}

			ArrayList<String> writeStringList = new ArrayList<String>();
			HashMap<String, ArrayList<DocumentResult>> result_lucene = new HashMap<>();

			HashMap<String, ArrayList<DocumentResult>> result_anc_apc = new HashMap<>();

			HashMap<String, ArrayList<DocumentResult>> result_bnn_bnn = new HashMap<>();

			HashMap<String, ArrayList<DocumentResult>> result_lnc_ltn = new HashMap<>();

			HashMap<String, ArrayList<DocumentResult>> result_BL = new HashMap<>();

			// Parse the .xml files from cs.stackexchange.com into a Dump Object
			ProjectUtils.status(0, 5, "Index .xml files");
			Dump dmp = q.indexDump(dumpDirectory);

			// Use our tags as test queries
			ArrayList<String> queries = dmp.getReadableTagNames();

			// ProjectUtils.status(1, 5, "Lucene Default ranking");
			// LuceneDefault lucene = new LuceneDefault(queries, 30);
			// result_lucene = lucene.getResults();

			ProjectUtils.status(1, 5, "TFIDF(anc.apc) ranking");
			TFIDF_anc_apc tfidf_anc_apc = new TFIDF_anc_apc(queries, 30);
			result_anc_apc = tfidf_anc_apc.getResults();
			tfidf_anc_apc.write();

			// Limit returned posts to 30
			ProjectUtils.status(2, 5, "TFIDF(lnc.ltn) ranking");
			TFIDF_lnc_ltn tfidf_lnc_ltn = new TFIDF_lnc_ltn(queries, 30);
			result_lnc_ltn = tfidf_lnc_ltn.getResult();

			ProjectUtils.status(3, 5, "TFIDF(bnn.bnn) ranking");
			TFIDF_bnn_bnn tfidf_bnn_bnn = new TFIDF_bnn_bnn(queries, 30);
			result_bnn_bnn = tfidf_bnn_bnn.getResults();

			ProjectUtils.status(4, 5, "Language Model(BL) ranking");
			LanguageModel_BL bigram = new LanguageModel_BL(queries, 30);
			result_BL = bigram.getReulst();

			// Generate relevance information based on tags
			// all posts that have a specific tag should be marked as
			// relevant given a search query which is that tag
			ProjectUtils.status(5, 5, "Generate .qrels file (pseudo relevance)");
			ProjectUtils.writeQrelsFile(queries, dmp, ProjectConfig.OUTPUT_MODIFIER + "tags");

			for (String page : queries) {
				System.out.println("Page=" + page);
				ArrayList<DocumentResult> bnn_list = result_bnn_bnn.get(page);
				ArrayList<DocumentResult> lnc_list = result_lnc_ltn.get(page);
				ArrayList<DocumentResult> bl_list = result_BL.get(page);
				ArrayList<DocumentResult> anc_list = result_anc_apc.get(page);

				HashMap<String, HashMap<String, String>> relevance_data = read_dataFile(
						ProjectConfig.OUTPUT_DIRECTORY + "/" + ProjectConfig.OUTPUT_MODIFIER + "tags.qrels");
				HashMap<String, String> relevantDocs = relevance_data.get(page);

				ArrayList<Integer> total_unique_docs = getAllUniqueDocumentId(bnn_list, lnc_list, bl_list, anc_list);

				System.out.println("Total :" + total_unique_docs.size() + " docs for Query: " + page);
				System.out.println(bnn_list.size() + " = " + lnc_list.size());

				/*
				 * bnn_bnn:1, lnc_ltn:2, BL:3, anc_apc:4
				 */
				for (Integer id : total_unique_docs) {
					DocumentResult r1 = getDocumentResultById(id, bnn_list);
					DocumentResult r2 = getDocumentResultById(id, lnc_list);
					DocumentResult r3 = getDocumentResultById(id, bl_list);
					DocumentResult r4 = getDocumentResultById(id, anc_list);

					float f1 = (float) ((r1 == null) ? 0.0 : (float) 1 / r1.getRank());
					float f2 = (float) ((r2 == null) ? 0.0 : (float) 1 / r2.getRank());
					float f3 = (float) ((r3 == null) ? 0.0 : (float) 1 / r3.getRank());
					float f4 = (float) ((r4 == null) ? 0.0 : (float) 1 / r4.getRank());

					int relevant = 0;
					if (relevantDocs != null) {
						if (relevantDocs.get(id) != null) {
							if (Integer.parseInt(relevantDocs.get(id)) > 0) {
								relevant = 1;
							}
						}
					} else {
						System.out.println("There is no relevant data for Query " + id);
					}

					String line = relevant + " qid:" + page + " 1:" + f1 + " 2:" + f2 + " 3:" + f3 + " 4:" + f4
							+ " # DocId:" + id;
					writeStringList.add(line);

				}
				ProjectUtils.writeToFile("test_result.txt", writeStringList);

			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<Integer> getAllUniqueDocumentId(ArrayList<DocumentResult> bnn_list,
			ArrayList<DocumentResult> lnc_list, ArrayList<DocumentResult> bl_list,
			ArrayList<DocumentResult> lucene_list) {

		ArrayList<Integer> total_documents = new ArrayList<Integer>();
		ArrayList<DocumentResult> total_DocumentResult = new ArrayList<DocumentResult>();

		total_DocumentResult.addAll(bnn_list);
		total_DocumentResult.addAll(lnc_list);
		total_DocumentResult.addAll(bl_list);
		total_DocumentResult.addAll(lucene_list);

		for (DocumentResult rank : total_DocumentResult) {
			// total_documents.add(rank.getParaId());
			total_documents.add(rank.getId());
		}

		Set<Integer> hs = new HashSet<>();

		hs.addAll(total_documents);
		total_documents.clear();
		total_documents.addAll(hs);

		return total_documents;
	}

	// Function to read run file and store in hashmap inside HashMap
	public static HashMap<String, HashMap<String, String>> read_dataFile(String file_name) {
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

	private static DocumentResult getDocumentResultById(Integer id, ArrayList<DocumentResult> list) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		for (DocumentResult rank : list) {
			{
				if (rank.getId() == id) {
					return rank;
				}
			}
		}
		return null;
	}

	// private static ArrayList<String>
	// getContentResult(ArrayList<DocumentResult> list) {
	// ArrayList<String> result = new ArrayList<>();
	// if (list != null && list.size() > 0) {
	// for (DocumentResult rank : list) {
	// String line = rank.getRank() + " : " + rank.getParaContent();
	// result.add(line);
	// }
	// }
	//
	// return result;
	// }
}
