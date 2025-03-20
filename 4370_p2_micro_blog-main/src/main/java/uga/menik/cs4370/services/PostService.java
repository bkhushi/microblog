package uga.menik.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service contains post related functions.
 */
@Service
public class PostService {

    @Autowired
    private DataSource dataSource;

    public void likePost(String userId, String postId) {
        String sql = "INSERT INTO post_likes (user_id, post_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE user_id = user_id";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, postId);
            stmt.executeUpdate();
            System.out.println("Success: liked post.");

        } catch (SQLException e) {
            throw new RuntimeException("Error liking post.", e);
        }
    }

    public void unlikePost(String userId, String postId) {
        String sql = "DELETE FROM post_likes WHERE user_id = ? AND post_id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, postId);
            stmt.executeUpdate();
            System.out.println("Success: unliked post.");

        } catch (SQLException e) {
            throw new RuntimeException("Error unliking post.", e);
        }
    }
}
