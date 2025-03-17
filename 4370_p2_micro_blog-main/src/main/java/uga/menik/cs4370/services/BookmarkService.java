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

    public List<Post> getPostsNotByCurrentUser(String userIdToExclude) {
        List<Post> postsNotByUser = new ArrayList<>();

        final String sql = "select * from post where userId != ?";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Following line replaces the first place holder with username.
            pstmt.setString(1, userIdToExclude);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String userId = rs.getString("userId");
                    if (!userId.equals(userIdToExclude)) {
                        String postId = rs.getString("postId");
                        String content = rs.getString("content");
                        String postDate = rs.getString("postDate");

                        User user = getUserById(userId); /** need to join tables in sql */

                        int heartsCount = rs.getInt("heartsCount");
                        int commentsCount = rs.getInt("commentsCount");
                        boolean isHearted = rs.getBoolean("isHearted");
                        boolean isBookmarked = rs.getBoolean("isBookmarked");

                        Post posts = new Post(postId, content, postDate, user, heartsCount, commentsCount, isHearted, isBookmarked);
                        postsNotByUser.add(posts);
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return postsNotByUser;
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
