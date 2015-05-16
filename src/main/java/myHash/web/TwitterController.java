package myHash.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Varun on 5/10/2015.
 */
@Controller
@JsonInclude
@RequestMapping("/")
public class TwitterController {
    private TwitterService twitterService = new TwitterService();
    private List<TwitterTweet> myTweets = new ArrayList<TwitterTweet>();
    private HashMap<String,Integer> timePerCount =
            new HashMap<String,Integer>();

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String getForm(ModelMap model) {
        model.addAttribute("hashTag", new HashTag());
        return "index";
    }

    @RequestMapping(value= "/hashtagmng", method = RequestMethod.POST)
    public String postForm(@ModelAttribute("hashTag") HashTag hashTag) {
        try {
            if(hashTag.getEmailId()!=null && !(hashTag.getEmailId().isEmpty())) {
                if(!(twitterService.getTrends().isEmpty())) {
                    SendEmail sndEmail = new SendEmail(twitterService.getTrends(),hashTag.getEmailId());
                }
            }
        } catch (NullPointerException e) {
        }
        myTweets = twitterService.getUserHashTag(hashTag.getHashText());
        timePerCount = twitterService.getNoOfTweets(myTweets);
        return "redirect:timeline";
    }

    @RequestMapping(value = "/timeline", method = RequestMethod.GET)
    public String getData(ModelMap model) {
        model.addAttribute("tweetList",myTweets);
        model.addAttribute("timePerCount",timePerCount);
        return "timeline";
    }

    @RequestMapping(value = "/contact", method = RequestMethod.GET)
    public String getContact() {
        return "contact";
    }

    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public String getAbout() {
        return "about";
    }

    @RequestMapping(value= "/email", method = RequestMethod.GET)
    public void sendEmail(@ModelAttribute("email") HashTag hashTag) {
        return ;
    }

}
