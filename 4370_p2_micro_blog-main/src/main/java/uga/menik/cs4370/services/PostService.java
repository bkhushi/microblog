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

import uga.menik.cs4370.models.Comment;
import uga.menik.cs4370.models.ExpandedPost;
import uga.menik.cs4370.models.Post;
import uga.menik.cs4370.models.User;

@Service
public class PostService {

    @Autowired
    private DataSource dataSource;
    private UserService userService;

    public PostService(DataSource dataSource, UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
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
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser == null) {
            throw new RuntimeException("User is not authenticated.");
        }
         */
        if (userId == null || userId.isEmpty()) {
            throw new RuntimeException("User ID is required to create a post.");
        }

        final String sql = "INSERT INTO post (content, user_id) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
     * Fetches post from users that the logged-in user follows
     */
    public List<Post> getPostsFromFollowedUsers(String userId) {
        List<Post> posts = new ArrayList<>();

        String sql = "SELECT p.id, p.content, p.created_at, u.userId, u.username, u.firstName, u.lastName, "
                + "(SELECT COUNT(*) FROM heart h WHERE h.postId = p.id) AS heartsCount, "
                + "(SELECT COUNT(*) FROM comment c WHERE c.postId = p.id) AS commentsCount, "
                + "EXISTS (SELECT 1 FROM heart h WHERE h.postId = p.id AND h.userId = ?) AS isHearted, "
                + "EXISTS (SELECT 1 FROM bookmark b WHERE b.postId = p.id AND b.userId = ?) AS isBookmarked "
                + "FROM post p "
                + "JOIN user u ON p.id = u.userId "
                + "JOIN follow f ON p.id = f.following_id "
                + "WHERE f.follower_id = ? "
                + "ORDER BY p.created_at DESC";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId); // For isHearted check
            stmt.setString(2, userId); // For isBookmarked check
            stmt.setString(3, userId); // For fetching posts
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println("Columns in result: " + rs.getString("id"));
                User user = new User(rs.getString("userId"), rs.getString("firstName"), rs.getString("lastName"));

