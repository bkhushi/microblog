/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.services.BookmarkService;
import uga.menik.cs4370.services.UserService;


/**
 * Handles /bookmarks and its sub URLs.
 * No other URLs at this point.
 * 
 * Learn more about @Controller here: 
 * https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
 */
@Controller
@RequestMapping("/bookmarks")
public class BookmarksController {

    private final UserService userService;
    private final BookmarkService bookmarkService;
    
    @Autowired
    public BookmarksController(UserService userService, BookmarkService bookmarkService) {
        this.userService = userService;
        this.bookmarkService = bookmarkService;
    }


    /**
     * /bookmarks URL itself is handled by this.
     */
    @GetMapping
    public ModelAndView webpage() {
        // posts_page is a mustache template from src/main/resources/templates.
        // ModelAndView class enables initializing one and populating placeholders
        // in the template using Java objects assigned to named properties.
        ModelAndView mv = new ModelAndView("posts_page");
        

        /** Modified code starts here */
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser == null) {
            String errorMessage = "User is not logged in.";
            mv.addObject("errorMessage", errorMessage);
            return mv;
        }
        String loggedInUserId = loggedInUser.getUserId();
        try {
            List<Post> posts = bookmarkService.getBookmarkedPosts(loggedInUserId);
            if (posts.isEmpty()) {
                // Enable the following line if you want to show no content message.
                // Do that if your content list is empty.
                mv.addObject("isNoContent", true);
            } else {
                mv.addObject("posts", posts);
            }
        } catch (Exception E) {
            String errorMessage = "An error occurred while fetching posts.";
            mv.addObject("errorMessage", errorMessage);
            return mv;
        }
        
        

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        // List<Post> posts = Utility.createSamplePostsListWithoutComments();
        // mv.addObject("posts", posts);

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        
        

        

        return mv;
    }

    
    
}
