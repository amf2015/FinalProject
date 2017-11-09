package edu.unh.cs753853.team1;

import org.apache.lucene.document.Document;

public class DocumentResults {
    private Document doc;
    private String queryId;
    private String q0 = "Q0";
    private String paragraphId;
    private int rank;
    private float score;
    private String teamName;
    private String methodName;
    DocumentResults()
    {
        doc = new Document();
        queryId = "";
        paragraphId = "";
        rank = -1;
        score = 0.0f;
        teamName = "";
        methodName = "";
    }

    DocumentResults(Document d)
    {
        doc = d;
        queryId = "";
        paragraphId = "";
        rank = -1;
        score = 0.0f;
        teamName = "";
        methodName = "";
    }

    DocumentResults(Document d, String qid, String pid, int r, float s, String tn, String mn)
    {
        doc = d;
        queryId = qid;
        paragraphId = pid;
        rank = r;
        teamName = tn;
        methodName = mn;
    }


    public Boolean scoreEquals(float s)
    {
        return (score == s);
    }

    public Boolean scoreGreaterThan(float s)
    {
       return (score > s);
    }

    public Boolean scoreLessThan(float s)
    {
        return (score < s);
    }

    public void doc(Document d)
    {
        doc = d;
    }

    public Document getDoc()
    {
        return doc;
    }

    public void queryId(String qid)
    {
        queryId = qid;
    }

    public String getQueryId()
    {
        return queryId;
    }

    public void paragraphId(String pid)
    {
        paragraphId = pid;
    }

    public String getParagraphId()
    {
        return paragraphId;
    }

    public void rank(int r)
    {
        rank = r;
    }

    public int getRank()
    {
        return rank;
    }

    public void score(float s)
    {
        score = s;
    }

    public float getScore()
    {
        return score;
    }

    public void teamName(String tn)
    {
        teamName = tn;
    }

    public String getTeamName()
    {
        return teamName;
    }

    public void methodName(String mn)
    {
        methodName = mn;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public String getRunfileString()
    {
        return (queryId + " " + q0 + " " + paragraphId + " " + rank + " " + score + " " + teamName + "-" + methodName + "\n");
    }

}
