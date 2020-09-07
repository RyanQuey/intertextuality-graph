# why in java?
Cassandra Java driver is [possible to use in scala](https://github.com/DataStax-Examples/object-mapper-jvm/tree/master/scala), but [self-admits to being awkward and difficult to use](https://docs.datastax.com/en/developer/java-driver/4.9/manual/mapper/config/scala/), particularly the mapper. 

Note that in the example, the build.sbt is very complex and particular, using all kinds of advanced techniques, which are difficult to debug, and moreover, use javac commands anyway. Throughout the project, it says (more or less) "TODO find better way to do this".

I gave it a shot, but had the hardest time getting `sbt compile` to work. Although it is probably possible to do, it seems better just to stick with Java and build the models using that, and then integrate these models into the scala project separately. This approach seems to take advantage of the fact that scala has full compatibility with Java, and that way we are not forcing ourselves to use Scala where Java is better.