                Post post = new Post(
                        rs.getString("id"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a")),
                        user,
                        rs.getInt("heartsCount"),
                        rs.getInt("commentsCount"),
                        rs.getBoolean("isHearted"),
                        rs.getBoolean("isBookmarked")
                );
                posts.add(post);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching posts", e);
        }
        return posts;
    }

    public Post getPostById(String postId, String userId) {
        Post post = null;
        String sql = "SELECT p.id, p.content, p.created_at, u.userId, u.username, u.firstName, u.lastName, "
                + "(SELECT COUNT(*) FROM heart h WHERE h.postId = p.id) AS heartsCount, "
                + "(SELECT COUNT(*) FROM comment c WHERE c.postId = p.id) AS commentsCount, "
                + "(SELECT EXISTS (SELECT 1 FROM heart h WHERE h.postId = p.id AND h.userId = ?)) AS isHearted, "
                + "(SELECT EXISTS (SELECT 1 FROM bookmark b WHERE b.postId = p.id AND b.userId = ?)) AS isBookmarked "
                + "FROM post p "
                + "JOIN user u ON p.user_id = u.userId "
                + "WHERE p.id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);  // Set the first userId for heart checking
            stmt.setString(2, userId);  // Set the second userId for bookmark checking
            stmt.setString(3, postId);  // Set the postId

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(rs.getString("userId"), rs.getString("firstName"), rs.getString("lastName"));
                post = new Post(
                        rs.getString("id"),
                        rs.getString("content"),
                        rs.getString("created_at"),
                        user,
                        rs.getInt("heartsCount"),
                        rs.getInt("commentsCount"),
                        rs.getBoolean("isHearted"),
                        rs.getBoolean("isBookmarked")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching post by id", e);
        }

        return post; 
    }

    public List<Comment> getCommentsForPost(String postId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.commentId, c.commentText, c.commentDate, c.postId, u.userId, u.username, u.firstName, u.lastName "
                + "FROM comment c "
                + "JOIN user u ON c.userId = u.userId "
                + "WHERE c.postId = ?"
                + "ORDER BY c.commentDate ASC";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User(rs.getString("userId"), rs.getString("firstName"), rs.getString("lastName"));
                Comment comment = new Comment(
                        rs.getString("commentId"),
                        rs.getString("commentText"),
                        //rs.getString("createdDate"),
                        rs.getString("commentDate"),
                        user
                );
                comments.add(comment);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching comments for post", e);
        }

        return comments;
    }

    public String getUserIdFromPostId(String postId) {
        String sql = "SELECT user_id FROM post WHERE id = ?";
    
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, postId);
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                return rs.getString("user_id"); // Return the userId linked to this post
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching userId from postId", e);
        }
        return null; // Return null if postId doesn't exist
    } 

    public ExpandedPost getExpandedPost(String postId) {
        ExpandedPost expandedPost = null;
        List<Comment> comments = new ArrayList<>();
    
        String sql = "SELECT p.id AS postId, p.content AS postText, p.created_at AS postDate, " +
                     "       u.userId AS postUserId, u.firstName AS postFirstName, u.lastName AS postLastName, " +
                     "       (SELECT COUNT(*) FROM heart h WHERE h.postId = p.id) AS heartsCount, " +
                     "       (SELECT COUNT(*) FROM comment c WHERE c.postId = p.id) AS commentsCount, " +
                     "       (SELECT EXISTS (SELECT 1 FROM heart h WHERE h.postId = p.id AND h.userId = ?)) AS isHearted, " +
                     "       (SELECT EXISTS (SELECT 1 FROM bookmark b WHERE b.postId = p.id AND b.userId = ?)) AS isBookmarked, " +
                     "       c.commentId, c.commentText, c.commentDate, " +
                     "       cu.userId AS commentUserId, cu.firstName AS commentFirstName, cu.lastName AS commentLastName " +
                     "FROM post p " +
                     "JOIN user u ON p.user_id = u.userId " +
                     "LEFT JOIN comment c ON p.id = c.postId " +
                     "LEFT JOIN user cu ON c.userId = cu.userId " +
                     "WHERE p.id = ? " +
                     "ORDER BY c.commentDate ASC";
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            String userId = userService.getLoggedInUser().getUserId();
    
            stmt.setString(1, userId);  // Set userId for heart check
            stmt.setString(2, userId);  // Set userId for bookmark check
            stmt.setString(3, postId);  // Set postId
    
            ResultSet rs = stmt.executeQuery();
    
            // Ensure post is initialized even if there are no comments
            if (!rs.isBeforeFirst()) { // No results
                expandedPost = new ExpandedPost(postId, null, null, null, 0, 0, false, false, new ArrayList<>());
            }
    
            while (rs.next()) {
                if (expandedPost == null) {  // Initialize only once
                    User postUser = new User(
                        rs.getString("postUserId"),
                        rs.getString("postFirstName"),
                        rs.getString("postLastName")
                    );
    
                    expandedPost = new ExpandedPost(
                        rs.getString("postId"),
                        rs.getString("postText"),
                        rs.getString("postDate"),
                        postUser,
                        rs.getInt("heartsCount"),
                        rs.getInt("commentsCount"),
                        rs.getBoolean("isHearted"),
                        rs.getBoolean("isBookmarked"),
                        new ArrayList<>()
                    );
                }
    
                if (rs.getString("commentId") != null) {  // If there is a comment
                    User commentUser = new User(
                        rs.getString("commentUserId"),
                        rs.getString("commentFirstName"),
                        rs.getString("commentLastName")
                    );
    
                    Comment comment = new Comment(
                        rs.getString("commentId"),
                        rs.getString("commentText"),
                        rs.getString("commentDate"),
                        commentUser
                    );
    
                    expandedPost.getComments().add(comment);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching post with comments", e);
        }
    
        return expandedPost;
    }
    
    
    

    /* public ExpandedPost getPostWithComments(String postId) {
        ExpandedPost post = null;
        List<Comment> comments = new ArrayList<>();
    
        String sql = "SELECT p.postId, p.postText, p.postDate, p.hearts_count, p.comments_count, p.is_hearted, p.is_bookmarked, " +
                     "       u.userId AS postUserId, u.firstName AS postFirstName, u.lastName AS postLastName, " +
                     "       c.commentId, c.commentText, c.commentDate, " +
                     "       cu.userId AS commentUserId, cu.firstName AS commentFirstName, cu.lastName AS commentLastName " +
                     "FROM post p " +
                     "JOIN user u ON p.userId = u.userId " +
                     "LEFT JOIN comment c ON p.postId = c.postId " +
                     "LEFT JOIN user cu ON c.userId = cu.userId " +
                     "WHERE p.postId = ? " +
                     "ORDER BY c.commentDate ASC";
    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setString(1, postId);
            ResultSet rs = stmt.executeQuery();
    
            while (rs.next()) {
                if (post == null) { // Create the post object only once
                    User postUser = new User(
                            rs.getString("postUserId"),
                            rs.getString("postFirstName"),
                            rs.getString("postLastName")
                    );
    
                    post = new ExpandedPost(
                            rs.getString("postId"),
                            rs.getString("postText"),
                            rs.getTimestamp("postDate").toLocalDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a")),
                            postUser,
                            rs.getInt("hearts_count"),
                            rs.getInt("comments_count"),
                            rs.getBoolean("is_hearted"),
                            rs.getBoolean("is_bookmarked"),
                            comments  // Initialize empty list, will populate below
                    );
                }
    
                // Process comments if they exist
                if (rs.getString("commentId") != null) {
                    User commentUser = new User(
                            rs.getString("commentUserId"),
                            rs.getString("commentFirstName"),
                            rs.getString("commentLastName")
                    );
    
                    Comment comment = new Comment(
                            rs.getString("commentId"),
                            rs.getString("commentText"),
                            rs.getTimestamp("commentDate").toLocalDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a")),
                            commentUser
                    );
    
                    comments.add(comment);
                }
            }
    
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching post and comments", e);
        }
    
        return post;
    }
     */

    /**
     * Retrieves posts containing specified hashtags.
     *
     * @param hashtags List of hashtags to search for.
     * @return List of matching posts.
     */
    /* public List<Post> getPostsByHashtags(List<String> hashtags) {
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
    } */
    /**
     * Retrieves posts from users that the logged-in user follows.
     *
     * @return List of posts ordered from most recent to oldest.
     */
    /* public List<Post> getPostsFromFollowedUsers(String userId) {
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
     */
}
