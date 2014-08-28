package com.musigma.ird.message;

import java.awt.image.BufferedImage;
import jade.util.leap.Serializable;

/******************************************************************************
 * THIS CLASS IS FOR STORING THE MESSAGE STRUCTURE THAT IS GOING TO BE SENT TO
 * THE RECEIVER AGENT. IT HAS THE TIME STAMOP AND THE FRAME AS A BUFFERED IMAGE
 * OBJECT. AUTHOR : ARJUN ASSI
 ******************************************************************************/

public class MessageBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*******************
	 * CLASS VARIABLES *
	 *******************/

	// Time stamp of the arrival of the frame
	private Long timeStamp;

	// Decoded image stored as a buffered image object
	private BufferedImage image;

	/*****************
	 * CLASS METHODS *
	 *****************/

	/*****************************
	 * GETTER AND SETTER FUNCTIONS
	 *****************************/

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

}
