package com.musigma.ird.setup;

/*******************************************************************************
 * THIS CLASS PROVIDES STATIC VARIABLES TO HOLD THE VALUES OF THE PROPERTIES
 * FILE. | AUTHOR : ARJUN ASSI
 *******************************************************************************/
public class Declarations {

	/*******************
	 * CLASS VARIABLES *
	 *******************/

	// This represents the rtsp stream source. eg: rtsp://ip:port/
	public static String sourceURI;

	// Uri of the redis server
	public static String uriRedis;

	// Uri of the ActiveMQ
	public static String uriQueue;

	// Topic of the ActiveMQ
	public static String nameQueue;

	// Uri of the meta data database postgre
	public static String uriMetaDB;

	// Username Metadata DB
	public static String userNameMetaDB;

	// Password Metadata DB
	public static String passwordMetaDB;

	// Database name
	public static String databaseNameMetaDB;

	// Table name
	public static String tableNameMetaDB;

	// Store stream to redis flag
	public static String storeToRedisFlag;

	// Store stream to ActiveMQ
	public static String storeToActiveMQFlag;

	// Meta DB access flag
	public static String storeToMetaDBFlag;

	// RTSP receiver agent local name
	public static String receiverAgent;

}
