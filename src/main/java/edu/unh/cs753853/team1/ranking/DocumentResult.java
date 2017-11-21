package edu.unh.cs753853.team1.ranking;

public class DocumentResult {
    private int docId;
    private float score;
    private int rank;

    DocumentResult(int id, float s) {
        this.docId = id;
        this.score = s;
        this.rank = 0;
    }

    public int getId() {
        return this.docId;
    }

    public float getScore() {
        return this.score;
    }

    public int getRank() {
        return this.rank;
    }

    public void setId(int id) {
        this.docId = id;
    }

    public void setScore(float s) {
        this.score = s;
    }

    public void setRank(int r)
    {
        this.rank = r;
    }
}
