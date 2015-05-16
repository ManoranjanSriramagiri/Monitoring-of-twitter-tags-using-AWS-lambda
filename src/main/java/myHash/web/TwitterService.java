package myHash.web;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.mongodb.*;
import org.springframework.social.twitter.api.*;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Varun on 5/10/2015.
 */
public class TwitterService {
    private AtomicLong counter = new AtomicLong();
    String textUri = "mongodb://test:test@ds031952.mongolab.com:31952/twitter";
    MongoClientURI uri = new MongoClientURI(textUri);
    MongoClient m = null;
    DB db = null;
    DBCollection tweetCollection = null;
    DBCollection hashTagCollection = null;
    DBCursor cursor = null;
    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'dd:HH:mm.sss'Z'");
    private static Twitter twitter = new TwitterTemplate("sTZFgeC2YxIQH3jJSUVTgtUE4","6prFnkYOUicvcRWPIyIaJj0C7BMgFOrEXqOKiUUNyL1lM5hYBd");
    List<Tweet> listTweet = new ArrayList<Tweet>();

    public TwitterService() {
        try {
            m = new MongoClient(uri);
            db = m.getDB("twitter");
            tweetCollection = db.getCollection("tweets");
            hashTagCollection = db.getCollection("hashTags");
        }
        catch(java.net.UnknownHostException e) {
            System.out.println("Custom Message "+e.getMessage());
        }
    }

    public ArrayList getUserHashTag(String hashTag) {
        ArrayList listTweets = null;
        DBObject query = new BasicDBObject("hashTag", hashTag);
        DBObject dbObj = hashTagCollection.findOne(query);
        if(dbObj != null) {
            listTweets = getTweetsLatest(hashTag, Long.parseLong(dbObj.get("twitterId").toString()));
        }
        else {
            listTweets = getTweets(hashTag);
        }
        return listTweets;
    }

    public ArrayList getTweets(String hashTag) {
        df.setTimeZone(tz);
        String myLocalTime = df.format(new Date());
        SearchOperations searchOps  = twitter.searchOperations();
        long twitterId = searchOps.search("#" + hashTag, 1).getTweets().iterator().next().getId();

        SearchResults searchResults = searchOps.search("#" + hashTag, 5000);
        listTweet = searchResults.getTweets();

        BasicDBObject document = new BasicDBObject();
        document.append("hashTag", hashTag);
        document.append("twitterId", twitterId);
        document.append("lastAccessed", myLocalTime);
        hashTagCollection.insert(document);

        return addTweets(hashTag, listTweet);
    }

    public ArrayList getTweetsLatest(String hashTag, long twitterId) {
        df.setTimeZone(tz);
        String myLocalTime = df.format(new Date());
        SearchOperations searchOps  = twitter.searchOperations();
        long newTwitterId = searchOps.search("#" + hashTag, 1).getTweets().iterator().next().getId();

        SearchResults searchResults = searchOps.search("#" + hashTag, 1000, twitterId ,newTwitterId);
        listTweet = searchResults.getTweets();

        DBObject query = new BasicDBObject("hashTag", hashTag);
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append("$set",new BasicDBObject().append("twitterId",newTwitterId).append("lastAccessed", myLocalTime));
        hashTagCollection.update(query, updateQuery);

        return addTweets(hashTag, listTweet);
    }

