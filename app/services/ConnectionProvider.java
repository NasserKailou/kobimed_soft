/**
 * 
 */
package services;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jooq.exception.DataAccessException;
/**
 * 
 * @author nasser
 *
 */
@Singleton
public class ConnectionProvider implements org.jooq.ConnectionProvider{
	
	public Connection connection=null;
	private final DataSources dataSource;
	
	@Inject
	public ConnectionProvider(DataSources dataSources) {
		this.dataSource=dataSources;
	}
	

	@Override
	public Connection acquire() throws DataAccessException {
		if (connection==null) {
			try {
				connection=dataSource.provideDataSource().getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return connection;
	}

	@Override
	public void release(Connection released) throws DataAccessException {
		if (this.connection != released) {
            throw new IllegalArgumentException("Expected " + this.connection + " but got " + released);
            
        }
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Error closing connection " + connection + e);
        }
    }

}
