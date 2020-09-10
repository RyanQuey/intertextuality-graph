# why in java?
Cassandra Java driver is [possible to use in scala](https://github.com/DataStax-Examples/object-mapper-jvm/tree/master/scala), but [self-admits to being awkward and difficult to use](https://docs.datastax.com/en/developer/java-driver/4.9/manual/mapper/config/scala/), particularly the mapper. 

Note that in the example, the build.sbt is very complex and particular, using all kinds of advanced techniques, which are difficult to debug, and moreover, use javac commands anyway. Throughout the project, it says (more or less) "TODO find better way to do this".

I gave it a shot, but had the hardest time getting `sbt compile` to work. Although it is probably possible to do, it seems better just to stick with Java and build the models using that, and then integrate these models into the scala project separately. This approach seems to take advantage of the fact that scala has full compatibility with Java, and that way we are not forcing ourselves to use Scala where Java is better.

So just use mvn. And since I want to use scala with sbt, separate projects. Though...maybe in the long run, it is easiest to just use mvn for both...

### Versioning

If you want to bump the version, you don't want to use env vars for that in the pom.xml itself, since that would mean we couldn't keep track of the version of this project independent of other projects that use it as submodule. Better to just:

1) update the pom.xml var `project-package.version` in this ./models dir
2) In ../scripts/config/env-vars.sh update INTERTEXTUALITY_GRAPH_MODELS_VERSION

Note that this env var stuff is only for the sake of the parent project; this repo does not use it at all (currently)

# Debugging
## C* Java driver not building generated classes:
```
[ERROR] /home/vagrant/projects/intertextuality-graph/models/target/generated-sources/annotations/com/ryanquey/intertextualitygraph/models/InventoryMapperImpl__MapperGenerated
.java:[7,54] cannot access com.ryanquey.intertextualitygraph.models.books.BookDaoImpl__MapperGenerated
  bad source file: /home/vagrant/projects/intertextuality-graph/models/target/generated-sources/annotations/com/ryanquey/intertextualitygraph/models/books/BookDaoImpl__Mapper
Generated.java
    file does not contain class com.ryanquey.intertextualitygraph.models.books.BookDaoImpl__MapperGenerated
    Please remove or make sure it appears in the correct subdirectory of the sourcepath.

```

Along with this, at least once for me, I found no classes in the target/classes dir and if I opened the file `/home/vagrant/projects/intertextuality-graph/models/target/generated-sources/annotations/com/ryanquey/intertextualitygraph/models/books/BookDaoImpl__MapperGenerated.java`, it was blank (hence, the file did not contain the class).

### Solution
Running mvn clean install a few more times solved it (which is strange...). Might try rm -rf the target dir also. TODO find a way to make sure `mvn install` gives consistent results. Java code should not be like this.
