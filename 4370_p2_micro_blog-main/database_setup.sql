-- Create the database.
create database if not exists cs4370_mb_platform;

-- Use the created database.
use cs4370_mb_platform;

-- Create the user table.
create table if not exists user (
    userId int auto_increment,
    username varchar(255) not null,
    password varchar(255) not null,
    firstName varchar(255) not null,
    lastName varchar(255) not null,
    primary key (userId),
    unique (username),
    constraint userName_min_length check (char_length(trim(userName)) >= 2),
    constraint firstName_min_length check (char_length(trim(firstName)) >= 2),
    constraint lastName_min_length check (char_length(trim(lastName)) >= 2)
);

CREATE TABLE if not exists follow (
    follower_id INT,
    following_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, following_id),
    FOREIGN KEY (follower_id) REFERENCES user(userID),
    FOREIGN KEY (following_id) REFERENCES user(userID)
);

-- Create post table if not exists
CREATE TABLE if not exists post (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(userId)
);

-- Create the comment table. 
CREATE TABLE comment (
    commentId INT AUTO_INCREMENT PRIMARY KEY,  -- Unique comment ID
    postId INT NOT NULL,  -- Foreign key referencing post(id)
    userId INT NOT NULL,  -- Foreign key referencing user(userId)
    commentDate DATETIME DEFAULT CURRENT_TIMESTAMP,  -- Timestamp of comment
    commentText TEXT NOT NULL,  -- Comment content

    -- Foreign key constraints
    CONSTRAINT fk_comment_post FOREIGN KEY (postId) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE
);

-- Create the heart table.
CREATE TABLE heart (
    postId INT NOT NULL,  -- Foreign key referencing post(id)
    userId INT NOT NULL,  -- Foreign key referencing user(userId)

    PRIMARY KEY (postId, userId),  -- Composite primary key (ensures unique likes)
    CONSTRAINT fk_heart_post FOREIGN KEY (postId) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_heart_user FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE
);

-- Create the bookmark table.
CREATE TABLE bookmark (
    postId INT NOT NULL,  -- Foreign key referencing post(id)
    userId INT NOT NULL,  -- Foreign key referencing user(userId)

    PRIMARY KEY (postId, userId),  -- Composite primary key (ensures unique bookmarks)
    CONSTRAINT fk_bookmark_post FOREIGN KEY (postId) REFERENCES post(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmark_user FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE
);

