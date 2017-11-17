package edu.unh.cs753853.team1.entities;

import java.util.HashMap;

public class Post {
	public int favoriteCount;
	public int answerCount;
	public String tagList[];
	public String tags;
	public HashMap<String, Tag> tagMap;
	public String postTitle;
	public int ownerUserId;
	public String postBody;
	public int viewCount;
	public int score;
	public String creationDate;
	public int acceptedAnswerId;
	public int postTypeId;
	public int postId;

	public Post() {

	}

	public int getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(int favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

	public int getAnswerCount() {
		return answerCount;
	}

	public void setAnswerCount(int answerCount) {
		this.answerCount = answerCount;
	}

	public String[] getTagList() {
		return tagList;
	}

	public void setTagList(String[] tagList) {
		this.tagList = tagList;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public HashMap<String, Tag> getTagMap() {
		return tagMap;
	}

	public void setTagMap(HashMap<String, Tag> tagMap) {
		this.tagMap = tagMap;
	}

	public String getPostTitle() {
		return postTitle;
	}

	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}

	public int getOwnerUserId() {
		return ownerUserId;
	}

	public void setOwnerUserId(int ownerUserId) {
		this.ownerUserId = ownerUserId;
	}

	public String getPostBody() {
		return postBody;
	}

	public void setPostBody(String postBody) {
		this.postBody = postBody;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public int getAcceptedAnswerId() {
		return acceptedAnswerId;
	}

	public void setAcceptedAnswerId(int acceptedAnswerId) {
		this.acceptedAnswerId = acceptedAnswerId;
	}

	public int getPostTypeId() {
		return postTypeId;
	}

	public void setPostTypeId(int postTypeId) {
		this.postTypeId = postTypeId;
	}

	public int getPostId() {
		return postId;
	}

	public void setPostId(int postId) {
		this.postId = postId;
	}
}