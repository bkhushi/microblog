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
public class HashtagService {

    @Autowired
    private DataSource dataSource;

    public List<Post> getPostsByHashtags(List<String> hashtags) {
        List<Post> posts = new ArrayList<>();

        // Build LIKE conditions for each hashtag
        List<String> conditions = new ArrayList<>();
        for (int i = 0; i < hashtags.size(); i++) {
            conditions.add("p.content LIKE ?");
        }

        String query = "SELECT DISTINCT p.*, u.userId, u.firstName, u.lastName " +
                      "FROM post p " +
                      "JOIN user u ON p.user_id = u.userId " +
                      "WHERE " + String.join(" OR ", conditions) + " " +
                      "ORDER BY p.created_at DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set parameters for LIKE conditions
            for (int i = 0; i < hashtags.size(); i++) {
                stmt.setString(i + 1, "%" + "#" + hashtags.get(i) + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                        rs.getString("userId"),
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
            throw new RuntimeException("Error fetching posts by hashtags", e);
        }

        return posts;
    }
}
