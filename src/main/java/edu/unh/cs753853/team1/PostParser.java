package edu.unh.cs753853.team1;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;



class Post {
    int favoriteCount;
    int commentCount;
    int answerCount;
    String tagList[];
    String tags;
    HashMap<String, Tag> tagMap;
    String postTitle;
    String lastActivityDate;
    String lastEditDate;
    int lastEditorUserId;
    int ownerUserId;
    String postBody;
    int viewCount;
    int score;
    String creationDate;
    int acceptedAnswerId;
    int postTypeId;
    int postId;
}

class PostHandler extends DefaultHandler {
    List<Post> posts;
    Post post;
    @Override
    public void startElement(
            String uri, String localName, String qName, Attributes attributes)
            throws SAXException {

        if (qName.equals("row")) {
            post = new Post();
            // Check for known attribute names and place value in appropriate
            //  slot in Post object
            post.postId = getInt(attributes, "Id");
            post.postTypeId = getInt(attributes, "PostTypeId");
            post.acceptedAnswerId = getInt(attributes, "AcceptedAnswerId");
            post.creationDate = getString(attributes, "CreationDate");
            post.viewCount = getInt(attributes, "ViewCount");
            String html = getString(attributes, "Body");
            post.postBody = html.replaceAll("\\<.*?>", "");
            post.ownerUserId = getInt(attributes, "OwnerUserId");
            post.lastEditorUserId = getInt(attributes, "LastEditorUserId");
            post.lastEditDate = getString(attributes, "lastEditDate");
            post.lastActivityDate = getString(attributes, "LasActivityDate");
            post.postTitle = getString(attributes, "Title");
            String rawTags = getString(attributes, "Tags");
            post.tags = rawTags.replace("<", "").replace(">", " ");
            post.tagList = rawTags.split("><");
            for (int i = 0; i < post.tagList.length; i++) {
                post.tagList[i] = post.tagList[i].replace("<", "").replace(">", "");
            }
            post.answerCount = getInt(attributes, "AnswerCount");
            post.commentCount = getInt(attributes, "CommentCount");
            post.favoriteCount = getInt(attributes, "FavoriteCount");
            post.score = getInt(attributes, "Score");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("row")) {
            if(posts == null)
                posts = new ArrayList<>();
            posts.add(post);
            if(posts.size() % 10000 == 0)
                System.out.print(".");
            if(posts.size() % 100000 == 0)
                System.out.println("(" + posts.size() + " parsed so far)");
        }
    }

    public List<Post> getPosts()
    {
        return posts;
    }

    private int getInt(Attributes attr, String value)
    {
        if(attr.getValue(value) == null)
            return 0;
        return Integer.parseInt(attr.getValue(value));
    }

    private String getString(Attributes attr, String value)
    {
        if(attr.getValue(value) == null)
            return "";
        return attr.getValue(value);
    }

    private String valueOrDefault(String value, String def) {
        if(value == null)
            return def;
        return value;
    }
}

public class PostParser {

    /**
     * Reads all <row ... /> in <posts>...</posts> and parses them into a Post object
     * @param postsFile the posts.xml file from the stack overflow dump
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

            System.out.println("Starting parse of Posts.xml ...");
            postParser.parse(inputFile, handler);
            posts = handler.getPosts();
            System.out.println("Parsing done.\n Parsed " + posts.size() + " posts");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }
}
