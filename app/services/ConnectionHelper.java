package services;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.google.inject.Inject;
import com.google.inject.Singleton;
/**
 * 
 * @author nasser
 *
 */

public class ConnectionHelper {

	 private ConnectionProvider pro;
	
	 @Inject
	public ConnectionHelper(ConnectionProvider pro){
		 this.pro=pro;
	}
	
	public  DSLContext connection(){
		return DSL.using(pro.acquire(),SQLDialect.POSTGRES_9_5);
	}
}
