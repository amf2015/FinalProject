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

class User {
    public int accountId;
    public int age;
    public int downVotes;
    public int upVotes;
    public int views;
    public String aboutMe;
    public String lastAccessDate;
    public String displayName;
    public String creationDate;
    public int reputation;
    public int userId;
}

public class UserParser {
}
