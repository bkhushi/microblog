/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.services.UserService;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private final DataSource dataSource;
    
    @Autowired
    public BookmarksController(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
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
        String loggedInUserId = userService.getLoggedInUser().getUserId();

        List<Post> postsToCheck = getPostsNotByCurrentUser(loggedInUserId);
        List<Post> bookmarkedPosts = new ArrayList<>();

        for (Post p : postsToCheck) {
            if (p.isBookmarked()) {
                bookmarkedPosts.add(p);
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");

        bookmarkedPosts.sort((p1, p2) -> 
            LocalDateTime.parse(p2.getPostDate(), formatter)
                        .compareTo(LocalDateTime.parse(p1.getPostDate(), formatter))
        );

        /** final sorted list should go where "posts" is! */
        mv.addObject("posts", bookmarkedPosts);


        // Following line populates sample data.
        // You should replace it with actual data from the database.
        // List<Post> posts = Utility.createSamplePostsListWithoutComments();
        // mv.addObject("posts", posts);

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        String errorMessage = "Some error occured!";
        mv.addObject("errorMessage", errorMessage);

        // Enable the following line if you want to show no content message.
        // Do that if your content list is empty.
         mv.addObject("isNoContent", true);

        return mv;
    }

    private List<Post> getPostsNotByCurrentUser(String userIdToExclude) {
        List<Post> postsNotByUser = new ArrayList<>();

        final String sql = "select * from post where userId != ?";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, userIdToExclude);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String userId = rs.getString("userId");
                    if (!userId.equals(userIdToExclude)) {
                        String postId = rs.getString("postId");
                        String content = rs.getString("content");
                        String postDate = rs.getString("postDate");

                        User user = userService.getUserById(userId); /** Need to fix here! */

                        int heartsCount = rs.getInt("heartsCount");
                        int commentsCount = rs.getInt("commentsCount");
                        boolean isHearted = rs.getBoolean("isHearted");
                        boolean isBookmarked = rs.getBoolean("isBookmarked");

                        Post posts = new Post(postId, content, postDate, user, heartsCount, commentsCount, isHearted, isBookmarked);
                        postsNotByUser.add(posts);
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return postsNotByUser;
    }
    
}
