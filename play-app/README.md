# Intertextuality Graph

To follow the steps in this tutorial, you will need the correct version of Java and sbt. The template requires:

* Java Software Developer's Kit (SE) 1.8 or higher
* sbt 1.3.4 or higher. Note: if you downloaded this project as a zip file from <https://developer.lightbend.com>, the file includes an sbt distribution for your convenience.

To check your Java version, enter the following in a command window:

```bash
java -version
```

To check your sbt version, enter the following in a command window:

```bash
sbt sbtVersion
```

If you do not have the required versions, follow these links to obtain them:

* [Java SE](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [sbt](http://www.scala-sbt.org/download.html)

## Build and run the project

This example Play project was created from a seed template. It includes all Play components and an Akka HTTP server. The project is also configured with filters for Cross-Site Request Forgery (CSRF) protection and security headers.

To build and run the project:


cd ..
`docker-compose up -d`
- After the message `Server started, ...` displays, enter the following URL in a browser: <http://localhost:9000>


## Deploying in Heroku
```
./scripts/deploy-to-prod.sh

# we have ci/cd enabled, so will work. This will build a docker image using heroku.yml, then run it
git push
```

## Debugging Docker/the deploy process
```
# go in and look around
docker run -it --entrypoint /bin/bash cambodia-in-charts

# play console - https://www.playframework.com/documentation/2.8.x/PlayConsole
sbt # or try sbt console
```
# Development
## Add Dependencies
https://www.playframework.com/documentation/2.8.x/sbtDependencies#Managed-dependencies

