package com.fis.thread;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import com.fss.ddtp.DDTP;
import com.fis.ddtp.SocketTransmitter;
import com.fss.sql.DatabaseHelper;
import com.fss.util.AppException;

/**
 * <p>Title: Listen server socket</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT-BU5</p>
 * @author Dang Dinh Trung
 * @version 1.0
 */

public class ClientManager implements Runnable
{
	protected ServerSocket serverSocket;
	protected Thread mthrMain = null;
	SubServer svMng;
	public ClientManager(){
		super();
	}

	public void start()
	{
		if(mthrMain != null)
			mthrMain.stop();
		mthrMain = new Thread(this);
		mthrMain.start();
	}

	public ClientManager(ServerSocket serverSocket, SubServer subServer) {
		this.serverSocket =serverSocket;
		svMng = subServer;
	}



	public void run() {
		try
		{
			while(!serverSocket.isClosed())
			{
				Socket sck = null;
				String strUserName = "";
				String strPassword = "";
				String strRequestID = "";

				try
				{
					sck = serverSocket.accept();
					sck.setSoLinger(true,0);
					sck.setKeepAlive(true);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					continue;
				}

				try
				{
					DDTP request = new DDTP(sck.getInputStream());
					strUserName = request.getString("UserName");
					strPassword = request.getString("Password");
					strRequestID = request.getRequestID();

					// Login to system
					if(!request.getFunctionName().equals("login"))
						throw new AppException("FSS-00020","ThreadServer.run");

					// Check username, password
					MessageProcessor processor = new MessageProcessor(svMng);
					boolean vtLogin = processor.login(strUserName,strPassword,sck.getInetAddress());
					if(!vtLogin)
						throw new Exception();
					SocketTransmitter channel = new SocketTransmitter(sck)
					{
						public void close()
						{
							if(msckMain != null)
							{
								try{
									DatabaseHelper.logout(this.getUserName());
									svMng.notifyUserDisconnected(this);
									DDTP request = new DDTP();
									request.setString("username", this.getUserName());
									request.setString("idSubServer", this.getIdSubServer() + "");
									request.setString("message",this.getUserName() + " đã thoát");
									svMng.getChannel().sendRequest("RequestProcessor", "notifyUserDisconnected", request);
									svMng.getListClient().remove(this.getUserName());
									super.close();
									this.close();
								} catch(Exception e){
									super.close();
								}
								
							}
						}
					};
					channel.mobjParent=svMng;
					channel.setUserName(strUserName);
					channel.setIdSubServer(svMng.PORT);
					channel.setPackage("com.fss.monitor.");
					channel.start();
					svMng.addClient(channel);
					svMng.notifyUserConnected(channel);
					if(svMng.getChannel().msckMain != null && !svMng.getChannel().msckMain.isClosed()){
						DDTP requestTo = new DDTP();
						requestTo.setString("username", channel.getUserName());
						requestTo.setString("subServer", channel.getIdSubServer() + "");
						svMng.getChannel().sendRequest("RequestProcessor", "notifyUserConnected", requestTo);
					} else {
						svMng.sendToClientListUser(channel);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		finally
		{
//			try
//			{
//				serverSocket.close();
//			}
//			catch(Exception e)
//			{
//			}
		}
		
	}
}
