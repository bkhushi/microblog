package uga.menik.cs4370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

    public List<Post> getPostsByHashtags(List<String> hashtags, String currentUserId) {
        List<Post> posts = new ArrayList<>();

        // Build WHERE clause to match ALL hashtags
        List<String> conditions = new ArrayList<>();
        for (String hashtag : hashtags) {
            conditions.add("p.content LIKE ?");
        }
        String whereClause = String.join(" AND ", conditions);

        String query = "SELECT p.id, p.content, p.created_at, p.user_id, " +
                "u.userId, u.firstName, u.lastName, " +
                "(SELECT COUNT(*) FROM heart h WHERE h.postId = p.id) as hearts_count, " +
                "(SELECT COUNT(*) FROM comment c WHERE c.postId = p.id) as comments_count, " +
                "EXISTS(SELECT 1 FROM heart h WHERE h.postId = p.id AND h.userId = ?) as is_hearted, " +
                "EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.id AND b.userId = ?) as is_bookmarked " +
                "FROM post p " +
                "JOIN user u ON p.user_id = u.userId " +
                "WHERE " + whereClause + " " +
                "ORDER BY p.created_at DESC";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            int paramIndex = 1;

            // Set user ID for heart and bookmark checks
            stmt.setString(paramIndex++, currentUserId);
            stmt.setString(paramIndex++, currentUserId);

            // Set hashtag parameters
            for (String hashtag : hashtags) {
                String searchTag = hashtag.startsWith("#") ? hashtag : "#" + hashtag;
                stmt.setString(paramIndex++, "%" + searchTag + "%");
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
                            convertUTCtoEST(rs.getString("created_at")),
                            user,
                            rs.getInt("hearts_count"),
                            rs.getInt("comments_count"),
                            rs.getBoolean("is_hearted"), // Get actual heart status
                            rs.getBoolean("is_bookmarked") // Get actual bookmark status
                    );

                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching posts by hashtags: " + e.getMessage(), e);
        }

        return posts;
    }

    private String convertUTCtoEST(String utcTimestamp) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");
        LocalDateTime utcDateTime = LocalDateTime.parse(utcTimestamp, inputFormatter);
        ZonedDateTime utcZoned = utcDateTime.atZone(ZoneId.of("UTC"));
        ZonedDateTime estZoned = utcZoned.withZoneSameInstant(ZoneId.of("America/New_York"));

        return estZoned.format(outputFormatter);
    }
}