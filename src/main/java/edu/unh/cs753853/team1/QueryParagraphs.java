/*
 * author:Bindu Kumari
 */
package edu.unh.cs753853.team1;
import java.io.File;
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
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs753853.team1.entities.Dump;
import edu.unh.cs753853.team1.entities.Post;
import edu.unh.cs753853.team1.entities.Tag;
import edu.unh.cs753853.team1.entities.User;
import edu.unh.cs753853.team1.entities.Vote;
import edu.unh.cs753853.team1.parser.PostParser;
import edu.unh.cs753853.team1.parser.TagParser;
import edu.unh.cs753853.team1.ranking.DocumentResult;
import edu.unh.cs753853.team1.ranking.TFIDF_bnn_bnn;
import edu.unh.cs753853.team1.ranking.TFIDF_lnc_ltn;
import edu.unh.cs753853.team1.utils.ProjectConfig;
import edu.unh.cs753853.team1.utils.ProjectUtils;


public class QueryParagraphs {

	private IndexSearcher is = null;
	private QueryParser qp = null;

	// directory structure..
	static final String INDEX_DIRECTORY = ProjectConfig.INDEX_DIRECTORY;
	static final private String OUTPUT_DIR = ProjectConfig.OUTPUT_DIRECTORY;

	private Dump indexDump(String dumpDir) throws IOException {
		Dump dmp = new Dump();
		Directory indexdir = FSDirectory.open((new File(INDEX_DIRECTORY)).toPath());
		IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
		conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter iw = new IndexWriter(indexdir, conf);

		// Create a Parser for our Post
		PostParser postParser = new PostParser();
		// Read post.xml file and parse it into a list of posts
		List<Post> postlist = postParser.readPosts(dumpDir + "Posts.xml");
		HashMap<Integer, Post> postById = new HashMap<>();
		for (Post post : postlist) {
			// Indexes all the posts that are questions
			// postTypeId of 1 signifies the post is a question
			if (post.postTypeId == 1) {
				this.indexPost(iw, post);
				postById.put(post.postId, post);
			}
		}
		// add posts list to our dmp object
		dmp.addPosts(postById);

		// get our tags and add them to the dmp object
		TagParser tagParser = new TagParser();
		dmp.addTags(tagParser.readTags(dumpDir + "Tags.xml"));

		iw.close();

		return dmp;
	}

	private void indexPost(IndexWriter iw, Post postInfo) throws IOException {
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

		iw.addDocument(postdoc);
	}

	public static void main(String[] args) {
		QueryParagraphs q = new QueryParagraphs();
		try {
			Dump dmp = q.indexDump( "stackoverflow/");

			// Use our tags as test queries
			ArrayList<String> queries = dmp.getReadableTagNames();
			
			TFIDF_bnn_bnn tfidf_bnn_bnn = new TFIDF_bnn_bnn(queries, 50);
			tfidf_bnn_bnn.storeScoresTo(ProjectConfig.OUTPUT_DIRECTORY + "/bnn-bnn.run");

			// Generate relevance information based on tags
			// 	all posts that have a specific tag should be marked as
			//  relevant given a search query which is that tag
			ProjectUtils.writeQrelsFile(queries, dmp, "tags");

		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
}
