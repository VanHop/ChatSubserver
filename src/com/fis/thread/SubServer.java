package com.fis.thread;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import com.fis.ddtp.SocketTransmitter;
import com.fis.ddtp.SocketTransmitter;
import com.fss.ddtp.DDTP;
import com.fss.thread.ManageableThread;
import com.fss.thread.ParameterType;
import com.fss.thread.ThreadConstant;
import com.fss.util.AppException;

public class SubServer extends ManageableThread{
	private SocketTransmitter channel;
	private Map<String, SocketTransmitter> listClient;
	private String subServerID;
	public int PORT;
	private int PORT_MAINSERVER;
	private String HOST;
	private ServerSocket serverSocket;
	private ClientManager threadServer;
	
	public SubServer(){
		listClient = new HashMap<String, SocketTransmitter>();
		Socket sck = null;

		channel = new SocketTransmitter(sck){
			public void close(){
				if(msckMain != null){
					logMonitor("mất kết nối tới MainServer");
					super.close();
				}
			}
		};
	}
	
	public int getPORT() {
		return PORT;
	}

	public void setPORT(int pORT) {
		PORT = pORT;
	}
	public void setParameterChannel(){
		channel.setPackage("com.fis.chat.server.thread.");
		channel.setIdSubServer(PORT);
		channel.mobjParent = this;
	}
	public void sendIdSubServer(){
		DDTP request = new DDTP();
		request.setString("idSubServer", PORT + "");
		try {
			channel.sendRequest("", "", request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void removeChannel(SocketTransmitter channel){
		listClient.remove(channel.getUserName());
	}
	public String getSubServerID() {
		return subServerID;
	}

	public SocketTransmitter getChannel() {
		return channel;
	}
	public void setChannel(SocketTransmitter channel) {
		this.channel = channel;
	}
	public void setSubServerID(String subServerID) {
		this.subServerID = subServerID;
	}

	public Map<String, SocketTransmitter> getListClient() {
		return listClient;
	}

	public SocketTransmitter getUser(String name){
		return listClient.get(name);
	}

	public void addClient(SocketTransmitter channel2) {
		listClient.put(channel2.getUserName(), channel2);
	}
	public void notifyUserConnected(SocketTransmitter channel) {

		try{
			if(channel.msckMain != null){
				logMonitor(channel.getUserName() + " đã đăng nhập");
				DDTP request = new DDTP();
				request.setString("username",channel.getUserName());
				request.setString("message",channel.getUserName() + ": đã online!");
				sendRequestToAll(request,"userConnected","MonitorProcessor");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public void sendRequestToAll(DDTP request,String strFunctionName,String strClassName) throws Exception{
		for(String username : listClient.keySet()){
			SocketTransmitter channelClient = listClient.get(username);
			if(channelClient.isOpen())
				channelClient.sendRequest(strClassName,strFunctionName,request);
		}
	}

	public SocketTransmitter findClient(String friend) {
		return listClient.get(friend);
	}
	public void notifyUserDisconnected(SocketTransmitter channel) {
		try{
			DDTP requestTo = new DDTP();
			logMonitor("Đã ngắt kết nối tới " + channel.getUserName());
			requestTo.setString("username",channel.getUserName());
			requestTo.setString("message",channel.getUserName() + " đã thoát!");
			for(String username : listClient.keySet()){
				if(username.equals(channel.getUserName()))
					continue;
				SocketTransmitter channelClient = listClient.get(username);
				if(channelClient.isOpen() && !channelClient.getUserName().equals(channel.getUserName()))
					channelClient.sendRequest("MonitorProcessor","userDisconnected",requestTo);
			}
		} catch(Exception e){
		}
	}

	public Vector getParameterDefinition() {
		Vector vtReturn = new Vector();
		vtReturn.addElement(createParameterDefinition("ServerPort", "", ParameterType.PARAM_TEXTBOX_MASK, "99999", "Server port for subserver"));
		vtReturn.addElement(createParameterDefinition("Host", "", ParameterType.PARAM_TEXTBOX_MASK, "localhost", "host main server"));
		vtReturn.addElement(createParameterDefinition("PortMainServer", "", ParameterType.PARAM_TEXTBOX_MASK, "99999", "Server port for mainserver"));
		vtReturn.addAll(super.getParameterDefinition());
		return vtReturn;
	}

	public void fillParameter() throws AppException {
		PORT = loadInteger("ServerPort");
		PORT_MAINSERVER = loadInteger("PortMainServer");
		HOST = loadMandatory("Host");
		super.fillParameter();
	}
	
	

	@Override
	protected void beforeSession() throws Exception {
		logMonitor("Before session");
		super.beforeSession();
		serverSocket = new ServerSocket(PORT);
		threadServer = new ClientManager(serverSocket,this);
		threadServer.start();
		logMonitor("Server start listening on "+PORT);
	}
	@Override
	protected void processSession() throws Exception {
		while(miThreadCommand != ThreadConstant.THREAD_STOP){
//			if(channel.msckMain != null && !channel.msckMain.isClosed()){
//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				continue;
//			}
//			
//			Socket sck = null;
//			try {
//				sck = new Socket(HOST,PORT_MAINSERVER);
//				sck.setSoLinger(true,0);
//				channel = new SocketTransmitter(sck){
//					public void close(){
//						if(msckMain != null){
//							logMonitor("mất kết nối tới MainServer");
//							super.close();
//						}
//					}
//				};
//				setParameterChannel();
//				channel.start();
//				sendIdSubServer();
//				logMonitor("đã kết nối với MainServer");
//				
//				Vector<String> listUser = new Vector<String>();
//				for(String username : listClient.keySet()){
//					listUser.add(username);
//				}
//				DDTP request = new DDTP();
//				request.setVector("listUser", listUser);
//				request.setString("IdSubServer", PORT + "");
//				channel.sendRequest("RequestProcessor", "receiveListUser", request);
//			}catch(Exception e){
//				e.printStackTrace();
//				continue;
//				
//			}
		}
		
	}

	@Override
	protected void afterSession() throws Exception {
		super.afterSession();
		for(String username : listClient.keySet())
			listClient.get(username).close();
		listClient.clear();
		serverSocket.close();
		channel.msckMain.close();
		channel.msckMain = null;
		channel.close();
	}
	@Override
	public void destroy(){
		super.destroy();
		for(int i = 0 ; i < listClient.size() ; i++){
			try{
				listClient.get(i).close();
				i--;
			}
			catch(Exception e){
				continue;
			}
		}
		listClient.clear();
		channel.close();
	}

	public void sendToClientListUser(SocketTransmitter channel2) {
		Vector<String> listUser = new Vector<String>();
		for(String username : listClient.keySet()){
			listUser.add(username);
		}
		DDTP request = new DDTP();
		request.setVector("listUser", listUser);
		try {
			channel2.sendRequest("MonitorProcessor", "updateListUser", request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
