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
    public String tags[];
    public String postTitle;
    public String lastActivityDate;
    public String lastEditDate;
    public int LastEditorUserId;
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
    public List<Post> readPosts(String postsFile) {
        List<Post> posts = new ArrayList<>();
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream in = new FileInputStream(postsFile);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

            Post post = null;

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement())
                {
                    StartElement startElement = event.asStartElement();

                    if(startElement.getName().getLocalPart().equals("row")) {
                        post = new Post();

                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while(attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if(attribute.getName().toString().equals("Id")) {
                                post.postId = Integer.parseInt(attribute.getValue());
                            } else if(attribute.getName().toString().equals("PostTypeId")) {
                                post.postTypeId = Integer.parseInt(attribute.getValue());
                            } else if(attribute.getName().toString().equals("AcceptedAnswerId")) {
                                post.acceptedAnswerId = Integer.parseInt(attribute.getValue());
                            } else if(attribute.getName().toString().equals("CreationDate")) {
                                post.creationDate = 
                            }
                        }
                    }
                }
            }
        }
    }
}
