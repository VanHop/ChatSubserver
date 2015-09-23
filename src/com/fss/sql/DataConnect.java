package com.fss.sql;

import java.sql.Connection;
import java.sql.DriverManager;

public class DataConnect {

	private final static String USERNAME = "scott";
	private final static String PASSWORD = "12345";
	private final static String SID = "dbTest";
	public static Connection getConnectOracleSQL(){
		
		 try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:" + SID, USERNAME, PASSWORD);
			return con;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		 
	}
}
