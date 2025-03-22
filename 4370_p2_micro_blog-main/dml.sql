-- Display the logged in user's bookmarked posts, and order by most recent to oldest. Used in the Bookmarks page.
-- Url:
SELECT DISTINCT p.id AS postId, p.content AS postText, p.created_at AS postDate, u.userId, u.username, u.firstName, u.lastName, 
(SELECT COUNT(*) FROM heart h WHERE h.postId = p.id) AS hearts_count, 
(SELECT COUNT(*) FROM comment c WHERE c.postId = p.id) AS comments_count, 
EXISTS (SELECT 1 FROM heart h WHERE h.postId = p.id AND h.userId = ?) AS is_hearted, 
TRUE AS is_bookmarked 
FROM post p 
JOIN bookmark b ON p.id = b.postId 
JOIN user u ON p.user_id = u.userId 
WHERE b.userId = ? 
ORDER BY p.created_at DESC;

-- Display all posts from a specific user and order by most recent to oldest. Used in the Profile page.
-- Url:
SELECT p.id AS postId, p.content AS postText, p.created_at AS postDate, u.userId, u.username, u.firstName, u.lastName, 
(SELECT COUNT(*) FROM heart h WHERE h.postId = p.id) AS hearts_count, 
(SELECT COUNT(*) FROM comment c WHERE c.postId = p.id) AS comments_count, 
EXISTS (SELECT 1 FROM heart h WHERE h.postId = p.id AND h.userId = ?) AS is_hearted, 
EXISTS (SELECT 1 FROM bookmark b WHERE b.postId = p.id AND b.userId = ?) AS is_bookmarked 
FROM post p 
JOIN user u ON p.user_id = u.userId
WHERE p.user_id = ? 
ORDER BY p.created_at DESC;

-- Get posts for home page (posts from followed users)
-- URL: http://localhost:8080/
SELECT p.id, p.content, p.created_at, u.userId, u.username, u.firstName, u.lastName,
(SELECT COUNT(*) FROM heart h WHERE h.postId = p.id) as hearts_count,
(SELECT COUNT(*) FROM comment c WHERE c.postId = p.id) as comments_count,
EXISTS(SELECT 1 FROM heart h WHERE h.postId = p.id AND h.userId = ?) as is_hearted,
EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.id AND b.userId = ?) as is_bookmarked
FROM post p
JOIN user u ON p.user_id = u.userId
WHERE p.user_id IN (
    SELECT following_id FROM follow WHERE follower_id = ?
    UNION
    SELECT ?
)
ORDER BY p.created_at DESC;

-- Search posts by hashtags
-- URL: http://localhost:8080/hashtagsearch?hashtags=java+coding
SELECT p.id, p.content, p.created_at, u.userId, u.username, u.firstName, u.lastName,
(SELECT COUNT(*) FROM heart h WHERE h.postId = p.id) as hearts_count,
(SELECT COUNT(*) FROM comment c WHERE c.postId = p.id) as comments_count,
EXISTS(SELECT 1 FROM heart h WHERE h.postId = p.id AND h.userId = ?) as is_hearted,
EXISTS(SELECT 1 FROM bookmark b WHERE b.postId = p.id AND b.userId = ?) as is_bookmarked
FROM post p
JOIN user u ON p.user_id = u.userId
WHERE p.content LIKE ? AND p.content LIKE ?
ORDER BY p.created_at DESC;

-- Get comments for a post
-- URL: http://localhost:8080/post/{postId}
SELECT c.commentId, c.commentText, c.commentDate, c.postId,
u.userId, u.username, u.firstName, u.lastName
FROM comment c
JOIN user u ON c.userId = u.userId
WHERE c.postId = ?
ORDER BY c.commentDate DESC;

-- Like a post
-- URL: http://localhost:8080/post/like/{postId}
INSERT INTO heart (postId, userId) VALUES (?, ?);

-- Unlike a post
-- URL: http://localhost:8080/post/unlike/{postId}
DELETE FROM heart WHERE postId = ? AND userId = ?;

-- Bookmark a post
-- URL: http://localhost:8080/post/bookmark/{postId}
INSERT INTO bookmark (postId, userId) VALUES (?, ?);

-- Remove bookmark
-- URL: http://localhost:8080/post/unbookmark/{postId}
DELETE FROM bookmark WHERE postId = ? AND userId = ?;

-- Add comment to post
-- URL: http://localhost:8080/post/comment/{postId}
INSERT INTO comment (postId, userId, commentText) VALUES (?, ?, ?);

-- Follow user
-- URL: http://localhost:8080/follow/{userId}
INSERT INTO follow (follower_id, following_id) VALUES (?, ?);

-- Unfollow user
-- URL: http://localhost:8080/unfollow/{userId}
DELETE FROM follow WHERE follower_id = ? AND following_id = ?;