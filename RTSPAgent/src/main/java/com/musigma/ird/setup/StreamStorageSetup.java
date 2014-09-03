package com.musigma.ird.setup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
 * THIS CLASS IS FOR INITIALIZING AND CONTROLLING THE STORAGE OF THE RTSP
 * STREAM. IT PROVIDES FUNCTIONS FOR WRITING THE RTSP STREAM TO A REDIS SERVER,
 * A FLAT FILE AND TO A QUEUE. IT ALSO KEEPS TRACK OF THE STORAGE DETAILS AND
 * STORES THIS METADATA IN A POSTGRE DATABASE. | AUTHOR : ARJUN ASSI
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

	// Uri of the meta data database postgre
	private String uriMetaDB;

	// Location and name of the flat file to store the data
	private String locationFile;

	// Number of lines before the override happens
	private int linesInFile;

	// Curent line number in the file
	private int currentLine;

	// Username Metadata DB
	private String userNameMetaDB;

	// Password Metadata DB
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

	public String getLocationFile() {
		return locationFile;
	}

	public void setLocationFile(String locationFile) {
		this.locationFile = locationFile;
	}

	public int getLinesInFile() {
		return linesInFile;
	}

	public void setLinesInFile(int linesInFile) {
		this.linesInFile = linesInFile;
	}

	public int getCurrentLine() {
		return currentLine;
	}

	public void setCurrentLine(int currentLine) {
		this.currentLine = currentLine;
	}

	/***************************************************************************
	 * THIS FUNCTION SETS UP A CONNECTION TO THE REDIS SERVER IDENTIFIED BY THE
	 * REDIS URI. IT RETURNS A REDIS CONNECTION OBJECT
	 ***************************************************************************/
	public Jedis setupRedis() {

		// Create and return a Jedis object(Java wrapper for Redis)
		Jedis jedis = new Jedis(uriRedis);
		log.info("Created a Redis connection");

		// Return the Connection object
		return jedis;
	}

	/***************************************************************************
	 * THIS FUNCTION INSERTS A FRAME AS A STRING AND THE TIMESTAMP AS THE KEY IN
	 * THE REDIS SERVER
	 ***************************************************************************/
	public void pushToRedis(Jedis jedis, MessageBean messageBean) {

		// Insert the Message bean as atime stamp and frame
		jedis.set(messageBean.getTimeStamp().toString(), messageBean
				.getByteArray().toString());
		System.out.println("Successfully pushed to redis");
	}

	/*********************************************************
	 * THIS FUNCTION CLOSES THE CONNECTION TO THE REDIS SERVER
	 *********************************************************/
	public void closeRedis(Jedis jedis) {

		// Close the connection to the redis server
		jedis.close();
		log.info("Connection to Redis closed");
	}

	/*************************************************************************
	 * THIS FUNCTION CREATES A JMS CONNECTION BASED ON THE URI OF ACTIVEMQ AND
	 * RETURNS THE SESSION OBJECT
	 *************************************************************************/
	public Session setupActiveMQ() {

		// Initialize the jms connection object
		javax.jms.Connection connection = null;

		// Initialize the session
		Session session = null;

		// Create a connection object for the ActiveMQ
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
				ActiveMQConnectionFactory.DEFAULT_BROKER_URL);

		// Start the jms connection
		try {
			connection = connectionFactory.createConnection();
			connection.start();

			// Create a session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		} catch (JMSException e) {
			log.error(e);
		}

		log.info("ActiveMQ setup at the uri : " + uriQueue);

		// Return the jms connection object
		return session;

	}

	/*************************************************************************
	 * THIS FUNCTION CREATES A QUEUE BASED ON THE URI OF ACTIVEMQ AND RETURNS
	 * THE QUEUE OBJECT
	 *************************************************************************/
	public Queue CreateActiveMQQueue(Session session, String nameQueue) {

		// Initialize the queue object
		Queue queue = null;

		// Create a queue
		try {
			queue = session.createQueue(nameQueue);

		} catch (JMSException e) {
			log.error(e);
		}

		// Return the queue object
		return queue;
	}

	/*************************************************************************
	 * THIS FUNCTION CREATES AN ACTIVE MQ MESSAGE PRODUCER BASED ON THE URI OF
	 * ACTIVEMQ AND RETURNS THAT OBJECT
	 *************************************************************************/
	public MessageProducer createMessageProducerActiveMQ(Queue queue,
			Session session) {

		// Initialize the messageProducer object
		MessageProducer messageProducer = null;

		// Create a message producer for the session and queue
		try {
			messageProducer = session.createProducer(queue);

		} catch (JMSException e) {
			log.error(e);
		}

		// Return the object
		return messageProducer;
	}

	/**************************************************************************
	 * THIS FUNCTION CREATES A MESSAGE BASED ON THE URI OF ACTIVEMQ AND RETURNS
	 * THAT OBJECT
	 **************************************************************************/
	public Message createActiveMQMessage(Session session) {

		// Initialize the message object
		Message message = null;

		// Create the message
		try {
			message = session.createMessage();

		} catch (JMSException e) {
			log.error(e);
		}

		// Return the message object
		return message;
	}

	/****************************************************************************
	 * THIS FUNCTION PUSHES A MESSAGE TO ACTIVE MQ BASED ON THE MESSAGE PRODUCER
	 ****************************************************************************/
	public void pushToActiveMQ(Message message,
			MessageProducer messageProducer, MessageBean messageBean) {

		// Set the time stamp and the image object in the queue
		try {
			message.setLongProperty("TimeStamp", messageBean.getTimeStamp());
			message.setStringProperty("Image", messageBean.getByteArray()
					.toString());

			// Push the message to the Queue
			messageProducer.send(message);

		} catch (JMSException e) {
			log.error(e);
		}
	}

	/**************************************************************************
	 * THIS FUNCTION CLOSES THE SESSION TO THE QUEUE TO WHICH THE FRAMES ARE
	 * BEING WRITTEN
	 **************************************************************************/
	public void closeActiveMQ(Session session) {

		// Close the session
		try {
			session.close();
			log.info("ActiveMQ session disconnected at the uri : " + uriQueue);

		} catch (JMSException e) {
			log.error(e);
		}
	}

	/*************************************************************************
	 * THIS FUNCTION IS FOR SETTING UP A FILE SPECIFIED IN THE PROPERTIES FILE
	 * THIS PROVIDES A CONNECTION OBJECT THAT CAN BE USED TO APPEND VALUES TO
	 * THE FILE
	 *************************************************************************/
	public BufferedWriter setupFlatFile() {

		// Buffered writer to write to the file
		BufferedWriter bufferedWriter = null;

		// File object to write to the file
		File file = new File(locationFile);

		// Initialize the current line number to 1
		currentLine = 1;

		// Initialize the buffered writer object to point to the file
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(file, true));

		} catch (IOException e) {
			log.error(e);
		}

		log.info("Flat file setup : " + locationFile);

		// Return the Buffered Writer object
		return bufferedWriter;

	}

	/***************************************************************************
	 * THIS FUNCTION WRITES TO A FILE TILL THE SPECIFIED LINE NUMBER IS
	 * MENTIONED, POST WHICH IT OVERWRITES THE EXISTING FILE FROM THE FIRST LINE
	 ****************************************************************************/
	public void writeToFile(BufferedWriter bufferedWriter,
			MessageBean messageBean) {

		try {

			// Check if the number of lines have reached the limit
			if (currentLine < linesInFile) {

				// Append to the file in this case and increment the line number
				bufferedWriter.write(messageBean.getTimeStamp() + " "
						+ messageBean.getByteArray());
			} else {

				// Create a new temporary buffered writer and override the file
				// and then input the new data from the first line
				BufferedWriter bufferedWriterToOveride = new BufferedWriter(
						new FileWriter(locationFile, true));

				// Write to the file
				bufferedWriterToOveride.write(messageBean.getTimeStamp() + " "
						+ messageBean.getByteArray());

				// Set the current line number to 1 again
				currentLine = 1;

				// Close the temporary buffered writer
				bufferedWriterToOveride.close();
			}
		} catch (IOException e) {
			log.error(e);
		}
	}

	/******************************************************
	 * THIS FUNTION CLOSES THE CONNECTION TO THE FLAT FILE
	 ******************************************************/
	public void closeFlatFile(BufferedWriter bufferedWriter) {

		// Close the buffered writer
		try {
			bufferedWriter.close();
			log.info("Flat file closed : " + locationFile);

		} catch (IOException e) {
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

			// Register Driver
			DriverManager.registerDriver(new org.postgresql.Driver());

			// Establish connection
			connection = DriverManager.getConnection(uriMetaDB
					+ databaseNameMetaDB, userNameMetaDB, passwordMetaDB);
			log.info("Opened database successfully");

			// initialize the statement object
			statement = connection.createStatement();

			// Sql query to create the table
			String sqlQuery = "CREATE TABLE " + tableNameMetaDB
					+ " (STREAM_STORAGE_TYPE CHAR(50) PRIMARY KEY NOT NULL, "
					+ "URI_DETAILS CHAR(50) NOT NULL);";

			// Execute the query and close the statement object
			statement.execute(sqlQuery);
			statement.close();

		} catch (SQLException e) {
			log.error(e);
		}

		log.info("The metadatabase has been set up at :" + uriMetaDB + "/"
				+ databaseNameMetaDB);

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
			String sqlQuery = "INSERT INTO " + tableNameMetaDB
					+ " (STREAM_STORAGE_TYPE,URI_DETAILS)" + " VALUES (" + "'"
					+ Storage_type + "'" + "," + "'" + URI + "'" + " );";

			// Execute the query and close the statement object
			statement.execute(sqlQuery);
			statement.close();

		} catch (SQLException e) {
			log.error(e);
		}
	}

	/*************************************************************
	 * THIS FUNCTION CLOSES THE CONNECTION TO THE METADATA DATABASE
	 *************************************************************/
	public void closeMetaDB(Connection connection) {
		try {

			// Close the connection to jdbc
			connection.close();
			log.info("The metadatabase has been disconnected :" + uriMetaDB
					+ databaseNameMetaDB);

		} catch (SQLException e) {
			log.error(e);
		}
	}
}
