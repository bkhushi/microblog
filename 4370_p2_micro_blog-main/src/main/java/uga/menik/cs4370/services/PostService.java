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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.cs4370.models.Comment;
import uga.menik.cs4370.models.ExpandedPost;
import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;

@Service
public class PostService {

    @Autowired
    private DataSource dataSource;
    private UserService userService;
    private CommentService commentService;

    public PostService(DataSource dataSource, UserService userService, CommentService commentService) {
        this.dataSource = dataSource;
        this.userService = userService;
        this.commentService = commentService;
    }

    public void likePost(String userId, String postId) {
        final String sql = "INSERT INTO heart (userId, postId) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, postId);
            pstmt.executeUpdate();
            System.out.println("Success: liked post");

        } catch (SQLException e) {
            throw new RuntimeException("Error liking post: ", e);
        }
    }

    public void unlikePost(String userId, String postId) {
        final String sql = "DELETE FROM heart WHERE userId = ? AND postId = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, postId);
            pstmt.executeUpdate();
            System.out.println("Success: unliked post");

        } catch (SQLException e) {
            throw new RuntimeException("Error unliking post: ", e);
        }
    }

    public void bookmark(String userId, String postId) {
        final String sql = "INSERT INTO bookmark (userId, postId) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, postId);
            pstmt.executeUpdate();
            System.out.println("Success: bookmarked post");
        } catch (SQLException e) {
            throw new RuntimeException("Error bookmarking post: ", e);
        }
    }

    public void unBookmark(String userId, String postId) {
        final String sql = "DELETE from bookmark WHERE userId = ? AND postId = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, postId);
            pstmt.executeUpdate();
            System.out.println("Success: bookmarked post");
        } catch (SQLException e) {
            throw new RuntimeException("Error bookmarking post: ", e);
        }
    }

    public void addComment(String userId, String postId, String commentText) {
        String sql = "INSERT INTO comment (postId, userId, commentText) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, postId);
            stmt.setString(2, userId);
            stmt.setString(3, commentText);

            stmt.executeUpdate();
            System.out.println("Success: commented on post");
        } catch (SQLException e) {
            throw new RuntimeException("Error commenting on post", e);
        }
    }

    /**
     * Creates a new post for the logged-in user. Supports hashtags, which are
     * automatically extracted and linked.
     */
    public boolean createPost(String content, String userId) {
        /*
         * User loggedInUser = userService.getLoggedInUser();
         * if (loggedInUser == null) {
         * throw new RuntimeException("User is not authenticated.");
         * }
         */
        if (userId == null || userId.isEmpty()) {
            throw new RuntimeException("User ID is required to create a post.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Post content cannot be empty.");
        }
        
        final String sql = "INSERT INTO post (content, user_id) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, content);
            pstmt.setString(2, userId);
            // pstmt.setString(2, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating post", e);
        }
    }

    /**
     * Fetches post from users that the logged-in user follows
     */
    public List<Post> getPostsFromFollowedUsers(String userId) {
        List<Post> posts = new ArrayList<>();

        String sql = "SELECT p.id, p.content, p.created_at, "
                + "u.userId, u.username, u.firstName, u.lastName, "
                + "(SELECT COUNT(*) FROM heart h WHERE h.postId = p.id) as hearts_count, "
                + "(SELECT COUNT(*) FROM comment c WHERE c.postId = p.id) as comments_count, "
                + "EXISTS(SELECT 1 FROM heart h WHERE h.postId = p.id AND h.userId = ?) as is_hearted, "
                + "EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.id AND b.userId = ?) as is_bookmarked "
                + "FROM post p "
                + "JOIN user u ON p.user_id = u.userId "
                + "WHERE p.user_id IN ( "
                + "    SELECT following_id FROM follow WHERE follower_id = ? "
                + "    UNION "
                + "    SELECT ? "
                + // Include posts from the logged-in user
                ") "
                + "ORDER BY p.created_at DESC";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId); // For is_hearted check
            stmt.setString(2, userId); // For is_bookmarked check
            stmt.setString(3, userId); // For followed users
            stmt.setString(4, userId); // For self posts

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
                            rs.getBoolean("is_hearted"),
                            rs.getBoolean("is_bookmarked"));
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching posts from followed users: " + e.getMessage(), e);
        }

        return posts;
    }

    public List<ExpandedPost> getExpandedPost(String postId) {
        String currUserId = userService.getLoggedInUser().getUserId();
        Post post = getPostById(postId, currUserId);
        List<Comment> commentsForPost = commentService.getCommentsForPost(postId);

        return List.of(new ExpandedPost(
                post.getPostId(),
                post.getContent(),
                post.getPostDate(),
                post.getUser(),
                post.getHeartsCount(),
                commentsForPost.size(),
                post.getHearted(),
                post.isBookmarked(),
                commentsForPost));
    }

    public Post getPostById(String postId, String userId) {
        Post post = null;

        String sql = "SELECT p.id, p.content, p.created_at, u.userId, u.username, u.firstName, u.lastName, "
                + "(SELECT COUNT(*) FROM heart h WHERE h.postId = p.id) AS heartsCount, "
                + "(SELECT COUNT(*) FROM comment c WHERE c.postId = p.id) AS commentsCount, "
                + "EXISTS (SELECT 1 FROM heart h WHERE h.postId = p.id AND h.userId = ?) AS isHearted, "
                + "EXISTS (SELECT 1 FROM bookmark b WHERE b.postId = p.id AND b.userId = ?) AS isBookmarked "
                + "FROM post p "
                + "JOIN user u ON p.user_id = u.userId "
                + "WHERE p.id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId); // For hearted check
            stmt.setString(2, userId); // For bookmarked check
            stmt.setString(3, postId); // Fetch post by postId
            ResultSet rs = stmt.executeQuery();
            System.out.println(" Fetching post for postId: " + postId + " using userId: " + userId);
            if (rs.next()) {
                User user = new User(rs.getString("userId"), rs.getString("firstName"), rs.getString("lastName"));

                // Format the date
                String formattedDate = "Never";
                Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");
                    formattedDate = sdf.format(timestamp);
                }

                post = new Post(
                        rs.getString("id"),
                        rs.getString("content"),
                        convertUTCtoEST(rs.getString("created_at")),
                        user,
                        rs.getInt("heartsCount"),
                        rs.getInt("commentsCount"),
                        rs.getBoolean("isHearted"),
                        rs.getBoolean("isBookmarked"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching post by ID", e);
        }

        return post;
    }

    private String convertUTCtoEST(String utcTimestamp) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");
        LocalDateTime utcDateTime = LocalDateTime.parse(utcTimestamp, inputFormatter);
        ZonedDateTime utcZoned = utcDateTime.atZone(ZoneId.of("UTC"));
        ZonedDateTime estZoned = utcZoned.withZoneSameInstant(ZoneId.of("America/New_York"));

        return estZoned.format(outputFormatter);
    }

    /**
     * Retrieves posts containing specified hashtags.
     *
     * @param hashtags List of hashtags to search for.
     * @return List of matching posts.
     */
    /*
     * public List<Post> getPostsByHashtags(List<String> hashtags) {
     * List<Post> posts = new ArrayList<>();
     * 
     * String query = "SELECT DISTINCT p.*, u.userId, u.firstName, u.lastName " +
     * "FROM post p " +
     * "JOIN user u ON p.user_id = u.userId " +
     * "JOIN post_hashtag ph ON p.id = ph.post_id " +
     * "JOIN hashtag h ON ph.hashtag_id = h.id " +
     * "WHERE h.tag IN (" + String.join(",", hashtags) + ") " +
     * "ORDER BY p.created_at DESC";
     * 
     * try (Connection conn = dataSource.getConnection();
     * PreparedStatement stmt = conn.prepareStatement(query)) {
     * for (int i = 0; i < hashtags.size(); i++) {
     * stmt.setString(i + 1, hashtags.get(i));
     * }
     * 
     * try (ResultSet rs = stmt.executeQuery()) {
     * while (rs.next()) {
     * User user = new User(rs.getString("userId"), rs.getString("firstName"),
     * rs.getString("lastName"));
     * Post post = new Post(
     * rs.getString("id"),
     * rs.getString("content"),
     * rs.getString("created_at"),
     * user,
     * rs.getInt("hearts_count"),
     * rs.getInt("comments_count"),
     * rs.getBoolean("is_hearted"),
     * rs.getBoolean("is_bookmarked")
     * );
     * posts.add(post);
     * }
     * }
     * } catch (SQLException e) {
     * throw new RuntimeException("Error fetching posts by hashtags", e);
     * }
     * return posts;
     * }
     */
    /**
     * Retrieves posts from users that the logged-in user follows.
     *
     * @return List of posts ordered from most recent to oldest.
     */
    /*
     * public List<Post> getPostsFromFollowedUsers(String userId) {
     * List<Post> posts = new ArrayList<>();
     * 
     * String sql = "SELECT p.*, u.username, u.firstName, u.lastName " +
     * "FROM post p " +
     * "JOIN user u ON p.user_id = u.userId " +
     * "LEFT JOIN follow f ON p.user_id = f.following_id AND f.follower_id = ? " +
     * "WHERE f.follower_id IS NOT NULL OR p.user_id = ? " + // Include self-posts
     * "ORDER BY p.created_at DESC";
     * 
     * try (Connection conn = dataSource.getConnection();
     * PreparedStatement pstmt = conn.prepareStatement(sql)) {
     * 
     * pstmt.setString(1, userId); // First param: followed users
     * pstmt.setString(2, userId); // Second param: self-posts
     * 
     * try (ResultSet rs = pstmt.executeQuery()) {
     * while (rs.next()) {
     * User user = new User(
     * rs.getString("user_id"),
     * rs.getString("firstName"),
     * rs.getString("lastName")
     * );
     * 
     * Post post = new Post(
     * rs.getString("id"),
     * rs.getString("content"),
     * rs.getString("created_at"),
     * user,
     * rs.getInt("hearts_count"),
     * rs.getInt("comments_count"),
     * rs.getBoolean("is_hearted"),
     * rs.getBoolean("is_bookmarked")
     * );
     * posts.add(post);
     * }
     * }
     * } catch (SQLException e) {
     * throw new RuntimeException("Error fetching posts from followed users", e);
     * }
     * 
     * return posts;
     * }
     */
}
