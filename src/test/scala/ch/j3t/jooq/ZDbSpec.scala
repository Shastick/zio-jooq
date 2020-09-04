package ch.j3t.jooq

import org.jooq.SQLDialect
import org.jooq.exception.DataAccessException
import org.jooq.impl.{ DSL, SQLDataType }
import zio.ZIO
import zio.test.Assertion.{ anything, equalTo, fails, isSubtype, succeeds }
import zio.test._
import zio.test.DefaultRunnableSpec

object ZDbSpec extends DefaultRunnableSpec {

  val tbl = DSL.table("testTable")
  val fld = DSL.field("testField", SQLDataType.VARCHAR)

  override def spec = suite("ZDbSpec")(
    testM("Simple ops works") {
      val zDb = newDbWithDsl("default")
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
      val zDb          = newDbWithDsl("default")
      val faultySelect = zDb.of(_.select(fld).from(DSL.table("NotHere")).fetch())
      assertM(faultySelect.run)(fails(isSubtype[DataAccessException](anything)))
    },
    testM("Simple ops works with a transaction") {
      val zDb = newDbWithDsl("transact")
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
      val zDb          = newDbWithDsl("transact")
      val faultySelect = zDb.ofTransaction(_.select(fld).from(DSL.table("NotHere")).fetch())
      assertM(faultySelect.run)(fails(isSubtype[DataAccessException](anything)))
    },
    testM("pool-based DSLContext does not blow up") {
      val zDb         = ZDb.pooledConnections(dbCreds("pooled"), SQLDialect.H2)
      def dummyQuery  = zDb.of(_.select(fld).from(tbl).fetch())
      val createTable = zDb.of(_.createTable(tbl).column(fld).execute())
      assertM(createTable *> ZIO.collectAllPar(Set(dummyQuery, dummyQuery, dummyQuery, dummyQuery, dummyQuery)).run)(
        succeeds(anything)
      )
    }
  )

  def dbCreds(schema: String) = DbCreds(
    s"jdbc:h2:mem:db;INIT=create schema if not exists $schema\\; set schema $schema ",
    "",
    ""
  )

  /**
   * Note that the h2 database persists between tests. It's a tad ugly, but it works...
   */
  def newDbWithDsl(schema: String) =
    ZDb.singleConnection(
      dbCreds(schema),
      SQLDialect.H2
    )
}
