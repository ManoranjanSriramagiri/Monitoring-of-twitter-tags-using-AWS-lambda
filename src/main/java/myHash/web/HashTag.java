package myHash.web;

import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by Varun on 5/11/2015.
 */
@Component
public class HashTag {
    private String hashText;
    private long twitterId;
    private Date lastAccessed;
    private String emailId;

    public String getHashText() {
        return hashText;
    }

    public void setHashText(String hashText) {
        this.hashText = hashText;
    }

    public long getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(long twitterId) {
        this.twitterId = twitterId;
    }

    public Date getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Date lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }
}