    public ArrayList addTweets(String hashTag, List<Tweet> newList) {
        ArrayList hashTweets=new ArrayList();
        cursor = tweetCollection.find();
        try {
            while(cursor.hasNext()) {
                DBObject myObj=cursor.next();
                if(!cursor.hasNext()) {
                    counter.set(Long.parseLong(myObj.get("id").toString(),36));
                }
            }
        } finally {
            cursor.close();
        }
        if(!(newList.isEmpty())) {
            try {
                String myTweetStruc= "{\"tweet\":[";
                for (Tweet tweet: newList) {
                    long tempId=counter.incrementAndGet();
                    String tweetId = java.lang.Long.toString(tempId, 36);

                    myTweetStruc += "{\"id\":\"" + tweetId +"\",\"hashTag\":\"" + hashTag +"\",\"myTweet\":\"" + tweet.getText().replaceAll("[\\n\r]","").replaceAll("\\n[\t]*\n","").replaceAll("\"","'") +
                            "\",\"username\":\"" + tweet.getFromUser() +"\",\"created_at\":\"" + tweet.getCreatedAt().toString() +"\",\"noOfRetweets\":\"" + tweet.getRetweetCount() + "\"},";

                }
                String tempStruc = myTweetStruc.subSequence(0, myTweetStruc.lastIndexOf(',')).toString();
                tempStruc += "]}";

                invokeLambda(tempStruc);
            } catch(Exception e) {
                System.out.println("Custom Message "+e.getMessage());
            }
        }

        BasicDBObject query = new BasicDBObject();
        query.put("hashTag", hashTag);
        cursor = tweetCollection.find(query);

        while(cursor.hasNext()) {
            DBObject myObj=cursor.next();
            TwitterTweet myTweet = new TwitterTweet(myObj.get("id").toString(),myObj.get("hashTag").toString(),
                    myObj.get("myTweet").toString(),myObj.get("username").toString(),myObj.get("created_at").toString(),
                    Integer.parseInt(myObj.get("noOfRetweets").toString()));
            hashTweets.add(myTweet);
            Collections.sort(hashTweets, new Comparator<TwitterTweet>() {
                public int compare(TwitterTweet t1, TwitterTweet t2) {
                    Date d1 = new Date(Date.parse(t1.getCreated_at()));
                    Date d2 = new Date(Date.parse(t2.getCreated_at()));
                    return d2.compareTo(d1);
                }
            });
        }
        return hashTweets;
    }

    public void invokeLambda(String str) {
        AWSCredentials credentials = new BasicAWSCredentials("AKIAJCDVFGC425YXTY5Q","dGT7/DDUXzNpyaiC+fS6re3747W1C16/CZK3iC3O");
        AWSLambdaClient alc=new AWSLambdaClient(credentials);

        InvokeRequest invokeRequest = new  InvokeRequest();
        invokeRequest.setFunctionName("cmpe273East1");
        invokeRequest.setPayload(str);
        alc.invoke(invokeRequest);
    }

    public ArrayList getTrends() {
        ArrayList<String> listTrends = new ArrayList<String>();
        long woei = Long.parseLong("23424977");
        SearchOperations searchOps  = twitter.searchOperations();
        TimelineOperations timelineOperations = twitter.timelineOperations();
        //timelineOperations
        Trends trends = searchOps.getLocalTrends(woei);
        List<Trend> listTrend = trends.getTrends();
        for(Trend trend : listTrend) {
            listTrends.add(trend.getName());
            //System.out.println(trend.getName() + " : " + trend.getQuery());
        }
        return listTrends;
    }

    public HashMap<String,Integer> getNoOfTweets(List<TwitterTweet> ltTweet) {
        TimelineOperations userOperations = twitter.timelineOperations();
        StreamingOperations streamingOperations = twitter.streamingOperations();
        SearchOperations searchOperations = twitter.searchOperations();
        HashMap<String,Integer> hsMap = new HashMap<String, Integer>();
        Date nowTime = new Date();
        for(int i=5; i>=1; i--) {
            int count = 0;
            for (TwitterTweet tweet: ltTweet) {
                Date d1 = new Date(Date.parse(tweet.getCreated_at()));
                String[] tempSplit1 = nowTime.toString().split(" ");
                int hour = Integer.parseInt(tempSplit1[3].split(":")[0]);
                String[] tempSplit2 = d1.toString().split(" ");
                if(tempSplit1[5].equalsIgnoreCase(tempSplit2[5]) && tempSplit1[1].equalsIgnoreCase(tempSplit2[1])
                        && tempSplit1[2].equalsIgnoreCase(tempSplit2[2]) && (hour-i)==Integer.parseInt(tempSplit2[3].split(":")[0])) {
                    count++;
                }
            }
            String tempStr = (nowTime.getHours()-i) + " - " + ((nowTime.getHours()-i) + 1);
            hsMap.put(tempStr, count);
        }
        return hsMap;
    }
}
