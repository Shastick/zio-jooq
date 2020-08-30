package ch.j3t.jooq

import org.jooq.DSLContext
import zio.ZIO
import zio.blocking._

/**
 * Using effectBlocking with a synchronous usage of the DSL (eg, no use of the xxxAsync methods)
 */
class ZDb(dslSup: ZIO[Any, Throwable, DSLContext]) {

  def of[T](f: DSLContext => T) =
    dslSup.flatMap(dsl => ZDb.result(f)(dsl))

  def ofTransaction[T](f: DSLContext => T) =
    dslSup.flatMap(dsl => ZDb.transactionResult(f)(dsl))

}

class ZIODSLContext(underlying: DSLContext) {

  def zioResult[T](f: DSLContext => T) =
    ZDb.result(f)(underlying)

  def zioTransactionResult[T](f: DSLContext => T) =
    ZDb.transactionResult(f)(underlying)

}

object ZDb {

  implicit def toZioDsl(dsl: DSLContext): ZIODSLContext = new ZIODSLContext(dsl)

  private[jooq] def result[T](f: DSLContext => T)(dsl: DSLContext) =
    effectBlocking(f(dsl))

  private[jooq] def transactionResult[T](f: DSLContext => T)(dsl: DSLContext) =
    effectBlocking(dsl.transactionResult(c => f(c.dsl)))

}
