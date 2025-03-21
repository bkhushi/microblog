package uga.menik.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;

@Service
public class ProfileService {

    @Autowired
    private DataSource dataSource;

    public List<Post> getPostsBySpecificUser(String userId) {
        List<Post> postsByUser = new ArrayList<>();

        final String sql = "SELECT p.id AS postId, p.content AS postText, p.created_at AS postDate, " +
                "u.userId, u.username, u.firstName, u.lastName, " +
                "(SELECT COUNT(*) FROM heart h WHERE h.postId = p.id) AS hearts_count, " +
                "(SELECT COUNT(*) FROM comment c WHERE c.postId = p.id) AS comments_count, " +
                "EXISTS (SELECT 1 FROM heart h WHERE h.postId = p.id AND h.userId = ?) AS is_hearted, " +
                "EXISTS (SELECT 1 FROM bookmark b WHERE b.postId = p.id AND b.userId = ?) AS is_bookmarked " +
                "FROM post p " +
                "JOIN user u ON p.user_id = u.userId " +
                "WHERE p.user_id = ? " +
                "ORDER BY p.created_at DESC";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId); // Checking if the logged-in user liked the post
            pstmt.setString(2, userId); // Checking if the logged-in user bookmarked the post
            pstmt.setString(3, userId); // Filtering posts by the specific user

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(rs.getString("userId"), 
                        rs.getString("firstName"),
                        rs.getString("lastName")
                    );

                    Post post = new Post(
                        rs.getString("postId"),
                        rs.getString("postText"),
                        rs.getTimestamp("postDate").toLocalDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a")),
                        user,
                        rs.getInt("hearts_count"),
                        rs.getInt("comments_count"),
                        rs.getBoolean("is_hearted"),
                        rs.getBoolean("is_bookmarked")
                    );
                    postsByUser.add(post);
                }
            }
        } catch(SQLException e) {
            throw new RuntimeException("Error fetching posts by profile", e);
        }

        return postsByUser;
    }

}
