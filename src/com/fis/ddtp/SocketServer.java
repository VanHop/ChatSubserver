package com.fis.ddtp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fis.util.LinkQueue;
import com.fss.ddtp.DDTP;
import com.fss.ddtp.Processor;
import com.fss.util.AppException;

/**
 * <p>Title: A Thread which listen from request queue of SocketTransmitterThread</p>
 * <p>Description:
 *    -   Always read from request queue of SocketTransmitterThread
 *    -   After that, it will call a class to handle request
 * </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT-BU5</p>
 * @author Dang Dinh Trung
 * @version 2.0
 */

public class SocketServer implements Runnable
{
	private Thread mthrMain;
	private SocketTransmitter channel = null;
	private LinkQueue<DDTP> mlqRequestVsip = null;
	//private Map<String, Method> cacheMethods;
	//private Map<String, Object> cacheObjects;
	//ExecutorService executor;

	Long start;
	///////////////////////////////////////////////////////////
	/**
	 * Create processorthread for transmitter thread
	 * @param socketTransmitter transmitter thread
	 */
	///////////////////////////////////////////////////////////
	public SocketServer(com.fis.ddtp.SocketTransmitter socketTransmitter)
	{
		this.channel = socketTransmitter;
		mlqRequestVsip = socketTransmitter.mlqRequestVsip;
		//executor = Executors.newFixedThreadPool(5);
		//cacheMethods = new HashMap<String,Method>();
		//cacheObjects = new HashMap<String, Object>();
	}
	///////////////////////////////////////////////////////////
	/**
	 * Start processor thread
	 */
	///////////////////////////////////////////////////////////
	public void start()
	{
		if(mthrMain != null)
			mthrMain.stop();
		mthrMain = new Thread(this);
		mthrMain.start();
	}
	///////////////////////////////////////////////////////////
	/**
	 * always listen from request queue and process request
	 * @author
	 * - Thai Hoang Hiep
	 * - Dang Dinh Trung
	 */
	///////////////////////////////////////////////////////////
	public void run()
	{
		while(isConnected())
		{
			// Get request from queue
			DDTP request = getFirstRequest();
			DDTP response = null;
			try
			{
				// Process request
				if(request == null)
					continue;
				response = Processor.processRequest(channel,request);
			}
			catch(Exception e)
			{
				response = new DDTP();
				if(e instanceof AppException)
					response.setException((AppException)e);
				else
					response.setException(new AppException(e.getMessage(),"SocketServer.run",""));
				e.printStackTrace();
			}
			finally
			{
				try
				{
					// Return response
					if(request != null)
					{
						String strRequestID = request.getRequestID();
						if(strRequestID.length() > 0 && channel != null)
						{
							response.setResponseID(strRequestID);
							channel.sendResponse(response);
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	///////////////////////////////////////////////////////////
	/**
	 * Check connection
	 * @return true if connected, otherwise false
	 * @author HiepTH
	 */
	///////////////////////////////////////////////////////////
	public boolean isConnected()
	{
		return (channel != null && channel.isOpen());
	}
	////////////////////////////////////////////////////////////
	/**
	 * gets DDTP request from request queue and removes it
	 * @param iIndex request index
	 * @return request data
	 * Author: TrungDD
	 */
	////////////////////////////////////////////////////////////
	public DDTP getFirstRequest()
	{
		if(mlqRequestVsip==null)
			return null;

		DDTP ddtpReturn = (DDTP)mlqRequestVsip.dequeueWait(30);
		return ddtpReturn;
	}
}
