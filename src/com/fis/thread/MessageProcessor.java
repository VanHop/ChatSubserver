package com.fis.thread;

import java.net.InetAddress;
import java.util.Vector;


import com.fis.ddtp.SocketTransmitter;
import com.fis.ddtp.SocketProcessor;
import com.fis.ddtp.SocketTransmitter;
import com.fss.ddtp.DDTP;
import com.fss.sql.DatabaseHelper;

public class MessageProcessor extends SocketProcessor{

	private SubServer svMng;
	
	public MessageProcessor(){}

	public MessageProcessor(SubServer svMng2) {
		svMng = svMng2;
	}

	public void sendFile(){
		try {
			String friend = request.getString("friend");
			SocketTransmitter channelClient = svMng.findClient(friend);
			//gui client duoc gui tin nhan
			if(channelClient != null){
				channelClient.sendRequest("MonitorProcessor", "receiveFile", request);
				svMng.logMonitor(channel.getUserName() + " đã gửi 1 file cho " + friend);
			}
			else{
				request.setString("idSubServer", svMng.getChannel().getIdSubServer() + "");
				if(svMng.getChannel().msckMain != null)
					svMng.getChannel().sendRequest("RequestProcessor", "sendFile", request);
				else	svMng.logMonitor("Không thể gửi file cho " + friend);
			}
		} catch (Exception e) {
			svMng.logMonitor("Không thể gửi file cho " + request.getString("friend"));
			e.printStackTrace();
		}
	}
	public void sendMessage(){
		try {
			boolean flag = false;
			DDTP requestToClient = new DDTP();
			//gui lai client da gui tin nhan
			String message = request.getString("message");
			String me = request.getString("me");
			String friend = request.getString("friend");
			
			requestToClient.setString("me", friend);
			requestToClient.setString("message", message);
			requestToClient.setString("friend", me);
			requestToClient.setString("logTime", request.getString("logTime"));
			//tim nguoi can gui
			SocketTransmitter channelClient = svMng.findClient(friend);
			//gui client duoc gui tin nhan
			if(channelClient != null){
				channelClient.sendRequest("MonitorProcessor", "receiveMessage", requestToClient);
//				System.out.println(message + "," + (System.currentTimeMillis() - Long.parseLong(request.getString("logTime"))));
				//svMng.logMonitor(me + " đã gửi 1 tin nhắn cho " + friend);
			}
			else{
				requestToClient.setString("idSubServer", svMng.getChannel().getIdSubServer() + "");
				if(svMng.getChannel().msckMain != null){
					requestToClient.setRequestID(System.currentTimeMillis()+"");
					DDTP response = svMng.getChannel().sendRequest("RequestProcessor", "sendMessage", requestToClient);
					if((String)response.getReturn() != null){
						svMng.logMonitor(me + " đã gửi 1 tin nhắn cho " + friend);
					} else {
						svMng.logMonitor("Không thể gửi tin nhắn cho " + request.getString("friend"));
						flag = true;
					}
				}
				else{
					svMng.logMonitor("Không thể gửi tin nhắn cho " + request.getString("friend"));
					flag = true;
				}
			}
			if(flag){
				requestToClient.clear();
				requestToClient.setString("me", me);
				requestToClient.setString("message", "Không thể gửi tin nhắn cho " + friend);
				requestToClient.setString("friend", friend);
				requestToClient.setString("logTime", request.getString("logTime"));
				channel.sendRequest("MonitorProcessor", "receiveMessage", requestToClient);
			} else {
				requestToClient.clear();
				requestToClient.setString("me", me);
				requestToClient.setString("message", message);
				requestToClient.setString("friend", friend);
				requestToClient.setString("logTime", request.getString("logTime"));
				channel.sendRequest("MonitorProcessor", "receiveMessage", requestToClient);
			}
			
		} catch (Exception e) {
			svMng.logMonitor("Không thể gửi tin nhắn cho " + request.getString("friend"));
			e.printStackTrace();
		}
	}
	public String receiverMessageServer(){
		String me = request.getString("me");
		SocketTransmitter channelClient = svMng.getListClient().get(me);
		try {
			if(channelClient != null && channelClient.isOpen()){
				channelClient.sendRequest("MonitorProcessor", "receiveMessage", request);
				svMng.logMonitor(me + " đã nhận được tin nhắn của " + request.getString("friend"));
				return "OK";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	public void receiverFileServer(){
		String me = request.getString("friend");
		SocketTransmitter channelClient = svMng.getListClient().get(me);
		try {
			if(channelClient != null && channelClient.isOpen()){
				channelClient.sendRequest("MonitorProcessor", "receiveFile", request);
				svMng.logMonitor(me + " đã nhận được 1 file của " + request.getString("me"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	public void makeGroup(){

		try {
			Vector<String> userInGroup =  request.getVector("listUserInGroup");
			for(String username : userInGroup){
				SocketTransmitter channelClient = svMng.findClient(username);
				if(channelClient != null)
					channelClient.sendRequest("MonitorProcessor", "makeGroup", request);
			}
			svMng.logMonitor(channel.getUserName() + " đã tạo group " + request.getString("groupName"));
			for(String username : userInGroup){
				SocketTransmitter channelClient = svMng.findClient(username);
				if(channelClient == null){
					request.setString("idSubServer", svMng.getChannel().getIdSubServer() + "");
					if(svMng.getChannel().msckMain != null){
						svMng.getChannel().sendRequest("RequestProcessor", "makeGroup", request);
						break;
					} else {
						svMng.logMonitor(channel.getUserName() + " không thể gửi yêu cầu tạo group " + request.getString("groupName") + " cho " + username);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void setCaller(Object objCaller) {
		super.setCaller(objCaller);
		svMng = (SubServer) channel.mobjParent;
	}
	@SuppressWarnings("unchecked")
	public void sendMessageIntoGroup(){
		try {
			Vector<String> userInGroup =  request.getVector("listUserInGroup");
			for(String username : userInGroup){
				SocketTransmitter channelClient = svMng.findClient(username);
				if(channelClient != null)
					channelClient.sendRequest("MonitorProcessor", "sendMessageIntoGroup", request);
			}
			svMng.logMonitor(channel.getUserName() + " đã gửi 1 tin nhắn cho group " + request.getString("groupName"));
			for(String username : userInGroup){
				SocketTransmitter channelClient = svMng.findClient(username);
				if(channelClient == null){
					request.setString("idSubServer", svMng.getChannel().getIdSubServer() + "");
					if(svMng.getChannel().msckMain != null){
						svMng.getChannel().sendRequest("RequestProcessor", "sendMessageIntoGroup", request);
						break;
					} else {
						svMng.logMonitor("Không thể gửi tin nhắn cho " + username + " trong group " + request.getString("groupName"));
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	public void updateGroup(){
		try {
			Vector<String> userInGroup =  request.getVector("listUserInGroup");
			Vector<String> userIntoGroup =  request.getVector("listUserIntoGroup");
			Vector<String> allUserInRequest = new Vector<String>();
			for(String username : userInGroup)
				allUserInRequest.add(username);
			for(String username : userIntoGroup){
				if(!allUserInRequest.contains(username))
					allUserInRequest.add(username);
			}
			for(String username : allUserInRequest){
				SocketTransmitter channelClient = svMng.findClient(username);
				if(channelClient != null){
					channelClient.sendRequest("MonitorProcessor", "updateGroup", request);
				}
			}
			svMng.logMonitor(channel.getUserName() + " đã cập nhật lại group " + request.getString("groupName"));
			for(String username : allUserInRequest){
				SocketTransmitter channelClient = svMng.findClient(username);
				if(channelClient == null){
					request.setString("idSubServer", svMng.getChannel().getIdSubServer() + "");
					request.setVector("allUserInRequest", allUserInRequest);
					if(svMng.getChannel().msckMain != null){
						svMng.getChannel().sendRequest("RequestProcessor", "updateGroup", request);
						break;
					} else {
						svMng.logMonitor(channel.getUserName() + " không thể cập nhật lại group " + request.getString("groupName") + " cho " + username);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void logoutGroup(){
		try{
			Vector<String> userInGroup = request.getVector("listUserInGroup");
			for(String username : userInGroup){
				SocketTransmitter channelClient = svMng.findClient(username);
				if(channelClient != null)
					channelClient.sendRequest("MonitorProcessor", "logoutGroup", request);
			}
			svMng.logMonitor(request.getString("username") + " đã thoát khỏi group " + request.getString("group"));
			for(String username : userInGroup){
				SocketTransmitter channelClient = svMng.findClient(username);
				if(channelClient == null){
					request.setString("idSubServer", svMng.getChannel().getIdSubServer() + "");
					if(svMng.getChannel().msckMain != null){
						svMng.getChannel().sendRequest("RequestProcessor", "logoutGroup", request);
						break;
					} else {
						svMng.logMonitor("Không thể gửi thông báo " + request.getString("username") + " thoát khỏi group " + request.getString("group") + " cho " + username);
					}
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public boolean login(String strUserName,String strPassword,InetAddress address){
		
		return DatabaseHelper.checkUser(strUserName, strPassword);
		
	}
}
