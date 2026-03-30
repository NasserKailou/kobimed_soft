package services.stock;

import java.util.List;

import com.google.inject.Inject;

import models.stock.tables.daos.ServicesDao;
import models.stock.tables.pojos.Services;
import services.ConnectionHelper;

/**
 * 
 * @author nasser
 *
 */
public class ServicesServicesImp extends ServicesDao {

	 private final ConnectionHelper con;

	    @Inject
	    public ServicesServicesImp(ConnectionHelper con) {
	        this.con = con;
	        this.setConfiguration(con.connection().configuration());
	    }

	    
	    public String saveLogical(Services service, boolean b) {
	        try {
	            if(b)
	                super.insert(service);
	            else
	                super.update(service);
	            return "ok";
	        } catch (Exception e) {
	            return e.getMessage();
	        }
	    }

	    
	    public Services getById(int id) {
	        return super.findById(id);
	    }

	  
	    public List<Services> getAll() {
	        return super.findAll();
	    }

	    public boolean isServiceExist(String libelle) {
	        List<Services> service=super.fetchByNomS(libelle);
	        return service.size() > 0;
	    }
}
