/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.services.HashtagService;
import uga.menik.cs4370.services.UserService;

/**
 * Handles /hashtagsearch URL and possibly others.
 * At this point no other URLs.
 */
@Controller
@RequestMapping("/hashtagsearch")
public class HashtagSearchController {

    private final HashtagService hashtagService;
    private final UserService userService;

    @Autowired
    public HashtagSearchController(HashtagService hashtagService, UserService userService) {
        this.hashtagService = hashtagService;
        this.userService = userService;
    }

    /**
     * This function handles the /hashtagsearch URL itself.
     * This URL can process a request parameter with name hashtags.
     * In the browser the URL will look something like below:
     * http://localhost:8081/hashtagsearch?hashtags=%23amazing+%23fireworks
     * Note: the value of the hashtags is URL encoded.
     */
    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "hashtags") String hashtags) {
        ModelAndView mv = new ModelAndView("posts_page");

        try {
            // Get current user ID
            String currentUserId = userService.getLoggedInUser().getUserId();

            List<String> hashtagList = Arrays.stream(hashtags.split("\\s+"))
                    .map(tag -> tag.startsWith("#") ? tag : "#" + tag)
                    .collect(Collectors.toList());

            List<Post> posts = hashtagService.getPostsByHashtags(hashtagList, currentUserId);
            mv.addObject("posts", posts);

            if (posts.isEmpty()) {
                mv.addObject("isNoContent", true);
            }
        } catch (Exception e) {
            mv.addObject("errorMessage", "Error searching hashtags: " + e.getMessage());
        }

        return mv;
    }

}
