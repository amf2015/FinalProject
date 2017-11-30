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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs753853.team1.entities.Dump;
import edu.unh.cs753853.team1.entities.Post;
import edu.unh.cs753853.team1.parser.PostParser;
import edu.unh.cs753853.team1.parser.TagParser;
<<<<<<< HEAD
=======
import edu.unh.cs753853.team1.ranking.DocumentResult;
import edu.unh.cs753853.team1.ranking.LanguageModel_BL;
import edu.unh.cs753853.team1.ranking.TFIDF_bnn_bnn;
>>>>>>> 7a047cc0192af26dc762b31750c3d8417a98e0d8
import edu.unh.cs753853.team1.ranking.TFIDF_lnc_ltn;
import edu.unh.cs753853.team1.utils.ProjectConfig;
import edu.unh.cs753853.team1.utils.ProjectUtils;

public class QueryParagraphs {

	private IndexSearcher is = null;
	private QueryParser qp = null;

	// directory structure..
	static final String INDEX_DIRECTORY = ProjectConfig.INDEX_DIRECTORY;
	static final private String OUTPUT_DIR = ProjectConfig.OUTPUT_DIRECTORY;
<<<<<<< HEAD

	private ArrayList<String> queries;
=======
>>>>>>> 7a047cc0192af26dc762b31750c3d8417a98e0d8

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

		// Save post: Id, Score, AnswerCount, Title, Body
		postdoc.add(new StringField("postid", Integer.toString(postInfo.postId), Field.Store.YES));
		postdoc.add(new StringField("postscore", Integer.toString(postInfo.score), Field.Store.YES));
		postdoc.add(new StringField("postanswers", Integer.toString(postInfo.answerCount), Field.Store.YES));
		postdoc.add(new Field("posttitle", postInfo.postTitle, indexType));
		postdoc.add(new Field("postbody", postInfo.postBody, indexType));

		writer.addDocument(postdoc);
	}
<<<<<<< HEAD

	void addQuery(String s) {
		if (queries == null) {
			queries = new ArrayList<String>();
		}
		queries.add(s);
	}
=======
	

>>>>>>> 7a047cc0192af26dc762b31750c3d8417a98e0d8

	/*
	 * dump max results per query write results to filename
	 */
<<<<<<< HEAD
	private void rankPosts(Dump dump, int max, String filename) throws IOException {

=======
	private void rankPosts(ArrayList<String> queries, int max, String filename)
			throws IOException {
		
>>>>>>> 7a047cc0192af26dc762b31750c3d8417a98e0d8
		if (is == null) {
			is = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(INDEX_DIRECTORY).toPath()))));
		}
		if (qp == null) {
			qp = new QueryParser("postbody", new StandardAnalyzer());
		}

		Query q;
		TopDocs tds;
		ScoreDoc[] retDocs;
