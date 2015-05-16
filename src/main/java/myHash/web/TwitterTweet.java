package myHash.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Varun on 5/10/2015.
 */
public class TwitterTweet {
    private final String id;
    private final String hashTag;
    private final String myTweet;
    private final String userName;
    private final String created_at;
    private final int noOfRetweets;

    @JsonCreator
    public TwitterTweet(@JsonProperty("id") String id, @JsonProperty("hashTag") String hashTag,
                        @JsonProperty("myTweet") String myTweet, @JsonProperty("userName") String userName,
                        @JsonProperty("created_at") String created_at, @JsonProperty("noOfRetweets") int noOfRetweets) {
        this.id = id;
        this.hashTag = hashTag;
        this.myTweet = myTweet;
        this.userName = userName;
        this.created_at = created_at;
        this.noOfRetweets = noOfRetweets;
    }

    public String getId() {
        return id;
    }

    public String getHashTag() {
        return hashTag;
    }

    public String getMyTweet() {
        return myTweet;
    }

    public String getUserName() {
        return userName;
    }

    public String getCreated_at() {
        return created_at;
    }

    public int getNoOfRetweets() {
        return noOfRetweets;
    }
}
