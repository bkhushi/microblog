/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.cs4370.controllers;

import java.sql.Connection;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;
import uga.menik.cs4370.services.UserService;

/**
 * Handles /profile URL and its sub URLs.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    // UserService has user login and registration related functions.
    private final UserService userService;
    private final DataSource dataSource; // added idk if it needs

    /**
     * See notes in AuthInterceptor.java regarding how this works 
     * through dependency injection and inversion of control.
     */
    @Autowired
    public ProfileController(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    /**
     * This function handles /profile URL itself.
     * This serves the webpage that shows posts of the logged in user.
     */
    @GetMapping
    public ModelAndView profileOfLoggedInUser() {
        System.out.println("User is attempting to view profile of the logged in user.");
        return profileOfSpecificUser(userService.getLoggedInUser().getUserId());
    }

    /**
     * This function handles /profile/{userId} URL.
     * This serves the webpage that shows posts of a speific user given by userId.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * how path variables work.
     */
    @GetMapping("/{userId}")
    public ModelAndView profileOfSpecificUser(@PathVariable("userId") String userId) {
        System.out.println("User is attempting to view profile: " + userId);
        
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("posts_page");

        // Following line populates sample data.
        // You should replace it with actual data from the database.
        // List<Post> posts = Utility.createSamplePostsListWithoutComments();
        List<Post> posts = getPostsBySpecificUser(userId);

        if (posts.isEmpty()) {
            // Enable the following line if you want to show no content message.
            // Do that if your content list is empty.
            mv.addObject("isNoContent", true);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");

            posts.sort((p1, p2) -> 
                LocalDateTime.parse(p2.getPostDate(), formatter)
                            .compareTo(LocalDateTime.parse(p1.getPostDate(), formatter))
            );
            mv.addObject("posts", posts);
        }
    

        // If an error occured, you can set the following property with the
        // error message to show the error message to the user.
        String errorMessage = "Some error occured!";
        mv.addObject("errorMessage", errorMessage);

        
        
        return mv;
    }

    private List<Post> getPostsBySpecificUser(String userId) {
        List<Post> postsByUser = new ArrayList<>();

        final String sql = "select * from post where userId == ?";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    if (userId.equals(userId)) {
                        String postId = rs.getString("postId");
                        String content = rs.getString("content");
                        String postDate = rs.getString("postDate");

                        User user = getUserById(userId); 

                        int heartsCount = rs.getInt("heartsCount");
                        int commentsCount = rs.getInt("commentsCount");
                        boolean isHearted = rs.getBoolean("isHearted");
                        boolean isBookmarked = rs.getBoolean("isBookmarked");

                        Post posts = new Post(postId, content, postDate, user, heartsCount, commentsCount, isHearted, isBookmarked);
                        postsByUser.add(posts);
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return postsByUser;
    }

    private User getUserById(String userId) {

        final String sql = "select * from users where userId == ?";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    return new User(
                        userId,
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("profileImagePath")
                    );
                    
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
}
