package com.musigma.ird.setup;

import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

/*****************************************************************************
 * THIS CLASS IS FOR INITIALIZING AND CONTROLLING THE RTSP STREAM. IT PROVIDES
 * FUNCTIONS FOR CONNECTING TO THE RTSP SOURCE, PARSING THE STREAM AND FINALLY
 * STOPPING THE CLIENT. | AUTHOR : ARJUN ASSI
 *****************************************************************************/

public class RTSPHandler {

	/*******************
	 * CLASS VARIABLES *
	 *******************/

	// This represents the rtsp stream source. eg: rtsp://ip:port/
	private static String sourceURI;

	// Videocapture object provided by openCV. Used to open the rtsp stream
	private VideoCapture videoCapture;

	// Mat object provided by openCV. Stores the images as a frame
	private Mat mat;

	// MatOfByte object provided by openCV. Stores the frames as a byte
	// array
	private MatOfByte matOfByte;

	// To store the image as a byte array
	private byte[] byteArray;

	// Logger object
	private static org.apache.log4j.Logger log = Logger
			.getLogger(RTSPHandler.class.getName());

	/*****************
	 * CLASS METHODS *
	 *****************/

	/***********************************************************************
	 * THIS FUNCTION LOADS THE NATIVE LIBRARY WHICH ALLOWS FOR ACCESSING THE
	 * OPENCV RELATED FUNCTIONS AND OBJECTS
	 ***********************************************************************/
	public void loadNativeLibrary() {

		// Load the native library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	/*************************************************************************
	 * THIS FUNCTION CONNECTS TO THE RTSP STREAM SOURCE AND OPENS A CONNECTION
	 * VIA OPENCV VIDEOCAPTURE OBJECT. IT LOGS AN ERROR MESSAGE IF IT IS NOT
	 * ABLE TO CONNECT TO A RTSP STREAM
	 *************************************************************************/
	public void connectToRTSPSource(String sourceURI) {

		// Create a videocapture object
		videoCapture = new VideoCapture();

		// Open the rtsp stream
		videoCapture.open(sourceURI);

		// Initialize the Mat object
		mat = new Mat();
		matOfByte = new MatOfByte();

		// Log the outcome
		if (videoCapture.isOpened())
			log.info("Connected to the source stream : " + sourceURI);
		else
			log.error("Could not open the source stream : " + sourceURI);
	}

	/*************************************************************************
	 * THIS FUNCTION PARSES THE INPUT STREAM BY EXTRACTING THE NEXT FRAME FROM
	 * THE RTSP STREAM. IT THEN CONVERTS THE FRAME INTO A BYTE ARRAY AND THEN
	 * RETURNS THE OBJECT. IF IT DOES NOT FIND A FRAME, IT RETURNS NULL
	 *************************************************************************/
	public byte[] parseStream() {

		// Variable to check whether the input stream has got any frames
		boolean hasFrame;

		// Read the next frame from the stream
		hasFrame = videoCapture.read(mat);

		// If the frame is not present return null
		if (!hasFrame) {
			// Log the end of frame as a warning
			log.warn("End of stream reached");
			return null;
		}

		// Convert the frame into a byte array
		Highgui.imencode(".jpg", mat, matOfByte);
		byteArray = matOfByte.toArray();

		// Return the byte array object
		return byteArray;
	}

	/*********************************************************************
	 * THIS FUNCTION STOPS THE CLIENT FROM RECEIVING ANY MORE FRAMES. THE
	 * CONNECTION TO THE SOURCE IS STILL OPEN THOUGH
	 *********************************************************************/
	public void stopClient() {

		// Release the connection to the stream
		videoCapture.release();
		log.info("The client is stopped and is not receiving frames from the source stream : "
				+ sourceURI);
	}
}
