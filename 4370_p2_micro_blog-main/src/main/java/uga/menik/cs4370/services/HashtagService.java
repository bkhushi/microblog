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

        // Build WHERE clause to match ALL hashtags (using AND)
        List<String> conditions = new ArrayList<>();
        for (String hashtag : hashtags) {
            conditions.add("p.content LIKE ?");
        }
        String whereClause = String.join(" AND ", conditions);

        String query = "SELECT p.id, p.content, p.created_at, p.user_id, " +
                "u.userId, u.username, u.firstName, u.lastName, " +
                "(SELECT COUNT(*) FROM heart h WHERE h.postId = p.id) as hearts_count, " +
                "(SELECT COUNT(*) FROM comment c WHERE c.postId = p.id) as comments_count " +
                "FROM post p " +
                "JOIN user u ON p.user_id = u.userId " +
                "WHERE " + whereClause + " " +
                "ORDER BY p.created_at DESC";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set each hashtag as a parameter
            for (int i = 0; i < hashtags.size(); i++) {
                // Add # if not present and wrap with wildcards
                String hashtag = hashtags.get(i);
                if (!hashtag.startsWith("#")) {
                    hashtag = "#" + hashtag;
                }
                stmt.setString(i + 1, "%" + hashtag + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                            rs.getString("userId"),
                            rs.getString("firstName"),
                            rs.getString("lastName"));

                    Post post = new Post(
                            rs.getString("id"),
                            rs.getString("content"),
                            rs.getString("created_at"),
                            user,
                            rs.getInt("hearts_count"),
                            rs.getInt("comments_count"),
                            false, // isHearted
                            false // isBookmarked
                    );

                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching posts by hashtags: " + e.getMessage(), e);
        }

        return posts;
    }
}