<<<<<<< HEAD
		ArrayList<String> runStrings = new ArrayList<String>();

		while (queries.size() > 0) {
			String tmpQ = queries.remove(queries.size() - 1);
=======
		ArrayList<String> runStrings = new ArrayList<>();
		
		for(String tmpQ: queries) {
>>>>>>> 7a047cc0192af26dc762b31750c3d8417a98e0d8
			try {
				q = qp.parse(tmpQ);
				tds = is.search(q, max);
				retDocs = tds.scoreDocs;

				Document d;

				for (int i = 0; i < retDocs.length; i++) {
					d = is.doc(retDocs[i].doc);
<<<<<<< HEAD
					String runFileString = tmpQ + " Q0 " + d.getField("posttitle").stringValue() + " " + i + " "
							+ tds.scoreDocs[i].score + " team1-" + "method";
=======
					String runFileString = tmpQ.replace(" ", "-") + " Q0 "
							+ d.getField("postid").stringValue() + " " + i + " "
							+ tds.scoreDocs[i].score + " team1-" + "lucene-default";
>>>>>>> 7a047cc0192af26dc762b31750c3d8417a98e0d8
					runStrings.add(runFileString);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		writeRunfile(filename, runStrings);

	}

<<<<<<< HEAD
	/*
	 * private ArrayList<Data.Page> getPageListFromPath(String path) {
	 * ArrayList<Data.Page> pageList = new ArrayList<Data.Page>(); try {
	 * FileInputStream fis = new FileInputStream(new File(path)); for (Data.Page
	 * page : DeserializeData.iterableAnnotations(fis)) { pageList.add(page);
	 * //System.out.println(page.toString());
	 * 
	 * } } catch (FileNotFoundException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (RuntimeCborException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } return pageList; }
	 * 
	 * // Function to read run file and store in hashmap inside HashMap public
	 * static HashMap<String, HashMap<String, String>> read_dataFile( String
	 * file_name) { HashMap<String, HashMap<String, String>> query = new
	 * HashMap<String, HashMap<String, String>>();
	 * 
	 * File f = new File(file_name); BufferedReader br = null; try { br = new
	 * BufferedReader(new FileReader(f)); ArrayList<String> al = new
	 * ArrayList<>(); String text = null; while ((text = br.readLine()) != null)
	 * { String queryId = text.split(" ")[0]; String paraID = text.split(" "
	 * )[2]; String rank = text.split(" ")[3];
	 * 
	 * if (al.contains(queryId)) query.get(queryId).put(paraID, rank); else {
	 * HashMap<String, String> docs = new HashMap<String, String>();
	 * docs.put(paraID, rank); query.put(queryId, docs); al.add(queryId); } } }
	 * catch (FileNotFoundException e) { e.printStackTrace(); } catch
	 * (IOException e) { e.printStackTrace(); }
	 * 
	 * try { if (br != null) br.close(); } catch (IOException e) {
	 * e.printStackTrace(); }
	 * 
	 * return query; }
	 * 
	 */

	public void writeRunfile(String filename, ArrayList<String> runfileStrings) {
		String fullpath = OUTPUT_DIR + "/" + filename;

=======

	public void writeRunfile(String filename, ArrayList<String> runfileStrings) {
		String fullpath = filename;
		
>>>>>>> 7a047cc0192af26dc762b31750c3d8417a98e0d8
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

<<<<<<< HEAD
=======


>>>>>>> 7a047cc0192af26dc762b31750c3d8417a98e0d8
	public static void main(String[] args) {
		QueryParagraphs q = new QueryParagraphs();
		try {
			// Default .xml dump directory ("stackoverflow/")
		    String dumpDirectory = ProjectConfig.STACK_DIRECTORY;

		    // Argument allows user to specify .xml dump directory, defaults to ProjectConfig.STACK_DIRECTORY ("stackoverflow/")
			if(args.length == 1)
			{
			    // If we have an argument, add it to the end of the default directory
				// 	e.g. "stackoverflow/" + arg[0]
				dumpDirectory += args[0];
				// Set a modifier so that we can label files and keep track of which directory they
				// were indexed from.
				ProjectConfig.set_OUTPUT_MODIFIER(args[0].replace("/","") + "-");
			}

			// Parse the .xml files from cs.stackexchange.com into a Dump Object
            ProjectUtils.status(0, 5, "Index .xml files");
			Dump dmp = q.indexDump(dumpDirectory);

			// Use our tags as test queries
<<<<<<< HEAD
			ArrayList<String> cs_queries = cs_stackexchange.getReadableTagNames();

			// try {
			// q.rankPosts(dmp, 20, "rankOutput");
			// }
=======
			ArrayList<String> queries = dmp.getReadableTagNames();

			ProjectUtils.status(1, 5, "Lucene Default ranking");
            q.rankPosts(queries, 30, ProjectConfig.OUTPUT_DIRECTORY + "/" + ProjectConfig.OUTPUT_MODIFIER + "lucene.run");
>>>>>>> 7a047cc0192af26dc762b31750c3d8417a98e0d8

			// Limit returned posts to 30
			ProjectUtils.status(2, 5, "TFIDF(lnc.ltn) ranking");
			TFIDF_lnc_ltn tfidf_lnc_ltn = new TFIDF_lnc_ltn(queries, 30);
			tfidf_lnc_ltn.dumpScoresTo(ProjectConfig.OUTPUT_DIRECTORY + "/" + ProjectConfig.OUTPUT_MODIFIER + "lnc-ltn.run");

			ProjectUtils.status(3, 5, "TFIDF(bnn.bnn) ranking");
			TFIDF_bnn_bnn tfidf_bnn_bnn = new TFIDF_bnn_bnn(queries, 30);
			tfidf_bnn_bnn.storeScoresTo(ProjectConfig.OUTPUT_DIRECTORY + "/" + ProjectConfig.OUTPUT_MODIFIER + "bnn-bnn.run");

			ProjectUtils.status(4, 5, "Language Model(BL) ranking");
			LanguageModel_BL bigram = new LanguageModel_BL(queries, 30);
			bigram.generateResults(ProjectConfig.OUTPUT_DIRECTORY + "/" + ProjectConfig.OUTPUT_MODIFIER + "LM-BL.run");

			// Generate relevance information based on tags
<<<<<<< HEAD
			// all posts that have a specific tag should be marked as
			// relevant given a search query which is that tag
			ProjectUtils.writeQrelsFile(cs_queries, cs_stackexchange, "tags");
=======
			// 	all posts that have a specific tag should be marked as
			//  relevant given a search query which is that tag
			ProjectUtils.status(5, 5, "Generate .qrels file (pseudo relevance)");
			ProjectUtils.writeQrelsFile(queries, dmp, "tags");
>>>>>>> 7a047cc0192af26dc762b31750c3d8417a98e0d8

			System.out.println();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
}
