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
public class BookmarkService {
    @Autowired
    private DataSource dataSource;

    public List<Post> getBookmarkedPosts(String userIdToExclude) {
        List<Post> posts = new ArrayList<>();

        final String sql = "SELECT DISTINCT p.*, u.username, u.firstName, u.lastName " +
                "FROM post p " +
                "JOIN user u ON p.user_id = u.userId " +
                "WHERE p.isBookmarked = true AND p.user_id != ?" +
                "ORDER BY p.created_at DESC";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, userIdToExclude);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(rs.getString("userId"), 
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
        } catch(SQLException e) {
            throw new RuntimeException("Error fetching posts", e);
        }

        return posts;
    }

}
