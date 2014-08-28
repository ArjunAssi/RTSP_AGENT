package com.musigma.ird.setup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import com.musigma.ird.message.MessageBean;

/*******************************************************************************
 * THIS CLASS IS FOR INITIALIZING AND CONTROLLING THE STORAGE OF THERTSP STREAM.
 * IT PROVIDES FUNTIONS FOR WRITING THE RTSP STREAM TO A REDIS SERVER, A FLAT
 * FILE AND TO A QUEUE. IT ALSO KEEPS TRACK OF THE STORAGE DETAILS. AMD STORES
 * THIS METADATA IN A POSTGRE DATABASE | AUTHOR : ARJUN ASSI
 *******************************************************************************/

public class StreamStorageSetup {

	/*******************
	 * CLASS VARIABLES *
	 *******************/

	// Uri of the redis server
	private String uriRedis;

	// Uri of the ActiveMQ
	private String uriQueue;

	// Topic of the ActiveMQ
	private String nameQueue;

	// Uri of the meta data database postgres
	private String uriMetaDB;

	// Username
	private String userNameMetaDB;

	// Password
	private String passwordMetaDB;

	// Database name
	private String databaseNameMetaDB;

	// Table name
	private String tableNameMetaDB;

	// Logger object
	private static org.apache.log4j.Logger log = Logger
			.getLogger(StreamStorageSetup.class.getName());

	/*****************
	 * CLASS METHODS *
	 *****************/

	/*****************************
	 * GETTER AND SETTER FUNCTIONS
	 *****************************/

	public String getUriQueue() {
		return uriQueue;
	}

	public void setUriQueue(String uriQueue) {
		this.uriQueue = uriQueue;
	}

	public String getNameQueue() {
		return nameQueue;
	}

	public void setNameQueue(String nameQueue) {
		this.nameQueue = nameQueue;
	}

	public String getDatabaseNameMetaDB() {
		return databaseNameMetaDB;
	}

	public void setDatabaseNameMetaDB(String databaseNameMetaDB) {
		this.databaseNameMetaDB = databaseNameMetaDB;
	}

	public String getTableNameMetaDB() {
		return tableNameMetaDB;
	}

	public void setTableNameMetaDB(String tableNameMetaDB) {
		this.tableNameMetaDB = tableNameMetaDB;
	}

	public String getPasswordMetaDB() {
		return passwordMetaDB;
	}

	public void setPasswordMetaDB(String passwordMetaDB) {
		this.passwordMetaDB = passwordMetaDB;
	}

	public String getUriMetaDB() {
		return uriMetaDB;
	}

	public void setUriMetaDB(String uriMetaDB) {
		this.uriMetaDB = uriMetaDB;
	}

	public String getUserNameMetaDB() {
		return userNameMetaDB;
	}

	public void setUserNameMetaDB(String userNameMetaDB) {
		this.userNameMetaDB = userNameMetaDB;
	}

	public String getUriRedis() {
		return uriRedis;
	}

	public void setUriRedis(String uriRedis) {
		this.uriRedis = uriRedis;
	}

	/***********************************************************************
	 * THIS FUNCTION SETS UP A CONNECTION TO THE REDIS SERVER IDENTIFIED THE
	 * REDIS URI. IT RETURNS A REDIS CONNECTION OBJECT
	 ***********************************************************************/
	public Jedis setupRedis() {

		// Create and return a Jedis object(Java wrapper for Redis)
		return (new Jedis(uriRedis));
	}

	/*************************************************************************
	 * THIS FUNCTION INSERTS A FRAME AS A STRING WITH THE TIMESTAMP AS THE KEY
	 * IN THE REDIS SERVER
	 *************************************************************************/
	public void pushToRedis(Jedis jedis, MessageBean messageBean) {

		// Insert the Message bean as atime stamp and frame
		jedis.set(messageBean.getTimeStamp().toString(), messageBean.getImage()
				.toString());
	}

	public void closeRedis(Jedis jedis) {

		// Close the connection to the redis server
		jedis.close();
	}

