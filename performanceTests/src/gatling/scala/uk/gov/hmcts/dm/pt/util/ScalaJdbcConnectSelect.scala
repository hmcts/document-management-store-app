package uk.gov.hmcts.dm.pt.util

import java.sql.{Connection, Date, DriverManager, ResultSet}
import java.io._

import scala.sys.process._
/**
  * A Scala JDBC connection example by Alvin Alexander,
  * http://alvinalexander.com
  */
object ScalaJdbcConnectSelect {

  // there's probably a better way to do this
  var connection : Connection = _

  def initialise(): Unit =
  {
    // connect to the database on the localhost
    val driver = "org.postgresql.Driver"
    val url = "jdbc:postgresql://localhost:5432/evidence"
    val username = "postgres"
    val password = "postgres"
    // make the connection
    Class.forName(driver)
    connection = DriverManager.getConnection(url, username, password)
  }

  def fill_fileContent() {

    try {
      initialise()
      // create the statement, and run the select query
      val statement = connection.createStatement()
      val prep = connection.prepareStatement("INSERT INTO file_content (id, data, file_content_version_id) VALUES (?, ?, ?) ")
      prep.setInt(1, 2)
      prep.setObject(2, 44444)
      prep.setInt(3, 2)
      prep.executeUpdate
      //      val resultSet = statement.executeUpdate("Insert into public.folder (created_by, name) VALUES (\"T1\", \"T2\")")
      //      while ( resultSet.next() ) {
      //        val host = resultSet.getString("host")
      //        val user = resultSet.getString("user")
      //        println("host, user = " + host + ", " + user)
      //      }
    } catch {
      case e : Throwable => e.printStackTrace()
    }
    connection.close()
  }

  def fill_fileContentVersion() {

    try {
      initialise()
      // create the statement, and run the select query
      val statement = connection.createStatement()
      val prep = connection.prepareStatement("INSERT INTO file_content_version (id, date_created, mime_type, original_file_name, size, stored_file_id, itm_idx) VALUES (?, ?, ?, ?, ?, ?, ?) ")
      prep.setInt(1, 2)
      prep.setDate(2, Date.valueOf(java.time.LocalDate.now())) //Date.valueOf( "2017-07-24 15:07:55.456"))
      prep.setString(3, "text/plain")
      prep.setString(4, "Dummy.txt")
      prep.setInt(5, 9)
      prep.setInt(6, 2)
      prep.setInt(7, 0)

      prep.executeUpdate

    } catch {
      case e : Throwable => e.printStackTrace()
    }
    connection.close()
  }

  def fill_storedFile() {

    try {
      initialise()
      val prep = connection.prepareStatement("truncate table public.stored_file cascade;")
      prep.executeUpdate
      val record = connection.prepareStatement("Select * from stored_file")
      val reco = record.executeQuery()

      while(reco.next()){
        println(reco.getString(1))
      }
    } catch {
      case e : Throwable => e.printStackTrace()
    }
    connection.close()
  }

}