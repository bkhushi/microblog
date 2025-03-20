-- Display bookmarked posts, excluding the logged in user, and order by most recent to oldest. Used in the Bookmarks page.
-- Url:
SELECT DISTINCT p.*, u.username, u.firstName, u.lastName   
FROM post p  
JOIN user u ON p.user_id = u.userId 
WHERE p.isBookmarked = true AND p.user_id != ? 
ORDER BY p.created_at DESC;

-- Display all posts from a specific user and order by most recent to oldest. Used in the Profile page.
-- Url:
SELECT * 
FROM post 
WHERE user_id = ? 
ORDER BY p.created_at DESC;