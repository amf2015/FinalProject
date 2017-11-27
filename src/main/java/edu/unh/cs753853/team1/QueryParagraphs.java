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
import edu.unh.cs753853.team1.ranking.TFIDF_lnc_ltn;
import edu.unh.cs753853.team1.utils.ProjectConfig;
import edu.unh.cs753853.team1.utils.ProjectUtils;


public class QueryParagraphs {


	private IndexSearcher is = null;
	private QueryParser qp = null;

	// directory structure..
	static final String INDEX_DIRECTORY = ProjectConfig.INDEX_DIRECTORY;
	static final private String OUTPUT_DIR = ProjectConfig.OUTPUT_DIRECTORY;
	
	private ArrayList<String> queries;

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
	
	
	void addQuery(String s) {
		if(queries == null) {
			queries = new ArrayList<String>();
		}
		queries.add(s);
	}
	
	/*
	 * dump
	 * max results per query
	 * write results to filename
	 */
	private void rankPosts(Dump dump, int max, String filename)
			throws IOException {
		
		if (is == null) {
			is = new IndexSearcher(DirectoryReader.open(FSDirectory
					.open((new File(INDEX_DIRECTORY).toPath()))));
		}
		if (qp == null) {
			qp = new QueryParser("postbody", new StandardAnalyzer());
		}

		
		Query q;
		TopDocs tds;
		ScoreDoc[] retDocs;
		ArrayList<String> runStrings = new ArrayList<String>();
		
		while(queries.size() > 0) {
			String tmpQ = queries.remove(queries.size()-1);
			try {
				q = qp.parse(tmpQ);
				tds = is.search(q, max);
				retDocs = tds.scoreDocs;
				
				Document d;
				
				for (int i = 0; i < retDocs.length; i++) {
					d = is.doc(retDocs[i].doc);
					String runFileString = tmpQ + " Q0 "
							+ d.getField("posttitle").stringValue() + " " + i + " "
							+ tds.scoreDocs[i].score + " team1-" + "method";
					runStrings.add(runFileString);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		writeRunfile(filename, runStrings);

	}


	public void writeRunfile(String filename, ArrayList<String> runfileStrings) {
		String fullpath = OUTPUT_DIR + "/" + filename;
		
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

	
	

	public static void main(String[] args) {
		QueryParagraphs q = new QueryParagraphs();
		try {
			// Parse the .xml files from cs.stackexchange.com into a Dump Object
			Dump cs_stackexchange = q.indexDump(ProjectConfig.CS_STACK_DIRECTORY);

			// Use our tags as test queries
			ArrayList<String> cs_queries = cs_stackexchange.getReadableTagNames();
			
//			try {
				//q.rankPosts(dmp, 20, "rankOutput");
//			} 

			// Limit returned posts to 30
			TFIDF_lnc_ltn tfidf_lnc_ltn = new TFIDF_lnc_ltn(cs_queries, 30);
			tfidf_lnc_ltn.dumpScoresTo(ProjectConfig.OUTPUT_DIRECTORY + "/cs-lnc-ltn.run");

			// Generate relevance information based on tags
			// 	all posts that have a specific tag should be marked as
			//  relevant given a search query which is that tag
			ProjectUtils.writeQrelsFile(cs_queries, cs_stackexchange, "tags");

		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
}
