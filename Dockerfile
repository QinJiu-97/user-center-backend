FROM java:openjdk-8u111-jdk-alpine
# copy local code to the container image
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY /target/user-center-0.0.1-SNAPSHOT.jar /app/target/user-center-0.0.1-SNAPSHOT.jar

# Build a release artifact
#RUN mvn package -DskipTests

# Run the web service on container startup
CMD ["java","-jar","/app/target/user-center-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]