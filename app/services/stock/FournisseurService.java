package services.stock;

import com.google.inject.Inject;
import models.stock.tables.daos.FournisseurDao;
import models.stock.tables.pojos.Fournisseur;
import services.ConnectionHelper;

import java.util.List;

public class FournisseurService extends FournisseurDao{
    private final ConnectionHelper con;

    @Inject
    public FournisseurService(ConnectionHelper con) {
        this.con = con;
        this.setConfiguration(con.connection().configuration());
    }

    
    public String saveLogical(Fournisseur Fournisseur, boolean b) {
        try {
            if(b)
                super.insert(Fournisseur);
            else
                super.update(Fournisseur);
            return "ok";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    
    public Fournisseur getById(int id) {
        return super.findById(id);
    }

  
    public List<Fournisseur> getAll() {
        return super.findAll();
    }

    public boolean isFournisseurExist(String libelle) {
        List<Fournisseur> Fournisseurs=super.fetchByNomF(libelle);
        return Fournisseurs.size() > 0;
    }
}