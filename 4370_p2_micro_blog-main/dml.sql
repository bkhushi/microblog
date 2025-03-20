-- Display bookmarked posts, excluding the logged in user, and order by most recent to oldest. Used in the Bookmarks page.
-- Url:
SELECT DISTINCT p.*, u.username, u.firstName, u.lastName
FROM post_no_comments p
JOIN user u ON p.user_id = u.userId
WHERE p.is_bookmarked = true AND p.user_id != ?
ORDER BY p.created_at DESC

-- Display all posts from a specific user and order by most recent to oldest. Used in the Profile page.
-- Url:
SELECT * 
FROM post_no_comments
WHERE user_id = ? 
ORDER BY created_at DESC