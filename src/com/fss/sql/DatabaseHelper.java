package com.fss.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseHelper {
	public static Connection con = null;
	public static PreparedStatement ps = null;
	public static boolean checkUser(String username, String pass) {
//		con = DataConnect.getConnectOracleSQL();
//
//		try {
//			int password = Integer.parseInt(pass);
//			ps = con.prepareStatement("select * from username where username = '" + username + "' and password="
//					+ password + " and status = 0");
//			ResultSet rs = ps.executeQuery();
//			if(rs.next()){
//				ps.executeUpdate("update username set status = 1 where username = '" + username + "'");
//				return true;
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//		return false;
		return true;
	}

	
	public static boolean logout(String username){
//		con = DataConnect.getConnectOracleSQL();
//
//		try {
//
//			ps = con.prepareStatement("update username set status = 0 where username = '" + username + "'");
//			ps.executeQuery();
//			return true;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
		return true;
	}
}
