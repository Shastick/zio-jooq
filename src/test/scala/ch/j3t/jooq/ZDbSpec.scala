package ch.j3t.jooq

import java.util.Properties

import org.jooq.SQLDialect
import org.jooq.exception.DataAccessException
import org.jooq.impl.{ DSL, SQLDataType }
import zio.ZIO
import zio.test.Assertion.{ anything, equalTo, fails, isSubtype }
import zio.test._
import zio.test.DefaultRunnableSpec

object ZDbSpec extends DefaultRunnableSpec {

  val tbl = DSL.table("testTable")
  val fld = DSL.field("testField", SQLDataType.VARCHAR)

  override def spec = suite("ZDbSpec")(
    testM("Simple ops works") {
      val zDb = new ZDb(ZIO.succeed(newDbWithDsl("default")))
      for {
        _  <- zDb.of(_.createTable(tbl).column(fld).execute())
        r1 <- zDb.of(_.select(fld).from(tbl).fetch())
        _  <- zDb.of(_.insertInto(tbl).columns(fld).values("myVal").execute())
        r2 <- zDb.of(_.select(fld).from(tbl).fetch())
      } yield assert(r1.size)(equalTo(0)) &&
        assert(r2.size)(equalTo(1)) &&
        assert(r2.get(0).get(fld))(equalTo("myVal"))
    },
    testM("obvious failures fail") {
      val zDb          = new ZDb(ZIO.succeed(newDbWithDsl("default")))
      val faultySelect = zDb.of(_.select(fld).from(DSL.table("NotHere")).fetch())
      assertM(faultySelect.run)(fails(isSubtype[DataAccessException](anything)))
    },
    testM("Simple ops works with a transaction") {
      val zDb = new ZDb(ZIO.succeed(newDbWithDsl("transact")))
      for {
        _  <- zDb.ofTransaction(_.createTable(tbl).column(fld).execute())
        r1 <- zDb.ofTransaction(_.select(fld).from(tbl).fetch())
        _  <- zDb.ofTransaction(_.insertInto(tbl).columns(fld).values("myVal").execute())
        r2 <- zDb.ofTransaction(_.select(fld).from(tbl).fetch())
      } yield assert(r1.size)(equalTo(0)) &&
        assert(r2.size)(equalTo(1)) &&
        assert(r2.get(0).get(fld))(equalTo("myVal"))
    },
    testM("obvious failures fail with a transaction") {
      val zDb          = new ZDb(ZIO.succeed(newDbWithDsl("transact")))
      val faultySelect = zDb.ofTransaction(_.select(fld).from(DSL.table("NotHere")).fetch())
      assertM(faultySelect.run)(fails(isSubtype[DataAccessException](anything)))
    }
  )

  /**
   * Note that the h2 database persists between tests. It's a tad ugly, but it works...
   */
  def newDbWithDsl(schema: String) =
    DSL.using(
      new org.h2.Driver()
        .connect(
          s"jdbc:h2:mem:db;INIT=create schema if not exists $schema\\; set schema $schema ",
          new Properties()
        ),
      SQLDialect.H2
    )
}
