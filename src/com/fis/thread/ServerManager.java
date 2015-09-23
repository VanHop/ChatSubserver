package com.fis.thread;

import com.fss.thread.ProcessorListener;
import com.fss.thread.ThreadManager;


public class ServerManager extends ThreadManager{

	public ServerManager(int port, ProcessorListener lsn) throws Exception {
		super(port, lsn);
	}
	public boolean isOpen() {
		if(serverSocket.isClosed())
			return false;
		return true;
	}


}
