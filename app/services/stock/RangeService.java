package services.stock;

import com.google.inject.Inject;
import models.stock.tables.daos.RangeeDao;
import models.stock.tables.pojos.Rangee;
import services.ConnectionHelper;

import java.util.List;

/**
 * 
 * @author nasser
 *
 */
public class RangeService extends RangeeDao implements Irange {
    private final ConnectionHelper con;

    @Inject
    public RangeService(ConnectionHelper con) {
        this.con = con;
        this.setConfiguration(con.connection().configuration());
    }

    @Override
    public String saveLogical(Rangee rangee, boolean b) {
        try {
            if(b)
                super.insert(rangee);
            else
                super.update(rangee);
            return "ok";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public Rangee getById(Long id) {
        return super.findById(id);
    }

    @Override
    public List<Rangee> getAll() {
        return super.findAll();
    }

    @Override
    public boolean isRangeExist(String libelle) {
        List<Rangee> rangees=super.fetchByLibelle(libelle);
        return rangees.size() > 0;
    }
}
