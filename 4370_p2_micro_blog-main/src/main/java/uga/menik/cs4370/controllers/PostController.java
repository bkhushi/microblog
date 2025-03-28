/**
 * Copyright (c) 2024 Sami Menik, PhD. All rights reserved.
 *
 *  *This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
 */
package uga.menik.cs4370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.ExpandedPost;
import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.services.PostService;
import uga.menik.cs4370.services.UserService;

/**
 * Handles /post URL and its sub urls.
 */
@Controller
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostService postService;
    private UserService userService;

    @Autowired
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    /**
     * This function handles the /post/{postId} URL. This handlers serves the
     * web page for a specific post. Note there is a path variable {postId}. An
     * example URL handled by this function looks like below:
     * http://localhost:8081/post/1 The above URL assigns 1 to postId.
     *
     * See notes from HomeController.java regardig error URL parameter.
     */
    @GetMapping("/{postId}")
    public ModelAndView webpage(@PathVariable("postId") String postId,
            @RequestParam(name = "error", required = false) String error) {
        System.out.println("The user is attempting to view post with id: " + postId);

        ModelAndView mv = new ModelAndView("posts_page");

        String currUserId = userService.getLoggedInUser().getUserId();
        // Fetch the post from the database
        Post post = postService.getPostById(postId, currUserId); // Fetch the post by ID

        if (post == null) {
            System.out.println("No post found for the given postId: " + postId);
        } else {
            System.out.println("Post retrieved: " + post.getPostId() + " with content: " + post.getContent());
        }

        // Fetch the comments for the post
        List<ExpandedPost> posts = postService.getExpandedPost(postId);
        System.out.println("ExpandedPost list: " + posts);

        // Pass the post and comments directly to the view
        mv.addObject("posts", posts);

        // If an error occurred, you can set the following property with the error message
        String errorMessage = error;
        mv.addObject("errorMessage", errorMessage);

        return mv;
    }

    /**
     * Handles comments added on posts. See comments on webpage function to see
     * how path variables work here. This function handles form posts. See
     * comments in HomeController.java regarding form submissions.
     */
    @PostMapping("/{postId}/comment")
    public String postComment(@PathVariable("postId") String postId,
            @RequestParam(name = "comment") String comment) {
        System.out.println("The user is attempting add a comment:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tcomment: " + comment);

        // Redirect the user if the comment adding is a success.
        try {
            String currUserId = userService.getLoggedInUser().getUserId();
            postService.addComment(currUserId, postId, comment);
            return "redirect:/post/" + postId;
        } catch (Exception e) {
            // Redirect the user with an error message if there was an error.
            String message = URLEncoder.encode("Failed to post the comment. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/post/" + postId + "?error=" + message;
        }
    }

    /**
     * Handles likes added on posts. See comments on webpage function to see how
     * path variables work here. See comments in PeopleController.java in
     * followUnfollowUser function regarding get type form submissions and how
     * path variables work.
     */
    @GetMapping("/{postId}/heart/{isAdd}")
    public String addOrRemoveHeart(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
        System.out.println("The user is attempting add or remove a heart:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tisAdd: " + isAdd);

        // Redirect the user if the comment adding is a success.
        try {
            String currUserId = userService.getLoggedInUser().getUserId();
            if (isAdd) {
                System.out.println("userid: " + currUserId);
                System.out.println("postId: " + postId);
                postService.likePost(currUserId, postId);
            } else {
                postService.unlikePost(currUserId, postId);
            }
            return "redirect:/post/" + postId;
        } catch (Exception e) {
            // Redirect the user with an error message if there was an error.
            String message = URLEncoder.encode("Failed to (un)like the post. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/post/" + postId + "?error=" + message;
        }
    }

    /**
     * Handles bookmarking posts. See comments on webpage function to see how
     * path variables work here. See comments in PeopleController.java in
     * followUnfollowUser function regarding get type form submissions.
     */
    @GetMapping("/{postId}/bookmark/{isAdd}")
    public String addOrRemoveBookmark(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
        System.out.println("The user is attempting add or remove a bookmark:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tisAdd: " + isAdd);

        // Redirect the user if the comment adding is a success.
        try {
            String currUserId = userService.getLoggedInUser().getUserId();
            if (isAdd) {
                postService.bookmark(currUserId, postId);
            } else {
                postService.unBookmark(currUserId, postId);
            }
            return "redirect:/post/" + postId;
        } catch (Exception e) {
            // Redirect the user with an error message if there was an error.
            String message = URLEncoder.encode("Failed to (un)bookmark the post. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/post/" + postId + "?error=" + message;
        }
    }

}
