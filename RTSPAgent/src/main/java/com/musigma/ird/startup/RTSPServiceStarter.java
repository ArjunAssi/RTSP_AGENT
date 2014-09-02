package com.musigma.ird.startup;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.apache.log4j.Logger;
import com.musigma.ird.agent.RTSPAgent;
import com.musigma.ird.setup.ConfigSetup;

/*******************************************************************************
 * THIS IS MAIN STARTER CLASS. IT IS RESPONSIBLE FOR INITIALIZING THE JADE MAIN
 * CONTAINER AND ALSO CREATING AND STOPPING THE RTSP AGENT. IT TAKES THE
 * HOSTNAME AND THE IP OF THE MACHINE WHERE TO LAUNCH THE MAIN CONTAINER AND THE
 * RTSP AGENT. | AUTHOR : ARJUN ASSI
 *******************************************************************************/

public class RTSPServiceStarter {

	/*******************
	 * CLASS VARIABLES *
	 *******************/

	// Ip and port of the machine to lauch the rtsp agent
	private String HOST_NAME;
	private String PORT;

	// Jade profile variable to lauch the conatiner on the above mentioned ip
	// and port
	private static Profile profile;

	// Handle for main container
	private static ContainerController mainContainer;

	// Jade runtine instance
	private static jade.core.Runtime runtime;

	// Agent Controller for controlling the agent
	private static AgentController agentController;

	// Logger
	private static org.apache.log4j.Logger log = Logger
			.getLogger(RTSPServiceStarter.class.getName());

	/*****************
	 * CLASS METHODS *
	 *****************/

	/**********************************************************************
	 * THIS FUNCTION IS THE CONSTRUCTOR TO INITIALIZE THE HOST AND PORT FOR
	 * LAUNCHING THE RTSP AGENT
	 **********************************************************************/
	public RTSPServiceStarter(String HOST_NAME, String PORT) {

		this.HOST_NAME = HOST_NAME;
		this.PORT = PORT;
	}

	/*********************************************************************
	 * THIS FUNCTION SETS UP THE MAIN CONTAINER AND RETURNS THE CONTAINER
	 * CONTROLLER OBJECT FOR THE MAIN CONTAINER
	 *********************************************************************/
	public ContainerController mainContainerSetup() {

		// Create new profile based on the ip and port
		runtime = Runtime.instance();
		profile = new ProfileImpl();

		// Set the ip and port
		profile.setParameter(Profile.MAIN_HOST, HOST_NAME);
		profile.setParameter(Profile.MAIN_PORT, PORT);

		// Create and return the main container
		mainContainer = runtime.createMainContainer(profile);

		// Log the outcome
		log.info("Main Container started on machine : " + HOST_NAME + " : "
				+ PORT);

		// Return the container controller
		return mainContainer;
	}

	/*************************************************************************
	 * THIS FUNCTION CREATES THE RTSP AGENT AND THE RETURNS THE CONTROLLER FOR
	 * THE AGENT
	 *************************************************************************/
	public AgentController RTSPAgentStartup(ContainerController mainContainer) {

		// Create and start the RTSP AGENT
		try {
			agentController = mainContainer.createNewAgent("RTSPAGENT",
					RTSPAgent.class.getName(), null);
			agentController.start();
		} catch (StaleProxyException e) {
			log.error(e);
		}

		// Log the outcome
		log.info("RTSPAGENT started");

		// Return the controller for the RTSP AGENT
		return agentController;
	}

	/************************************
	 * THIS FUNCTION KILLS THE RTSP AGENT
	 ************************************/
	public void RTSPAgentkill(AgentController agentController) {

		// Kill the agent
		try {
			agentController.kill();

			// Log the outcome
			log.info("RTSPAGENT killed");
		} catch (StaleProxyException e) {
			log.error(e);
		}
	}

	/****************
	 * MAIN FUNCTION
	 ****************/
	@SuppressWarnings("unused")
	public static void main(String[] args) {

		// Load the properties file
		ConfigSetup.loadProperty();

		// Create Service starter object - arg[0] is hostname , args[1] is port
		RTSPServiceStarter rtspServiceStarter = new RTSPServiceStarter(args[0],
				args[1]);

		// Setup the main container
		ContainerController containerController = rtspServiceStarter
				.mainContainerSetup();

		// Set up and start the RTSP Agent
		AgentController agentController = rtspServiceStarter
				.RTSPAgentStartup(containerController);
	}
}
