## Investigation Project

This repository is for demonstrating the problem described in
https://stackoverflow.com/questions/57004314/how-to-solve-illegalstateexception-this-object-has-not-been-built-when-using?noredirect=1#comment100743815_57004314

# Reproducing the problem
* start the service using `gradle bootRun`
* obtain authentication tokens via endpoint `https://localhost:8444/oauth/token`
** Basic Authentication data: username: "frontend", password: "test"
** Request Body: grant_type:"password", username: "testuser@investigation.de", password: "test"
* everything should work fine
* exit from the service execution
* build jar using `gradle bootJar`
* execute the service using the command `java -jar .\build\libs\investigate-springsec-jar-only-problem.jar` (assuming you are in the project base folder)
* try to obtain authentication tokens from `https://localhost:8444/oauth/token`
* => error

# Getting rid of the problem
There are two possibilities to get rid of the error thrown by obtaining a token.
* Removing the autowired `strongEncryptor` in the class `EncryptionConverter`
* Removing the injected `PersistenceContext` in the class PasswordResetManagerImpl
Those two things are somehow in conflict, but only when running the project using the jar-file.

# But how to solve problem?