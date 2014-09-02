package com.musigma.ird.behaviours;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.sql.Connection;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import com.musigma.ird.message.MessageBean;
import com.musigma.ird.setup.Declarations;
import com.musigma.ird.setup.RTSPHandler;
import com.musigma.ird.setup.StreamStorageSetup;

/********************************************************************************
 * THIS CLASS IMPLEMENTS THE CYCLIC BEHAVIOUR OF THE AGENT. IT RECEIVES AN RTSP
 * STREAM AND KEEPS PARSING IT AND STORES IT TILL THE STREAM ENDS. | AUTHOR :
 * ARJUN ASSI
 ********************************************************************************/
public class RTSPAgentBehaviour extends CyclicBehaviour {

	/*******************
	 * CLASS VARIABLES *
	 *******************/

	private static final long serialVersionUID = 1L;

	// This class variable handles stream storage related functions
	StreamStorageSetup streamStorageSetup;

	// This class variable handles stream parsing related functions
	RTSPHandler rtspHandler;

	// This class variable handles encapsulating the image and the timestamp in
	// one bean
	MessageBean messageBean;

	// Connection to MetaDB
	Connection connectionDB;

	// Connection to redis
	Jedis jedis;

	// ActiveMQ session
	Session session;

	// ActiveMQ message
	Message message;

	// ActiveMQ Message Producer
	MessageProducer messageProducer;

	// ActiveMQ Queue
	Queue queue;

	// Acl message object
	ACLMessage aclMessage;

	// Logger object
	private static org.apache.log4j.Logger log = Logger
			.getLogger(RTSPAgentBehaviour.class.getName());

	/*****************
	 * CLASS METHODS *
	 *****************/

	/********************************************************************
	 * THIS IS THE CONCTRUCTOR METHOD FOR THIS BEHAVIOUR. IT ASSIGNS THE
	 * ARGUMENTS PASSED FROM THE SETUP FUNCTION TO THE BEHAVIOUR
	 ********************************************************************/
	public RTSPAgentBehaviour(ACLMessage aclMessage,
			StreamStorageSetup streamStorageSetup, Connection connectionDB,
			Jedis jedis, Session session, Queue queue,
			MessageProducer messageProducer, Message message,
			RTSPHandler rtspHandler, MessageBean messageBean) {

		// Initialize the class variables
		this.streamStorageSetup = streamStorageSetup;
		this.rtspHandler = rtspHandler;
		this.messageBean = messageBean;
		this.connectionDB = connectionDB;
		this.jedis = jedis;
		this.session = session;
		this.messageProducer = messageProducer;
		this.message = message;
		this.queue = queue;
		this.aclMessage = aclMessage;

	}

	/********************
	 * SUPER CONSTRUCTOR
	 ********************/
	public RTSPAgentBehaviour(Agent a) {
		super(a);
	}

	/****************************************************************************
	 * THIS IS THE ACTION METHOD. IT RECEIVES THE STREAM AND PARSES EACH FRAME
	 * AND STORES IT AS A BYTE STREAM OBJECT WITH THE TIMESTAMP AND PASSES IT TO
	 * THE NEXT AGENT. IT ALSO STORES THIS STREAM INTO REDIS AND ACTIVEMQ
	 ****************************************************************************/
	public void action() {

		// Initialize the holder for the image
		byte[] byteArray = null;

		// Initialize the timestamp
		Long timeStamp = null;

		// Get the next frame from the stream
		byteArray = rtspHandler.parseStream();

		// Check if the stream actually contains frames
		if (byteArray != null) {
			timeStamp = System.currentTimeMillis();

			// Encapsulate this info in the message bean
			messageBean.setByteArray(byteArray);
			messageBean.setTimeStamp(timeStamp);

			// Set the content of the ACL message
			try {
				aclMessage.setContentObject(messageBean);
			} catch (IOException e) {
				log.error(e);
			}

			// Send this message object to the next agent
			myAgent.send(aclMessage);

			// Select where to store the stream based on input flag
			if (Declarations.storeToRedisFlag.equals("true")) {

				// Store the info in redis
				streamStorageSetup.pushToRedis(jedis, messageBean);
			}

			if (Declarations.storeToActiveMQFlag.equals("true")) {

				// Store the info in ActiveMQ
				streamStorageSetup.pushToActiveMQ(message, messageProducer,
						messageBean);
			}
		}

		else

			// For the time being just exiting the system
			System.exit(0);
	}
}
