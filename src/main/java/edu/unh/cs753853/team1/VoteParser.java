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

class Vote {
    public String creationDate;
    public int voteTypeId;
    public int postId;
    public int voteId;
}

public class VoteParser {

    @SuppressWarnings("unchecked")
    public List<Vote> readVotes(String votesFile) {
        // Create a list to store the votes we read from the xml file
        List<Vote> votes = new ArrayList<>();

        // Catch: FileNotFoundException, XMLStreamException
        try {
            // Create an xml input instance
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Open an input stream for our votes file
            InputStream in = new FileInputStream(votesFile);
            // Get an event reader for our votes.xml input stream
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

            // Temporarily holds our Vote object before it is inserted into the list
            Vote vote = null;

            // While we still have XML events to read
            while (eventReader.hasNext()) {
                // Get the next event
                XMLEvent event = eventReader.nextEvent();

                // If our event signals the start of a tag
                if (event.isStartElement()) {
                    // Keep track of the tag
                    StartElement startElement = event.asStartElement();

                    // If the tags name is "row"
                    if (startElement.getName().getLocalPart().equals("row")) {
                        // Create a new blank Vote object
                        vote = new Vote();

                        // Get an iterator over all attributes in the tag
                        Iterator<Attribute> attributes = startElement.getAttributes();

                        // While there are still attributes to process
                        while (attributes.hasNext()) {
                            // Get the next attribute and its name
                            Attribute attribute = attributes.next();
                            String attrName = attribute.getName().toString();

                            // Check for known attribute names and place value in appropriate
                            //  slot in Vote object
                            switch (attrName) {
                                case "Id":
                                    vote.voteId = Integer.parseInt(attribute.getValue());
                                    break;
                                case "VoteTypeId":
                                    vote.voteTypeId = Integer.parseInt(attribute.getValue());
                                    break;
                                case "PostId":
                                    vote.postId = Integer.parseInt(attribute.getValue());
                                    break;
                                case "CreationDate":
                                    vote.creationDate = attribute.getValue();
                                    break;
                                default:
                                    System.out.println("Invalid <row... /> tag in votes.xml");
                                    break;
                            }
                        }
                    }
                    continue;
                }
                // If the event signals the end of a tag
                if (event.isEndElement()) {
                    // Get the event
                    EndElement endElement = event.asEndElement();

                    // If the name of the tag that just ended is "row"
                    if (endElement.getName().getLocalPart().equals("row")) {
                        // Add the current Vote object to the list
                        votes.add(vote);
                    }
                }
            }
        } catch (FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }
        return votes;
    }
}
