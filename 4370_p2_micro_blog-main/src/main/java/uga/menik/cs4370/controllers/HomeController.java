/**
 * Copyright (c) 2024 Sami Menik, PhD. All rights reserved.
 *
 *  *This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
 */
package uga.menik.cs4370.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.services.PostService;
import uga.menik.cs4370.services.UserService;
import uga.menik.cs4370.utility.Utility;

/**
 * This controller handles the home page and some of it's sub URLs.
 */
@Controller
@RequestMapping
public class HomeController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    public HomeController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    /**
     * This is the specific function that handles the root URL itself.
     *
     * Note that this accepts a URL parameter called error. The value to this
     * parameter can be shown to the user as an error message. See notes in
     * HashtagSearchController.java regarding URL parameters.
     */
    @GetMapping("/sample")
    public ModelAndView sample(@RequestParam(name = "error", required = false) String error) {
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("home_page");

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        List<Post> posts = Utility.createSamplePostsListWithoutComments();
        mv.addObject("posts", posts);

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        // An error message can be optionally specified with a url query parameter too.
        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
        // mv.addObject("isNoContent", true);
        return mv;
    }

    @GetMapping
    public ModelAndView webpage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("home_page");

        String currentUserId = userService.getLoggedInUser().getUserId();
        // Fetch posts from followed users
        String currUserId = userService.getLoggedInUser().getUserId();
        List<Post> posts = postService.getPostsFromFollowedUsers(currUserId);
        mv.addObject("posts", posts);

        if (posts.isEmpty()) {
            mv.addObject("isNoContent", true);
        }

        mv.addObject("errorMessage", error);
        return mv;
    }

    /**
     * This function handles the /createpost URL. This handles a post request
     * that is going to be a form submission. The form for this can be found in
     * the home page. The form has a input field with name = posttext. Note that
     * the @RequestParam annotation has the same name. This makes it possible to
     * access the value from the input from the form after it is submitted.
     */
    @PostMapping("/createpost")
    public String createPost(@RequestParam(name = "posttext") String postText) {
        System.out.println("User is creating post: " + postText);

        String currentUserId = userService.getLoggedInUser().getUserId();

        if (postText == null || postText.trim().isEmpty()) {
            return "redirect:/?error=Post cannot be empty";
        }

        boolean success = postService.createPost(postText, currentUserId);

        if (!success) {
            return "redirect:/?error=Failed to create the post. Please try again.";
        }

        return "redirect:/";
    }

}
