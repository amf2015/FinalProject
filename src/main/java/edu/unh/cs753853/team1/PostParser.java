package edu.unh.cs753853.team1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

class Post {
    public int favoriteCount;
    public int commentCount;
    public int answerCount;
    public String tagList[];
    public String tags;
    public String postTitle;
    public String lastActivityDate;
    public String lastEditDate;
    public int lastEditorUserId;
    public int ownerUserId;
    public String postBody;
    public int viewCount;
    public int score;
    public String creationDate;
    public int acceptedAnswerId;
    public int postTypeId;
    public int postId;
}

public class PostParser {

    /**
     * Reads all <row ... /> in <posts>...</posts> and parses them into a Post object
     * @param postsFile the posts.xml file from the stack overflow dump
     * @return List<Post>
     */
    @SuppressWarnings("unchecked")
    public List<Post> readPosts(String postsFile) {
        // Create a list to store the posts we read from the xml file
        List<Post> posts = new ArrayList<>();

        // Catch: FileNotFoundException, XMLStreamException
        try {
            // Create an xml input instance
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Open an input stream for our posts file
            InputStream in = new FileInputStream(postsFile);
            // Get an event reader for our posts.xml input stream
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

            // Temporarily holds our Post object before it is inserted into the list
            Post post = null;

            // While we still have XML events to read
            while (eventReader.hasNext()) {
                // Get the next event
                XMLEvent event = eventReader.nextEvent();

                // If our event signals the start of a tag
                if (event.isStartElement())
                {
                    // Keep track of the tag
                    StartElement startElement = event.asStartElement();

                    // If the tags name is "row"
                    if(startElement.getName().getLocalPart().equals("row")) {
                        // Create a new blank Post object
                        post = new Post();

                        // Get an iterator over all attributes in the tag
                        Iterator<Attribute> attributes = startElement.getAttributes();

                        // While there are still attributes to process
                        while(attributes.hasNext()) {
                            // Get the next attribute and its name
                            Attribute attribute = attributes.next();
                            String attrName = attribute.getName().toString();

                            // Check for known attribute names and place value in appropriate
                            //  slot in Post object
                            switch(attrName) {
                                case "Id":
                                    post.postId = Integer.parseInt(attribute.getValue());
                                    break;
                                case "PostTypeId":
                                    post.postTypeId = Integer.parseInt(attribute.getValue());
                                    break;
                                case "AcceptedAnswerId":
                                    post.acceptedAnswerId = Integer.parseInt(attribute.getValue());
                                    break;
                                case "CreationDate":
                                    post.creationDate = attribute.getValue();
                                    break;
                                case "ViewCount":
                                    post.viewCount = Integer.parseInt(attribute.getValue());
                                    break;
                                case "Body":
                                    String html = attribute.getValue();
                                    post.postBody = html.replaceAll("\\<.*?>","");
                                    break;
                                case "OwnerUserId":
                                    post.ownerUserId= Integer.parseInt(attribute.getValue());
                                    break;
                                case "LastEditorUserId":
                                    post.lastEditorUserId = Integer.parseInt(attribute.getValue());
                                    break;
                                case "LastEditDate":
                                    post.lastEditDate = attribute.getValue();
                                    break;
                                case "LastActivityDate":
                                    post.lastActivityDate = attribute.getValue();
                                    break;
                                case "Title":
                                    post.postTitle = attribute.getValue();
                                    break;
                                case "Tags":
                                    String rawTags = attribute.getValue();
                                    post.tags = rawTags.replace("<", "").replace(">", " ");
                                    post.tagList = rawTags.split("><");
                                    for(int i = 0; i < post.tagList.length; i++)
                                    {
                                        post.tagList[i] = post.tagList[i].replace("<", "").replace(">", "");
                                    }
                                    break;
                                case "AnswerCount":
                                    post.answerCount = Integer.parseInt(attribute.getValue());
                                    break;
                                case "CommentCount":
                                    post.commentCount = Integer.parseInt(attribute.getValue());
                                    break;
                                case "FavoriteCount":
                                    post.favoriteCount = Integer.parseInt(attribute.getValue());
                                    break;
                                case "Score":
                                    post.score = Integer.parseInt(attribute.getValue());
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    // Go to the next event
                    continue;
                }
                // If the event signals the end of a tag
                if(event.isEndElement()) {
                    // Get the event
                    EndElement endElement = event.asEndElement();

                    // If the name of the tag that just ended is "row"
                    if(endElement.getName().getLocalPart().equals("row")) {
                        // Add the current Post object to the list
                        posts.add(post);
                    }
                }
            }
        } catch(FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }
        return posts;
    }
}
