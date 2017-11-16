package edu.unh.cs753853.team1;

import java.io.File;
import java.io.IOException;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs753853.team1.entities.Post;
import edu.unh.cs753853.team1.entities.Tag;
import edu.unh.cs753853.team1.entities.User;
import edu.unh.cs753853.team1.entities.Vote;
import edu.unh.cs753853.team1.parser.PostParser;
import edu.unh.cs753853.team1.parser.TagParser;
import edu.unh.cs753853.team1.utils.ProjectConfig;

public class StackOverflowDump {

	List<Post> post;
	HashMap<String, Tag> tag;
	HashMap<Integer, User> user;
	HashMap<Integer, Vote> vote;

	private static final String INDEX_DIRECTORY = ProjectConfig.INDEX_DIRECTORY;
	private static final String OUTPUT_DIR = ProjectConfig.OUTPUT_DIRECTORY;

	private StackOverflowDump instance;

	public StackOverflowDump getDataDump() {
		return null;
	};

	private StackOverflowDump indexDump(String dumpDir) throws IOException {
		StackOverflowDump dmp = new StackOverflowDump();
		Directory indexdir = FSDirectory.open((new File(INDEX_DIRECTORY)).toPath());
		IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
		conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter iw = new IndexWriter(indexdir, conf);

		// Create a Parser for our Post
		PostParser postParser = new PostParser();
		// Read post.xml file and parse it into a list of posts
		List<Post> postlist = postParser.readPosts(dumpDir + "Posts.xml");
		for (Post post : postlist) {
			// Indexes all the posts that are questions
			// postTypeId of 1 signifies the post is a question
			if (post.postTypeId == 1) {
				this.indexPost(iw, post);
			}
		}
		// add posts list to our dmp object
		dmp.post = postlist;

		// get our tags and add them to the dmp object
		TagParser tagParser = new TagParser();
		dmp.tag = tagParser.readTags(dumpDir + "Tags.xml");

		// Link posts to tags that they contain
		dmp.linkTags();

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

	private void linkTags() {
		if (tag == null || post == null) {
			System.out.println("Either this.tag or this.post is null, cannot link");
			return;
		}

		for (Post p : post) {
			if (p.tagList == null)
				continue;

			p.tagMap = new HashMap<>();
			for (String t : p.tagList) {
				p.tagMap.put(t, tag.get(t));
			}
		}
	}
}
