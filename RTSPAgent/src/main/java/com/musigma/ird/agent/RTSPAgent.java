package com.musigma.ird.agent;

import java.io.BufferedWriter;
import java.sql.Connection;

import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.musigma.ird.behaviours.RTSPAgentBehaviour;
import com.musigma.ird.message.MessageBean;
import com.musigma.ird.setup.Declarations;
import com.musigma.ird.setup.RTSPHandler;
import com.musigma.ird.setup.StreamStorageSetup;

/*****************************************************************************
 * THIS CLASS IS THE AGENT BODY OF THE RTSP AGENT. IT ADDS A CYCLIC BEHAVIOUR
 * THAT IS IMPLEMENTED IN THE BEHAVIOURS PACKAGE. | AUTHOR : ARJUN ASSI
 *****************************************************************************/
public class RTSPAgent extends Agent {

	/*******************
	 * CLASS VARIABLES *
	 *******************/

	private static final long serialVersionUID = 1L;

	// Acl message object
	private ACLMessage aclMessage;

	// Message bean to store the image
	private MessageBean messageBean;

	// Stream storage object to handle the storage of the stream
	private StreamStorageSetup streamStorageSetup;

	// Connection to jdbc
	private Connection connectionDB;

	// Connection to Redis
	private Jedis jedis;

	// Session variable for ActiveMQ
	private Session session;

	// Message variable to store the message in ActiveMQ
	private Message message;

	// Message producer variable for ActiveMQ
	private MessageProducer messageProducer;

	// Queue
	private Queue queue;

	// buffered writer to write to the flat file
	private BufferedWriter bufferedWriter;

	// RTSP handler object to parse the stream
	private RTSPHandler rtspHandler;

	// Logger object
	private static org.apache.log4j.Logger log = Logger
			.getLogger(RTSPAgent.class.getName());

	/*****************
	 * CLASS METHODS *
	 *****************/

	/*************************************************************************
	 * THIS FUNCTION IS THE SETUP FUNCTION. IT CREATES OBJECTS OF MESSAGE BEAN,
	 * STREAM STORAGE CLASS AND OF THE STREAM HANDLING CLASS AND PASSES IT TO
	 * THE BEHAVIOUR
	 *************************************************************************/
	protected void setup() {

		// Initialize the ACL message and add the receiver
		aclMessage = new ACLMessage(ACLMessage.INFORM);
		aclMessage.addReceiver(new AID(Declarations.receiverAgent,
				AID.ISLOCALNAME));

		/*********************
		 * MESSAGE BEAN SETUP
		 *********************/

		// This object captures the buffered image and the timestamp of the
		// image and wraps it in a bean
		messageBean = new MessageBean();

		/****************
		 * STREAM STORAGE
		 ****************/

		// This object provides funtions for parsing and handling the stream
		streamStorageSetup = new StreamStorageSetup();

		/****************
		 * META DB SETUP
		 ****************/

		// SetUp MetaDB
		streamStorageSetup.setUriMetaDB(Declarations.uriMetaDB);
		streamStorageSetup
				.setDatabaseNameMetaDB(Declarations.databaseNameMetaDB);
		streamStorageSetup.setTableNameMetaDB(Declarations.tableNameMetaDB);
		streamStorageSetup.setUserNameMetaDB(Declarations.userNameMetaDB);
		streamStorageSetup.setPasswordMetaDB(Declarations.passwordMetaDB);

		// Connect to MetaDB
		connectionDB = streamStorageSetup.setupMetaDB();

		/**************
		 * REDIS SETUP
		 **************/

		// SetUp Redis
		streamStorageSetup.setUriRedis(Declarations.uriRedis);

		// Connect To redis
		jedis = streamStorageSetup.setupRedis();

		/*****************
		 * ACTIVEMQ SETUP
		 *****************/

		// SetUp ActiveMQ
		streamStorageSetup.setUriQueue(Declarations.uriQueue);
		streamStorageSetup.setNameQueue(Declarations.nameQueue);

		// Connect To ActiveMQ
		session = streamStorageSetup.setupActiveMQ();

		// Create a queue
		queue = streamStorageSetup.CreateActiveMQQueue(session,
				streamStorageSetup.getNameQueue());

		// Create a message producer
		messageProducer = streamStorageSetup.createMessageProducerActiveMQ(
				queue, session);

		// create a message
		message = streamStorageSetup.createActiveMQMessage(session);

		/*******************
		 * SET UP FLAT FILE
		 *******************/

		// Setup the Flat File
		streamStorageSetup.setLocationFile(Declarations.locationFile);
		streamStorageSetup.setLinesInFile(Declarations.linesInFile);

		// Connect to the file
		bufferedWriter = streamStorageSetup.setupFlatFile();

		/*****************
		 * UPDATE META DB
		 *****************/

		// Redis
		streamStorageSetup.updateMetaDB(connectionDB, "Redis",
				streamStorageSetup.getUriRedis());

		// ActiveMQ
		streamStorageSetup.updateMetaDB(connectionDB, "ActiveMQ",
				streamStorageSetup.getUriQueue());

		// FlatFile
		streamStorageSetup.updateMetaDB(connectionDB, "FlatFile",
				streamStorageSetup.getLocationFile());

		// Close MetaDB
		streamStorageSetup.closeMetaDB(connectionDB);

		/*********************
		 * RTSP HANDLER SETUP
		 *********************/

		// This object provides the stream storage funtionalities like writing
		// to redis, ActiveMQ
		rtspHandler = new RTSPHandler();

		// Load Native library
		rtspHandler.loadNativeLibrary();

		// SetUp RTSPhandler
		rtspHandler.connectToRTSPSource(Declarations.sourceURI);

		// This behaviour is the implementation of the agents activity. Inside
		// its action method calls will be made to all the other classes. Its a
		// cyclic behaviour
		addBehaviour(new RTSPAgentBehaviour(aclMessage, streamStorageSetup,
				connectionDB, jedis, session, queue, messageProducer, message,
				rtspHandler, messageBean, bufferedWriter));

		// Log the outcome once the behviour is setup
		log.info("Created the RTSP Agent and added its behaviour");
	}
}
