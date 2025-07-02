# Build and run the project

#### Prerequisites 
* java 17
* maven
  
\-\-\-\-\-\-\-

To build the project, navigate to the repository root and run `mvn clean package`

To run the jar: `java -jar target\checkpoint_home_assignment_02_07_2025-1.0-SNAPSHOT.jar`

# How i handled:

#### Rate-limiting
Added a random sleep before each weather api call, betwee 50 and 200 milliseconds.

#### Concurrency
Ran out of time

#### Retry Logic
Ran out of time

# What i'd improve with more time
1. Refactor out the Gson code and the API fetching code to utility classes, to prevent code repetition
2. Handle external parameters (for file path, for instance)
3. Proper error handling instead of just throwing a runtime exception when anything "bad" happens
4. Study the API more to see if i can make my calls more efficient per call, ultimately leading to less calls in total.
5. Implement all the bonus requirements
