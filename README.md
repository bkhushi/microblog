# microblog

Group: 4 Members: Khushi Bhatamrekar, Aditi Chiluguri, Catherine Chu, Grace Ryoo

Contributions:

Khushi Bhatamrekar:
- people page
- hashtag search
- recorded demo video

Aditi Chiluguri:
- home page
- posts

Catherine Chu: Implemented features of post interactions, including liking, unliking, bookmarking, and adding comments. Enhanced the home page to display posts from followed users. Developed the expanded post view to provide a detailed display of individual posts and associated interactions. Made sure posts cannot be empty and displayed an error message. 

Grace Ryoo:
- profile page
- bookmark page

How to run:

Start the MySQL docker container and get the mysql prompt.

Create the database and run all dml statements given in the database_setup.sql file. This is needed only if this is the first time running the web app.

Navigate to the directory with the pom.xml using the terminal in your local machine and run the following command:
On unix like machines:
mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Dserver.port=8081'

On windows command line:
mvn spring-boot:run -D"spring-boot.run.arguments=--server.port=8081"

On windows power shell:
mvn spring-boot:run --% -Dspring-boot.run.arguments="--server.port=8081"

Open the browser and navigate to the following URL:
http://localhost:8081/

Create an account and login.

For the homepage, the posts that show are from the users the logged in user follows as well as the logged in user's own posts. 