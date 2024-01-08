# spotify-messenger-bot

A bot which uses Spotify API, Genius API and a messenger platform API to send you the text of a song which is currently being played.

The app can be run by just Spring Boot Run command or in app and database Docker containers (different environment variables and run commands, see below and `.env.*` files)

# Prerequirements

-   Java
-   Docker
-   Maven (not really needed as the Maven wrapper is in this repo: `/.mvn`, can be used with a command: `./mvnw`)

# DB Migrations

DB migrations are run automatically by Flyway everytime the app is started. The strict naming should be followed:
V{version_number}\_\_clear-migration-name
Examples:

-   V0\_\_create-public-schema
-   V1_0\_\_create-user-table

# Most Used Commands

-   This command exports your vars from .env file to your OS's environment variables: `export $(xargs <.env)`: use in case SpringBoot doesn't read your .env file properly
-   Spring Boot Run (properties are configured so this way Docker containers are not being set up): `./mvnw spring-boot:run`
-   rebuild image and run Docker compose: `docker-compose up --build`
-   rebuild image and run Docker compose in background: `docker-compose up --build -d` **(use this to run the containers with app and DB)**
-   stop Docker compose containers: `docker-compose down` **(use this to stop the app and DB containers)**

# [start.spring.io](https://start.spring.io) config

![Sprint Start Config](spring-start-config.png)

# All Commands

-   This command exports your vars from .env file to your OS's environment variables: `export $(xargs <.env)`: use in case SpringBoot doesn't read your .env file properly **(most used)**
-   Spring Boot Run (propertoes are configures so thisw way Docker containers are not being set up): `./mvnw spring-boot:run`
-   Maven package (skipping tests): `./mvnw clean package -DskipTests`
-   Maven install (skipping tests): `./mvnw clean install -DskipTests` (used in Dockerfile)
-   run Docker compose: `docker-compose up`
-   build/rebuild Docker image: `docker-compose build`
-   rebuild image and run Docker compose: `docker-compose up --build` **(most used)**
-   rebuild image and run Docker compose in background: `docker-compose up --build -d` **(use this to run the containers with app and DB)** **(most used)**
-   run Docker compose and run containers in background: `docker-compose up -d`
-   force recreate Docker image, build and run Docker compose: `docker-compose up --force-recreate --build -d`
-   stop Docker compose containers: `docker-compose down` **(use this to stop the app and DB containers)** **(most used)**
-   restart Docker compose containers: `docker-compose restart`
-   pring Docker compose config (can be used to see if environment variables are used properly): `docker-compose config`

# Sources:

-   A really good guide on having both Spring Boot App and Database in Docker Containers: https://dev.to/francescoxx/java-crud-rest-api-using-spring-boot-hibernate-postgres-docker-and-docker-compose-5cln
-   Crud/JPA repository interface extending with derived queries: https://www.baeldung.com/spring-data-derived-queries
-   Postgres: https://hackernoon.com/using-postgres-effectively-in-spring-boot-applications
-   Postgres: https://www.baeldung.com/spring-boot-postgresql-docker
-   Postgres and Using Models, Repositories, Services in Controllers: https://zetcode.com/springboot/postgresql/?utm_content=cmp-true
-   Postgres Migrations (Flyway): https://www.enterprisedb.com/blog/generating-and-managing-postgresql-database-migrationsupgrades-spring-boot-jpa
-   Postgres and Docker: https://blog.devgenius.io/integrating-postgresql-database-running-on-docker-into-spring-boot-application-3e4386e422a6
-   JSON: https://www.baeldung.com/jackson-object-mapper-tutorial
-   Spring Boot and Docker: https://spring.io/guides/topicals/spring-boot-docker/
-   Spring Boot + Postgres + Docker: https://blog.phillipninan.com/how-to-containerize-spring-boot-and-postgres-jdbc
-   Spring Boot + Postgres + Docker: https://www.baeldung.com/spring-boot-postgresql-docker
-   Official Docker Guidelines for Spring Boot: https://www.docker.com/blog/9-tips-for-containerizing-your-spring-boot-code/
-   Deploying to Render (there's also something about Jib package that helps build Docker image without the need to write the Dockerfile): https://medium.com/@nithinsudarsan/deploying-a-spring-boot-application-with-docker-image-on-render-com-9a87f5ce5f72
-   Telegram Bot: https://codegym.cc/groups/posts/telegram-bot-in-java
-   Telegram and HTTP: https://github.com/samurayrj/rubenlagus-TelegramBots/blob/master/telegrambots/src/test/java/org/telegram/telegrambots/test/TestRestApi.java
-   Telegram Webhook Bot: https://stackoverflow.com/questions/74986452/telegram-bot-does-not-working-via-webhook-in-java