	/*************************************************************************
	 * THIS FUNCTION CREATES A JMS CONNECTION BASED ON THE URI OF ACTIVEMQ AND
	 * RETURNS THE CONNECTION OBJECT
	 *************************************************************************/
	public javax.jms.Connection setupActiveMQ() {

		// Initialize the jms connection object
		javax.jms.Connection connection = null;

		// Create a connection object for the ActiveMQ
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				uriQueue);

		// Start the jms connection
		try {
			connection = connectionFactory.createConnection();
			connection.start();
		} catch (JMSException e) {
			log.error(e);
		}

		// return the jms connection object
		return connection;
	}

	/***************************************************************************
	 * THIS FUNCTION IS FOR PUSHING THE FRAME ALONG WITH ITS TIME STAMP TO QUEUE
	 * SO THAT IT CAN BE READ BY ANY SUBSCRIBER
	 ***************************************************************************/
	public void pushToActiveMQ(javax.jms.Connection connection,
			String nameQueue, MessageBean messageBean) {

		// Initialize a session of ActiveMQ
		Session session = null;

		try {
			// Create the sesssion
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create a queue
			Queue queue = session.createQueue(nameQueue);

			// Create a message object
			MessageProducer messageProducer = session.createProducer(queue);
			Message message = session.createMessage();

			// Set the time stamp and the image object in the queue
			message.setLongProperty("TimeStamp", messageBean.getTimeStamp());
			message.setObjectProperty("Image", messageBean.getImage());

			// Push the message to the Queue
			messageProducer.send(message);
		} catch (JMSException e) {
			log.error(e);
		}

	}

	/**************************************************************************
	 * THIS FUNCTION CLOSES THE CONNECTION TO THE QUEUE TO WHICH THE FRAMES ARE
	 * BEING WRITTEN
	 **************************************************************************/
	public void closeActiveMQ(javax.jms.Connection connection) {

		// Close the connection
		try {
			connection.close();
		} catch (JMSException e) {
			log.error(e);
		}
	}

	/**************************************************************************
	 * THIS FUNCTION SETS UP A CONNECTION TO THE META DATA STORAGE DATABASE. IT
	 * RETURNS A CONNECTION OBJECT THAT CAN BE USED TO UPDATE THE DATABASE
	 **************************************************************************/
	public Connection setupMetaDB() {

		// Database connection object
		Connection connection = null;

		// Database statement object to execute sql queries
		Statement statement = null;
		try {
			// Establish connection
			connection = DriverManager.getConnection(uriMetaDB + "/"
					+ databaseNameMetaDB, userNameMetaDB, passwordMetaDB);
			log.info("Opened database successfully");

			// initialize the statement object
			statement = connection.createStatement();

			// Sql query to create the table
			String sqlQuery = "CREATE TABLE " + tableNameMetaDB
					+ "(STREAM_STORAGE_TYPE CHAR(50) PRIMARY KEY NOT NULL,"
					+ "URI_DETAILS CHAR(50) NOT NULL);";

			// Execute the query and close the statement object
			statement.execute(sqlQuery);
			statement.close();
		} catch (SQLException e) {
			log.error(e);
		}

		// Return the connection object
		return connection;
	}

	/**************************************************************************
	 * THIS FUNCTION TAKES A CONNECTION OBJECT, THE STORAGE TYPE AND THE URI OF
	 * THE STORAGE STRUCTURE AND THEN UPDATES THIS IN THE METADATA DATABASE
	 **************************************************************************/
	public void updateMetaDB(Connection connection, String Storage_type,
			String URI) {

		// Create the statement object
		try {
			Statement statement = connection.createStatement();

			// Query to execute the insert operation in the table
			String sqlQuery = "INSERT INTO " + tableNameMetaDB + "VALUES ("
					+ Storage_type + "," + URI + " );";

			// Execute the query and close the statement object
			statement.execute(sqlQuery);
			statement.close();
		} catch (SQLException e) {
			log.error(e);
		}
	}

	/*************************************************************
	 * THIS FUNTION CLOSES THE CONNECTION TO THE METADATA DATABASE
	 *************************************************************/
	public void closeMetaDB(Connection connection) {
		try {

			// Close the connection to jdbc
			connection.close();
		} catch (SQLException e) {
			log.error(e);
		}
	}
}
