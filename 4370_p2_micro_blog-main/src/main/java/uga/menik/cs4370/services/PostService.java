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

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;

@Service
public class PostService {

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private UserService userService;

    /**
     * Creates a new post for the logged-in user.
     * Supports hashtags, which are automatically extracted and linked.
     */
    public boolean createPost(String content, String userId) {
        /* 
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser == null) {
            throw new RuntimeException("User is not authenticated.");
        }
            */
        if (userId == null || userId.isEmpty()) {
            throw new RuntimeException("User ID is required to create a post.");
        }

        final String sql = "INSERT INTO post (content, user_id) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, content);
            pstmt.setString(2, userId);
            //pstmt.setString(2, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating post", e);
        }
    }

    /**
     * Retrieves posts containing specified hashtags.
     * @param hashtags List of hashtags to search for.
     * @return List of matching posts.
     */
    public List<Post> getPostsByHashtags(List<String> hashtags) {
        List<Post> posts = new ArrayList<>();
        
        String query = "SELECT DISTINCT p.*, u.userId, u.firstName, u.lastName " +
                "FROM post p " +
                "JOIN user u ON p.user_id = u.userId " +
                "JOIN post_hashtag ph ON p.id = ph.post_id " +
                "JOIN hashtag h ON ph.hashtag_id = h.id " +
                "WHERE h.tag IN (" + String.join(",", hashtags) + ") " +
                "ORDER BY p.created_at DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < hashtags.size(); i++) {
                stmt.setString(i + 1, hashtags.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(rs.getString("userId"), rs.getString("firstName"), rs.getString("lastName"));
                    Post post = new Post(
                            rs.getString("id"),
                            rs.getString("content"),
                            rs.getString("created_at"),
                            user,
                            rs.getInt("hearts_count"),
                            rs.getInt("comments_count"),
                            rs.getBoolean("is_hearted"),
                            rs.getBoolean("is_bookmarked")
                    );
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching posts by hashtags", e);
        }
        return posts;
    }

    /**
     * Retrieves posts from users that the logged-in user follows.
     * @return List of posts ordered from most recent to oldest.
     */
    public List<Post> getPostsFromFollowedUsers(String userId) {
        List<Post> posts = new ArrayList<>();
    
        String sql = "SELECT p.*, u.username, u.firstName, u.lastName " +
                     "FROM post p " +
                     "JOIN user u ON p.user_id = u.userId " +
                     "LEFT JOIN follow f ON p.user_id = f.following_id AND f.follower_id = ? " +
                     "WHERE f.follower_id IS NOT NULL OR p.user_id = ? " + // Include self-posts
                     "ORDER BY p.created_at DESC";
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setString(1, userId); // First param: followed users
            pstmt.setString(2, userId); // Second param: self-posts
    
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                        rs.getString("user_id"),
                        rs.getString("firstName"),
                        rs.getString("lastName")
                    );
    
                    Post post = new Post(
                        rs.getString("id"),
                        rs.getString("content"),
                        rs.getString("created_at"),
                        user,
                        rs.getInt("hearts_count"),
                        rs.getInt("comments_count"),
                        rs.getBoolean("is_hearted"),
                        rs.getBoolean("is_bookmarked")
                    );
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching posts from followed users", e);
        }
    
        return posts;
    }
    
    
}
