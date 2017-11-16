package edu.unh.cs753853.team1.parser;

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

import edu.unh.cs753853.team1.entities.User;

public class UserParser {

	@SuppressWarnings("unchecked")
	public List<User> readUsers(String usersFile) {
		// Create a list to store the users we read from the xml file
		List<User> users = new ArrayList<>();

		// Catch: FileNotFoundException, XMLStreamException
		try {
			// Create an xml input instance
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			// Open an input stream for our users file
			InputStream in = new FileInputStream(usersFile);
			// Get an event reader for our users.xml input stream
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

			// Temporarily holds our User object before it is inserted into the
			// list
			User user = null;

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
						// Create a new blank User object
						user = new User();

						// Get an iterator over all attributes in the tag
						Iterator<Attribute> attributes = startElement.getAttributes();

						// While there are still attributes to process
						while (attributes.hasNext()) {
							// Get the next attribute and its name
							Attribute attribute = attributes.next();
							String attrName = attribute.getName().toString();

							// Check for known attribute names and place value
							// in appropriate
							// slot in User object
							switch (attrName) {
							case "AccountId":
								user.accountId = Integer.parseInt(attribute.getValue());
								break;
							case "Age":
								user.age = Integer.parseInt(attribute.getValue());
								break;
							case "DownVotes":
								user.downVotes = Integer.parseInt(attribute.getValue());
								break;
							case "UpVotes":
								user.upVotes = Integer.parseInt(attribute.getValue());
								break;
							case "Views":
								user.views = Integer.parseInt(attribute.getValue());
								break;
							case "AboutMe":
								user.aboutMe = attribute.getValue();
								break;
							case "LastAccessDate":
								user.lastAccessDate = attribute.getValue();
								break;
							case "DisplayName":
								user.displayName = attribute.getValue();
								break;
							case "CreationDate":
								user.creationDate = attribute.getValue();
								break;
							case "Reputation":
								user.reputation = Integer.parseInt(attribute.getValue());
								break;
							case "Id":
								user.userId = Integer.parseInt(attribute.getValue());
								break;
							default:
								System.out.println("Invalid <row... /> tag in users.xml");
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
						// Add the current User object to the list
						users.add(user);
					}
				}
			}
		} catch (FileNotFoundException | XMLStreamException e) {
			e.printStackTrace();
		}
		return users;
	}
}
