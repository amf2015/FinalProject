package edu.unh.cs753853.team1.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.unh.cs753853.team1.entities.Tag;

public class TagParser {

	@SuppressWarnings("unchecked")
	public HashMap<String, Tag> readTags(String tagsFile) {
		// Create a list to store the tags we read from the xml file
		HashMap<String, Tag> tags = new HashMap<>();

		// Catch: FileNotFoundException, XMLStreamException
		try {
			// Create an xml input instance
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			// Open an input stream for our tags file
			InputStream in = new FileInputStream(tagsFile);
			// Get an event reader for our tags.xml input stream
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

			// Temporarily holds our Tag object before it is inserted into the
			// list
			Tag tag = null;

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
						// Create a new blank Tag object
						tag = new Tag();

						// Get an iterator over all attributes in the tag
						Iterator<Attribute> attributes = startElement.getAttributes();

						// While there are still attributes to process
						while (attributes.hasNext()) {
							// Get the next attribute and its name
							Attribute attribute = attributes.next();
							String attrName = attribute.getName().toString();

							// Check for known attribute names and place value
							// in appropriate
							// slot in Tag object
							switch (attrName) {
							case "Count":
								tag.count = Integer.parseInt(attribute.getValue());
								break;
							case "TagName":
								tag.tagName = attribute.getValue();
								break;
							case "Id":
								tag.tagId = Integer.parseInt(attribute.getValue());
								break;
							default:
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
						// Add the current Tag object to the list
						tags.put(tag.tagName, tag);
						System.out.println(tag.tagName);
					}
				}
			}
		} catch (FileNotFoundException | XMLStreamException e) {
			e.printStackTrace();
		}
		return tags;
	}
}