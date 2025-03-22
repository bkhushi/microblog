/**
 * Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

 *  *This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
 */
package uga.menik.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.cs4370.models.FollowableUser;

/**
 * This service contains people related functions.
 */
@Service
public class PeopleService {

    /**
     * This function should query and return all users that are followable. The
     * list should not contain the user with id userIdToExclude.
     */
    @Autowired
    private DataSource dataSource;

    public List<FollowableUser> getFollowableUsers(String userIdToExclude) {
        // Write an SQL query to find the users that are not the current user.

        // Run the query with a datasource.
        // See UserService.java to see how to inject DataSource instance and
        // use it to run a query.
        // Use the query result to create a list of followable users.
        // See UserService.java to see how to access rows and their attributes
        // from the query result.
        // Check the following createSampleFollowableUserList function to see 
        // how to create a list of FollowableUsers.
        // Replace the following line and return the list you created.
        List<FollowableUser> followableUsers = new ArrayList<>();

        String query = "SELECT u.userId, u.username, u.firstName, u.lastName, "
                + "CASE WHEN f.follower_id IS NOT NULL THEN true ELSE false END as is_followed "
                + "FROM user u "
                + "LEFT JOIN follow f ON f.following_id = u.userId AND f.follower_id = ? "
                + "WHERE u.userId != ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userIdToExclude); // For checking follow status
            stmt.setString(2, userIdToExclude); // For excluding current user

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String userId = rs.getString("userId");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    boolean isFollowed = rs.getBoolean("is_followed");

                    FollowableUser user = new FollowableUser(
                            userId,
                            firstName,
                            lastName,
                            isFollowed, // Use the actual follow status
                            "Never" // Default last active date
                    );

                    followableUsers.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching followable users: " + e.getMessage());
        }

        return followableUsers;
    }

    public void followUser(String followerId, String followingId) {
        String query = "INSERT INTO follow(follower_id, following_id) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, followerId);
            stmt.setString(2, followingId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to follow user", e);
        }
    }

    public void unfollowUser(String followerId, String folowwingId) {
        String query = "DELETE FROM follow WHERE follower_id = ? AND following_id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, followerId);
            stmt.setString(2, folowwingId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to unfollow user", e);
        }
    }
}
