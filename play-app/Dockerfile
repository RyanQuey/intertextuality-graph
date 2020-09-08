FROM mozilla/sbt:8u232_1.3.13

ARG ENVIRONMENT
ENV PLAY_PROJECT_NAME=intertextuality-graph-play-api

# keep in sync with build.sbt
ENV PLAY_PROJECT_VERSION=1.0

ENV OPENJDK_TAG=8u232 
# we're setting 1.3.4 in our build.sbt, but latest is 1.3.13
ENV SBT_VERSION=1.3.13

# if are doing dev, would use a volume instead
COPY . /app 
WORKDIR /app 

# IN PROD
# build a prod deployable tarball
# https://www.playframework.com/documentation/2.8.x/Deploying#Using-the-dist-task
# RUN sbt universal:packageZipTarball

# IN DEV will just do sbt run, so live reloads

# now build dist for prod and unzip it (zip file name based on build.sbt), or if just in dev, compile so later updates to image are faster to build
RUN [ $ENVIRONMENT == "production" ] && \
      sbt dist && \
      unzip /app/target/universal/${PLAY_PROJECT_NAME}-${PLAY_PROJECT_VERSION}-SNAPSHOT.zip || echo "in dev, don't do anything. package even slows things down, since if we change packages later just have to rebuild"

# finally, can run it
ENTRYPOINT [ $ENVIRONMENT == "production"] && /app/entrypoint.sh || /app/entrypoint.dev.sh
