# `zio-jooq`

Already using jOOq to run queries against your database from Scala? 
Quickly integrate existing code into your new ZIO App using `zio-jooq`.  

This lib essentially helps you quickly wrap ZIO's `effectBlocking()` around everything that is called on a `DSLContext`.

## Usage

```scala
val zDb = ZDb.fromDslContext(ctxt)
// alternatively:
    zDb = ZDb.fromDataSource(...)
    zDb = ZDb.fromConnection(...)
    zDb = ZDb.singleConnection(...)  
    zDb = ZDb.pooledConnections(...)

for {
  r1 <- zDb.of(_.select(field).from(table).fetch())
} yield r1

```

### Connection Pooling

You will need the hikariCp jars on the classpath if you intend to use the `pooledConnections` variants:
`zio-jooq` is built against version `3.4.5`

## About

This is an admittedly (very) thin wrapper around jOOq to let it work with ZIO. 
While it originates in a desire to learn more about ZIO, it also serves a real purpose in some codebases that rely
on jOOq and are integrating ZIO.

Please note that it is a work in progress.

## Disclaimer

This project stems from a particular way of using jOOq: if your use case is not covered, you're welcome to
implement it or fill an issue.

Additionally, I'm still pretty new to ZIO, so this library might not be the most idiomatic. 
Here again, comments are more than welcome.  

## Considerations
#### Why not directly use something like Doobie?

There are two reasons why Doobie is not involved here:
 - I only recently discovered that library, and don't know enough of it to make something useful out of it
 - `zio-jooq` is about letting you reuse as much as possible of the code you already have in a more _ZIO'ish_ way.

It's a bit early at this point to work on replacing Jooq's JDBC plumbing with Doobie, though it's a thought that is worth entertaining.
 
#### This looks like a super small lib...

Yes, it is! Honestly, it's probably better to copy paste it than to rely on it via traditional dependency management
(Please don't quote me on that ;)) 
 