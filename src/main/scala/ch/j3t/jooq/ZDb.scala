package ch.j3t.jooq

import org.jooq.DSLContext
import zio.ZIO
import zio.blocking._

/**
 * Class wrapping a ZIO that provides a DSLContext,
 * which will be used in conjunction with the logic passed by the users.
 */
class ZDb(dslSup: ZIO[Any, Throwable, DSLContext]) {

  /**
   * Return an effect that will pass a DSLContext to the specified function when run.
   * Note: statements must be explicitly executed (ie, run/fetch/execute methods MUST be called)
   */
  def of[T](f: DSLContext => T) =
    dslSup.flatMap(dsl => ZDb.result(f)(dsl))

  /**
   * Return an effect that will run the passed function within a transaction
   * Note: statements must be explicitly executed (ie, run/fetch/execute methods MUST be called)
   */
  def ofTransaction[T](f: DSLContext => T) =
    dslSup.flatMap(dsl => ZDb.transactionResult(f)(dsl))

}

object ZDb {

  /**
   * Wrap the application of 'dsl' to 'f' within a blocking effect
   */
  private[jooq] def result[T](f: DSLContext => T)(dsl: DSLContext) =
    effectBlocking(f(dsl))

  /**
   * Runs 'f' with a dsl provided from the context of a transaction (obtained from the passed dsl)
   */
  private[jooq] def transactionResult[T](f: DSLContext => T)(dsl: DSLContext) =
    effectBlocking(dsl.transactionResult(c => f(c.dsl)))

}
