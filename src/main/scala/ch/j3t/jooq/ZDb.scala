package ch.j3t.jooq

import java.sql.{ Connection, DriverManager }

import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }
import javax.sql.DataSource
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.{ DSLContext, SQLDialect }
import zio.{ ZIO }
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

  /**
   * @return a ZDb working from a DSLContext that wraps a single connection
   */
  def singleConnection(dbCreds: DbCreds, dialect: SQLDialect, settings: Settings = new Settings()): ZDb =
    fromConnection(DriverManager.getConnection(dbCreds.url, dbCreds.user, dbCreds.password), dialect, settings)

  /**
   * @return a ZDb working from a DSLContext that wraps a pooled data source that relies on the Hikari Connection Pool
   */
  def pooledConnections(hikariConfig: HikariConfig, dialect: SQLDialect, settings: Settings): ZDb =
    fromDataSource(new HikariDataSource(hikariConfig), dialect, settings)

  /**
   * @return a ZDb working from a DSLContext that wraps a pooled data source that relies on the Hikari Connection Pool
   *         with its default configuration.
   */
  def pooledConnections(dbCreds: DbCreds, dialect: SQLDialect, settings: Settings = new Settings()): ZDb =
    pooledConnections(dbCreds.deriveHikariCfg, dialect, settings)

  /**
   * @return a ZDb working from a DSLContext wrapping the passed datasource.
   */
  def fromDataSource(dataSource: DataSource, dialect: SQLDialect, settings: Settings = new Settings()): ZDb =
    fromDslContext(DSL.using(dataSource, dialect, settings))

  /**
   * @return a ZDb working from a DSLContext that wraps the default jooq data source
   *         (built from the passed JDBC connection)
   */
  def fromConnection(connection: Connection, dialect: SQLDialect, settings: Settings = new Settings()): ZDb =
    fromDslContext(DSL.using(connection, dialect, settings))

  /**
   * @return a ZDb that will always work from the passed DSLContext
   */
  def fromDslContext(dslContext: DSLContext): ZDb =
    new ZDb(ZIO.succeed(dslContext))

}
