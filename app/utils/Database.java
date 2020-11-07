package utils;

import java.sql.Connection;

import java.sql.DriverManager;

import services.DataSources;

/**
 * 
 * @author nasser
 *
 */
public class Database {
	
	public Connection getConnection() {
		try {
			Class.forName(DataSources.DB_DRIVER);
			Connection con = DriverManager.getConnection(DataSources.DB_URL, DataSources.DB_USER_NAME,
					DataSources.DB_PASSWORD);
			return con;
		} catch (Exception ex) {
			System.out.println("Database.getConnection() Error -->" + ex.getMessage());
			return null;
		}
	}

	public static void close(Connection con) {
		try {
			con.close();
		} catch (Exception ex) {
		}
	}
}