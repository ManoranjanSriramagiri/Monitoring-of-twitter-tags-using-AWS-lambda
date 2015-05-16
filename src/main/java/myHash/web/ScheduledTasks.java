package myHash.web;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.mongodb.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.social.twitter.api.SearchOperations;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Varun on 5/11/2015.
 */
@Component
public class ScheduledTasks {
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

    @Scheduled(fixedRate = 300000)
    public void callTwitterService() {
        try {
            m = new MongoClient(uri);
            db = m.getDB("twitter");
            tweetCollection = db.getCollection("tweets");
            hashTagCollection = db.getCollection("hashTags");
            DBObject myObj = null;

            cursor = hashTagCollection.find();
            try {
                while(cursor.hasNext()) {
                    myObj=cursor.next();
                    DBCursor newCursor = cursor;

                    while(newCursor.hasNext()) {
                        DBObject newObj = newCursor.next();
                        if(myObj.get("lastAccessed").toString().compareToIgnoreCase(newObj.get("lastAccessed").toString()) < 0) {
                            myObj = newObj;
                        }
                    }
                    break;
                }
            } finally {
                cursor.close();
            }

            if(myObj != null) {
                doDataBaseChanges(myObj.get("hashTag").toString(), myObj);
            }
        }
        catch(java.net.UnknownHostException e) {
            System.out.println("Custom Message "+e.getMessage());
        }
    }

    public void doDataBaseChanges(String hashTag, DBObject myObj) {
        long twitterId = Long.parseLong(myObj.get("twitterId").toString());
        df.setTimeZone(tz);
        String myLocalTime = df.format(new Date());
        SearchOperations searchOps  = twitter.searchOperations();
        long newTwitterId = searchOps.search("#" + hashTag, 1).getTweets().iterator().next().getId();

        SearchResults searchResults = searchOps.search("#" + hashTag, 1000, twitterId ,newTwitterId);
        List<Tweet> listTweet = searchResults.getTweets();

        DBObject query = new BasicDBObject("hashTag", hashTag);
        BasicDBObject updateQuery = new BasicDBObject();
        updateQuery.append("$set",new BasicDBObject().append("twitterId",newTwitterId).append("lastAccessed", myLocalTime));
        hashTagCollection.update(query, updateQuery);

        cursor = tweetCollection.find();
        try {
            while(cursor.hasNext()) {
                DBObject myNewObj=cursor.next();
                if(!cursor.hasNext()) {
                    counter.set(Long.parseLong(myNewObj.get("id").toString(),36));
                }
            }
        } finally {
            cursor.close();
        }
        if(!(listTweet.isEmpty())) {
            try {
                String myTweetStruc= "{\"tweet\":[";
                for (Tweet tweet: listTweet) {
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

//        BasicDBObject newQuery = new BasicDBObject();
//        newQuery.put("hashTag", hashTag);
//        cursor = tweetCollection.find(newQuery);
//
//        while(cursor.hasNext()) {
//            DBObject tweetObj=cursor.next();
//            TwitterTweet myTweet = new TwitterTweet(tweetObj.get("id").toString(),tweetObj.get("hashTag").toString(),
//                    tweetObj.get("myTweet").toString(),tweetObj.get("username").toString(),tweetObj.get("created_at").toString(),
//                    Integer.parseInt(tweetObj.get("noOfRetweets").toString()));
//            hashTweets.add(myTweet);
//        }
    }

    public void invokeLambda(String str) {
        AWSCredentials credentials = new BasicAWSCredentials("AKIAJCDVFGC425YXTY5Q","dGT7/DDUXzNpyaiC+fS6re3747W1C16/CZK3iC3O");
        AWSLambdaClient alc=new AWSLambdaClient(credentials);

        InvokeRequest invokeRequest = new  InvokeRequest();
        invokeRequest.setFunctionName("cmpe273East1");
        invokeRequest.setPayload(str);
        alc.invoke(invokeRequest);
    }
}
