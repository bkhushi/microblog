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

import org.springframework.stereotype.Service;

import uga.menik.cs4370.models.Comment;
import uga.menik.cs4370.models.User;

@Service
public class CommentService {

    private DataSource dataSource;

    public CommentService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Comment> getCommentsForPost(String postId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.commentId, c.commentText, c.commentDate, c.postId, u.userId, u.username, u.firstName, u.lastName "
                + "FROM comment c "
                + "JOIN user u ON c.userId = u.userId "
                + "WHERE c.postId = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User(rs.getString("userId"), rs.getString("firstName"), rs.getString("lastName"));
                Comment comment = new Comment(
                        rs.getString("commentId"),
                        rs.getString("commentText"),
                        convertUTCtoEST(rs.getString("commentDate")),
                        user
                );
                comments.add(comment);

            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching comments for post", e);
        }
        return comments;
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
