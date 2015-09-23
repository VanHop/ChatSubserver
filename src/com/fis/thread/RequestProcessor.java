package com.fis.thread;

import java.util.Vector;


import com.fis.ddtp.SocketProcessor;
import com.fis.ddtp.SocketTransmitter;
import com.fss.ddtp.DDTP;

public class RequestProcessor extends SocketProcessor
{

	private SubServer subSer;

	public RequestProcessor() throws Exception{
	}

	public RequestProcessor(SubServer mgr) throws Exception
	{
		subSer = mgr;
	}

	public void setCaller(Object objCaller)
	{
		super.setCaller(objCaller);
		if(channel != null && channel.mobjParent instanceof SubServer)
		{
			subSer = (SubServer)channel.mobjParent;
		}
	}

	public void notifyUserConnected() {
		try{
			String username = request.getString("username");
			String message = request.getString("message");
			DDTP requestTo = new DDTP();
			requestTo.setString("username",username);
			requestTo.setString("message",message);
			sendRequestToAll(requestTo,"userConnected","MonitorProcessor");
		} catch(Exception e){
		}
	}
//	public void notifyUserDisconnected() {
//		try{
//			String username = request.getString("username");
//			String message = request.getString("message");
//			DDTP requestTo = new DDTP();
//			requestTo.setString("username",username);
//			requestTo.setString("message",message);
//			sendRequestToAll(requestTo,"userConnected","MonitorProcessor");
//		} catch(Exception e){
//		}
//	}
	public void sendRequestToAll(DDTP request,String strFunctionName,String strClassName) throws Exception
	{
		for(String username : subSer.getListClient().keySet()){
			SocketTransmitter channelClient = subSer.getListClient().get(username);
			if(channelClient != null && channelClient.isOpen())
				channelClient.sendRequest(strClassName,strFunctionName,request);
		}
	}
	public Vector<String> getAllUser(){
		Vector<String> allUser = new Vector<String>();
		for(String username : subSer.getListClient().keySet()){
			allUser.add(username);
		}
		return allUser;
	}
	public void notifyUserDisconnected(){
		try {
			sendRequestToAll(request, "userDisconnected", "MonitorProcessor");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void receiverRequestMakeGroup(){
		Vector<String> userInGroup =  request.getVector("listUserInGroup");
		for(String username : userInGroup){
			SocketTransmitter channelClient = subSer.findClient(username);
			if(channelClient != null)
				try {
					channelClient.sendRequest("MonitorProcessor", "makeGroup", request);
					((SubServer)channel.mobjParent).logMonitor(channelClient.getUserName() + " đã được thêm vào group " + request.getString("groupName"));
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
	public void receiverMessageIntoGroup(){
		Vector<String> userInGroup =  request.getVector("listUserInGroup");
		for(String username : userInGroup){
			SocketTransmitter channelClient = subSer.findClient(username);
			if(channelClient != null)
				try {
					channelClient.sendRequest("MonitorProcessor", "sendMessageIntoGroup", request);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
	@SuppressWarnings("unchecked")
	public void receiverRequestLogoutGroup(){
		Vector<String> userInGroup =  request.getVector("listUserInGroup");
		for(String username : userInGroup){
			SocketTransmitter channelClient = subSer.findClient(username);
			if(channelClient != null)
				try {
					channelClient.sendRequest("MonitorProcessor", "logoutGroup", request);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
	@SuppressWarnings("unchecked")
	public void receiverRequestUpdateGroup(){
		Vector<String> userInGroup =  request.getVector("allUserInRequest");
		for(String username : userInGroup){
			SocketTransmitter channelClient = subSer.findClient(username);
			if(channelClient != null)
				try {
					channelClient.sendRequest("MonitorProcessor", "updateGroup", request);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
	public void notifyListUserOnline(){
		Vector<String> listUser = request.getVector("listUser");
		for(String username : subSer.getListClient().keySet()){
			SocketTransmitter channelClient = subSer.getListClient().get(username);
			try{
				DDTP requestTo = new DDTP();
				requestTo.setVector("listUser",listUser);
				channelClient.sendRequest("MonitorProcessor", "updateListUser", requestTo);
			} catch(Exception e){
			}
		}
	}
	public void listUserOnline(){
		Vector<String> listUser = request.getVector("listUser");
		String username = request.getString("username");
		SocketTransmitter channelClient = subSer.getListClient().get(username);
		
		DDTP request = new DDTP();
		request.setVector("listUser", listUser);
		try {
			channelClient.sendRequest("MonitorProcessor", "updateListUser", request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void getAllUserOnline(){
		Vector<String> listUser = request.getVector("listUser");
	}
}
