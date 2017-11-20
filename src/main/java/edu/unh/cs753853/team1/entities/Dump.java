package edu.unh.cs753853.team1.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dump {
    HashMap<Integer, Post> post;
    HashMap<String, Tag> tag;
    HashMap<Integer, User> user;
    HashMap<Integer, Vote> vote;
    HashMap<String, ArrayList<Post>> tagToPost;

    private void linkTags() {
        if (tag == null || post == null) {
            System.out.println("Either this.tag or this.post is null, cannot link");
            return;
        }

        if(tagToPost == null) {
            tagToPost = new HashMap<>();
        }

        for (Map.Entry<Integer, Post> postEntry : post.entrySet()) {
            Post p = postEntry.getValue();
            if (p.tagList == null)
                continue;

            p.tagMap = new HashMap<>();
            for (String t : p.tagList) {
                p.tagMap.put(t, tag.get(t));
                if(!tagToPost.containsKey(t)) {
                    tagToPost.put(t, new ArrayList<>());
                }
                tagToPost.get(t).add(p);
            }
        }
    }

    public void addPosts(HashMap<Integer, Post> pl)
    {
        this.post = pl;
    }

    public void addTags(HashMap<String, Tag> t)
    {
        this.tag = t;
        linkTags();
    }

    public void addUsers(HashMap<Integer, User> u)
    {
        this.user = u;
    }

    public void addVotes(HashMap<Integer, Vote> v)
    {
        this.vote = v;
    }

    public ArrayList<Post> getPosts()
    {
        ArrayList<Post> postList = new ArrayList<>(post.values());
        return postList;
    }

    public ArrayList<Tag> getTags()
    {
        ArrayList<Tag> tagList = new ArrayList<>(tag.values());
        return tagList;
    }

    public ArrayList<User> getUsers()
    {
        ArrayList<User> userList = new ArrayList<>(user.values());
        return userList;
    }

    public ArrayList<Vote> getVotes()
    {
        ArrayList<Vote> voteList = new ArrayList<>(vote.values());
        return voteList;
    }

    public Tag getTagByName(String tagname)
    {
        return this.tag.get(tagname);
    }

    public User getUserById(int userId)
    {
        return this.user.get(userId);
    }

    public Vote getVoteById(int voteId)
    {
        return this.vote.get(voteId);
    }

    public Post getPostById(int postId)
    {
        return this.post.get(postId);
    }

    public ArrayList<Post> getPostsWithTag(String tagName) {
        return tagToPost.get(tagName);
    }
}
