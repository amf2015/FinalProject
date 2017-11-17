package edu.unh.cs753853.team1.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.unh.cs753853.team1.entities.Post;

class PostHandler extends DefaultHandler {
	List<Post> posts;
	Post post;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (qName.equals("row")) {

			post = new Post();
			post.postTypeId = getInt(attributes, "PostTypeId");
			if (post.postTypeId != 1) {
				post = null;
				return;
			}

			// Check for known attribute names and place value in appropriate
			// slot in Post object
			post.postId = getInt(attributes, "Id");
			post.acceptedAnswerId = getInt(attributes, "AcceptedAnswerId");
			post.creationDate = getString(attributes, "CreationDate");
			post.viewCount = getInt(attributes, "ViewCount");
			String html = getString(attributes, "Body");
			post.postBody = html.replaceAll("\\<.*?>", "");
			post.ownerUserId = getInt(attributes, "OwnerUserId");
			post.postTitle = getString(attributes, "Title");
			String rawTags = getString(attributes, "Tags");
			post.tags = rawTags.replace("<", "").replace(">", " ");
			post.tagList = rawTags.split("><");
			for (int i = 0; i < post.tagList.length; i++) {
				post.tagList[i] = post.tagList[i].replace("<", "").replace(">", "");
			}
			post.answerCount = getInt(attributes, "AnswerCount");
			post.favoriteCount = getInt(attributes, "FavoriteCount");
			post.score = getInt(attributes, "Score");
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("row")) {
			if (posts == null)
				posts = new ArrayList<>();
			if (post == null)
				return;

			posts.add(post);
			if (posts.size() % 10000 == 0)
				System.out.print(".");
			if (posts.size() % 100000 == 0)
				System.out.println(" # " + posts.size() + "");
		}
	}

	public List<Post> getPosts() {
		return posts;
	}

	private int getInt(Attributes attr, String value) {
		if (attr.getValue(value) == null)
			return 0;
		return Integer.parseInt(attr.getValue(value));
	}

	private String getString(Attributes attr, String value) {
		if (attr.getValue(value) == null)
			return "";
		return attr.getValue(value);
	}
}

public class PostParser {

	/**
	 * Reads all <row ... /> in <posts>...</posts> and parses them into a Post
	 * object
	 * 
	 * @param postsFile
	 *            the posts.xml file from the stack overflow dump
	 * @return List<Post>
	 */
	public List<Post> readPosts(String postsFile) {
		// Create a list to store the posts we read from the xml file
		List<Post> posts = new ArrayList<>();

		// Catch: FileNotFoundException, XMLStreamException
		try {
			File inputFile = new File(postsFile);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
			SAXParser postParser = factory.newSAXParser();
			PostHandler handler = new PostHandler();

			System.out.println("Starting parse of " + postsFile + "...");
			postParser.parse(inputFile, handler);
			posts = handler.getPosts();
			System.out.println("Parsing done.\n Parsed " + posts.size() + " posts");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return posts;
	}
}
