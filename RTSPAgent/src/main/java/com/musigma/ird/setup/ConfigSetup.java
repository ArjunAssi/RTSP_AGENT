package com.musigma.ird.setup;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

/*********************************************************************
 * THIS CLASS IS FOR LOADING THE PROPERTIES FILE. | AUTHOR : ARJUN ASSI
 *********************************************************************/

public class ConfigSetup {

	/*******************
	 * CLASS VARIABLES *
	 *******************/
	private static org.apache.log4j.Logger log = Logger
			.getLogger(ConfigSetup.class.getName());

	/*****************
	 * CLASS METHODS *
	 *****************/

	/*******************************************************************
	 * THIS FUNCTION LOADS THE VALUES FROM THE PROPERTIES FILE INTO THE
	 * DECLARATIONS CLASS VARIABLES
	 *******************************************************************/
	public static void loadProperty() {

		// Create a properties object
		Properties properties = new Properties();

		// Get a input stream for the config properties file
		InputStream inputStream = ConfigSetup.class.getClassLoader()
				.getResourceAsStream("config.properties");

		// Check if the file is present or not. If not present, return null
		if (inputStream == null) {
			log.error("Could not locate the properties file");
		}

		// Load the properties file
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			log.error(e);
		}

		/***********************************************
		 * GET THE PROPERTIES FROM THE PROPERTIES FILE
		 ***********************************************/

		// RTSP source
		Declarations.sourceURI = properties.getProperty("sourceURI");

		// URI of the MetaDB
		Declarations.uriMetaDB = properties.getProperty("uriMetaDB");

		// Username
		Declarations.userNameMetaDB = properties.getProperty("userNameMetaDB");

		// Password
		Declarations.passwordMetaDB = properties.getProperty("passwordMetaDB");

		// Database Name
		Declarations.databaseNameMetaDB = properties
				.getProperty("databaseNameMetaDB");

		// Table Name
		Declarations.tableNameMetaDB = properties
				.getProperty("tableNameMetaDB");

		// URI of redis server
		Declarations.uriRedis = properties.getProperty("uriRedis");

		// URI of ActiveMQ
		Declarations.uriQueue = properties.getProperty("uriQueue");

		// Name of the Queue
		Declarations.nameQueue = properties.getProperty("nameQueue");

		// Storage type flag
		Declarations.storeToRedisFlag = properties
				.getProperty("storeToRedisFlag");
		Declarations.storeToActiveMQFlag = properties
				.getProperty("storeToActiveMQFlag");
		Declarations.storeToMetaDBFlag = properties
				.getProperty("storeToMetaDBFlag");

		// Receiver Agent
		Declarations.receiverAgent = properties.getProperty("receiverAgent");
	}
}
