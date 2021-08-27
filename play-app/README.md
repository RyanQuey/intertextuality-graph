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


```
cd ..
# docker-compose up -d
./scripts/run-dev-server.sh
```
- After the message `Server started, ...` displays, enter the following URL in a browser: <http://localhost:9000>


## Deploying in Heroku
```
./scripts/deploy-to-prod.sh

# we have ci/cd enabled, so will work. This will build a docker image using heroku.yml, then run it
git push
```

Make sure following env vars are set:

- KAFKA_URL
- APP_SECRET
- CASSANDRA_URL
- CASSANDRA_DATACENTER
- 
- 
- 


## Try the console
https://www.playframework.com/documentation/2.8.x/PlayConsole

```
./scripts/sbt/sbt.sh console
```

Now you can play around, using any of the classes it from this play app's classpath.


Some commands to try
```
import com.ryanquey.datautils.cassandraHelpers.CassandraDb
import com.ryanquey.intertextualitygraph.initializers.Initialize

new Initialize()
val graph = CassandraDb.graph
// If you want to try implicit execution:
// if this doesn't work, you know something is wrong 
graph.V().next()

// Can also try explicit execution
// can also try using CassandraDb.executeGraphTraversal(traversal); which gives better error message
import com.datastax.dse.driver.api.core.graph.DseGraph.g;
import com.datastax.dse.driver.api.core.graph.DseGraph.g._;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
import org.apache.tinkerpop.gremlin.structure.Vertex
val traversal : GraphTraversal[Vertex, Vertex]  = g.V();
val result = CassandraDb.executeGraphTraversal(traversal);
result


// try calling a raw string
import com.datastax.dse.driver.api.core.graph.ScriptGraphStatement
val groovyScript : String = "system.graphs()";
val graphs = CassandraDb.executeGraphString(groovyScript);
graphs.all()


```

## DB config
The play app is sharing configuration file with etl-tools, since etl-tools is on the classpath. See the ../etl-tools/src/main/resources/application.conf file. 


# Development
## Add Dependencies
https://www.playframework.com/documentation/2.8.x/sbtDependencies#Managed-dependencies

