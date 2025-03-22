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