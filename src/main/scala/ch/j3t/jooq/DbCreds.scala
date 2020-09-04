package ch.j3t.jooq

import com.zaxxer.hikari.HikariConfig

/**
 * @param url a JDBC url
 * @param user
 * @param password
 */
case class DbCreds(url: String, user: String, password: String) {

  /**
   * @return a HikariConfig with the minimally required settings.
   */
  def deriveHikariCfg = {
    val cfg = new HikariConfig()
    cfg.setJdbcUrl(url)
    cfg.setUsername(user)
    cfg.setPassword(password)
    cfg
  }
}
