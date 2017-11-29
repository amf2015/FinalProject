package edu.unh.cs753853.team1.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of the collection of .xml files found in a Dump from Stack Exchange
 *  provides access to posts, tags, users, and votes
 */
public class Dump {
    private HashMap<Integer, Post> post;
    private HashMap<String, Tag> tag;
    private HashMap<Integer, User> user;
    private HashMap<Integer, Vote> vote;
    private HashMap<String, ArrayList<String>> tagToPost;

    /**
     * Construct Dump so that all objects are initialized and not null
     */
    public Dump() {
        post = new HashMap<>();
        tag = new HashMap<>();
        user = new HashMap<>();
        vote = new HashMap<>();
        tagToPost = new HashMap<>();
    }

    /**
     * Populate each post with actual references to its Tags, instead of just
     *  string representations of the name of each tag
     */
    private void linkTags() {
        if (tag == null || post == null) {
            System.out.println("Either this.tag or this.post is null, cannot link");
            return;
        }

        for (Map.Entry<Integer, Post> postEntry : post.entrySet()) {
            Post p = postEntry.getValue();
            if (p.tagList == null)
                continue;

            p.tagMap = new HashMap<>();
            for (String t : p.tagList) {
                p.tagMap.put(t, tag.get(t));
                if(!tagToPost.containsKey(t)) {
                    tagToPost.put(t, new ArrayList<String>());
                }
                ArrayList<String> posts = tagToPost.get(t);
                posts.add(Integer.toString(p.postId));
                tagToPost.put(t, posts);
                if(tagToPost.get(t) == null)
                    System.out.println("tagToPost for " + t + " is null");
            }
        }
    }

    /**
     * Add a mapping of postId to Post to the Dump
     * @param pl A mapping from postId to Post object
     */
    public void addPosts(HashMap<Integer, Post> pl)
    {
        this.post = pl;
    }

    /**
     * Add a mapping of tagName to Tag object
     * @param t A mapping from tagName to Tag
     */
    public void addTags(HashMap<String, Tag> t)
    {
        this.tag = t;
        linkTags();
    }

    /**
     * Add a mapping of userId to User object
     * @param u A mapping from userId to User object
     */
    public void addUsers(HashMap<Integer, User> u)
    {
        this.user = u;
    }

    /**
     * Add a mapping of voteId to Vote object
     * @param v A mapping from voteId to Vote object
     */
    public void addVotes(HashMap<Integer, Vote> v)
    {
        this.vote = v;
    }

    /**
     * Returns the list of posts associated with the dump
     * @return a list of all posts
     */
    public ArrayList<Post> getPosts()
    {
        ArrayList<Post> postList = new ArrayList<>(post.values());
        return postList;
    }

    /**
     * Returns the list of tags associated with the dump
     * @return a list of all tags
     */
    public ArrayList<Tag> getTags()
    {
        ArrayList<Tag> tagList = new ArrayList<>(tag.values());
        return tagList;
    }

    /**
     * Returns the tagNames without any modification
     * @return the list of tagNames
     */
    public ArrayList<String> getRawTagNames() {
		ArrayList<String> tagNames = new ArrayList<>();
		for(Tag tag: this.getTags()) {
			tagNames.add(tag.tagName);
		}
		return tagNames;
    }

    /**
     * Returns a list of tagNames with "-" replaced with " "
     *  e.g. "tag-with-dashes" -> "tag with dashes"
     * @return a list of modified tagNames
     */
    public ArrayList<String> getReadableTagNames() {
        ArrayList<String> tagNames = new ArrayList<>();
        for(Tag tag: this.getTags()) {
            String tagName = tag.tagName.replace("-", " ");
            tagNames.add(tagName);
        }
        return tagNames;
    }

    /**
     * Returns all users in the user map
     * @return A list of user objects
     */
    public ArrayList<User> getUsers()
    {
        ArrayList<User> userList = new ArrayList<>(user.values());
        return userList;
    }

    /**
     * Returns all votes in the vote map
     * @return A list of all vote Objects
     */
    public ArrayList<Vote> getVotes()
    {
        ArrayList<Vote> voteList = new ArrayList<>(vote.values());
        return voteList;
    }

    /**
     * Gets the Tag object associated with the specified tagName
     * @param tagname the name of the tag to find
     * @return The Tag object with the given name
     */
    public Tag getTagByName(String tagname)
    {
        return this.tag.get(tagname);
    }

    /**
     * Gets the User object associated with the specified userId
     * @param userId the userId to look for
     * @return the User object with the given userId
     */
    public User getUserById(int userId)
    {
        return this.user.get(userId);
    }

    /**
     * Gets the Vote object associated with the given voteId
     * @param voteId the voteId to look for
     * @return the Vote object with the given voteId
     */
    public Vote getVoteById(int voteId)
    {
        return this.vote.get(voteId);
    }

    /**
     * Get the Post object associated with the given postId
     * @param postId the postId to look for
     * @return the Post object with the given postId
     */
    public Post getPostById(int postId)
    {
        return this.post.get(postId);
    }

    /**
     * Gathers together all the posts that are associated with the given tagName
     * @param tagName the tagName to look for
     * @return the list of posts with the given tag associated with them
     */
    public ArrayList<String> getPostsWithTag(String tagName) {
        return tagToPost.get(tagName);
    }
}
