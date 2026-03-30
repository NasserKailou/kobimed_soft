package services.stock;

import models.stock.tables.pojos.Rangee;

import java.util.List;

/**
 * 
 * @author nasser
 *
 */
public interface Irange {
    public String saveLogical(Rangee rangee, boolean b);
    public Rangee getById(Long id);
    public List<Rangee> getAll();
    public boolean isRangeExist(String libelle);
}
