package com.musigma.ird.message;

import jade.util.leap.Serializable;

/******************************************************************************
 * THIS CLASS IS FOR STORING THE MESSAGE STRUCTURE THAT IS GOING TO BE SENT TO
 * THE RECEIVER AGENT. IT HAS THE TIME STAMP AND THE FRAME AS A BYTE ARRAY
 * OBJECT. | AUTHOR : ARJUN ASSI
 ******************************************************************************/

public class MessageBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*******************
	 * CLASS VARIABLES *
	 *******************/

	// Time stamp of the arrival of the frame
	private Long timeStamp;

	// Decoded image stored as a byte array object
	private byte[] byteArray;

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

	public byte[] getByteArray() {
		return byteArray;
	}

	public void setByteArray(byte[] byteArray) {
		this.byteArray = byteArray;
	}

}
