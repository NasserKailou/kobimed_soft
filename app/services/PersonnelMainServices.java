package services;

import java.util.List;

import com.google.inject.Inject;

import models.public_.Tables;
import models.public_.tables.daos.PersonnelsDao;
import models.public_.tables.pojos.Personnels;

/**
 * 
 * @author nasser
 *
 */
public class PersonnelMainServices extends PersonnelsDao {

	private final ConnectionHelper con;

	@Inject
	public PersonnelMainServices(ConnectionHelper con) {
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	public String saveLogical(Personnels personnels, boolean b) {
		try {
			if (b)
				super.insert(personnels);
			else
				super.update(personnels);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public List<Personnels> findAll() {
		List<Personnels> p = con.connection().selectFrom(Tables.PERSONNELS).fetchInto(Personnels.class);
		con.connection().close();
		return p;// super.findAll();
	}

	public Personnels findById(Long id) {
		return super.findById(id);
	}

	public List<Personnels> listes(String categorie) {
		List<Personnels> p = con.connection().selectFrom(Tables.PERSONNELS)
				.where(Tables.PERSONNELS.IS_DELETED.eq(false)).fetchInto(Personnels.class);
		// System.out.println("la liste des chauffeur est : "+ p);
		con.connection().close();
		return p;// super.fetchByCategorie(categorie);
	}

	/**
	 * renvoie une liste d'objects de type Personnels dont le Nom commence par query
	 * 
	 * @param query
	 * @return List<Personnels> (pojos)
	 */
	public List<Personnels> findLikeName(String query) {
		String lnom = null == query ? "" : query;
		lnom = lnom.endsWith("%") ? lnom : lnom + "%";
		return con.connection().selectFrom(Tables.PERSONNELS).where(Tables.PERSONNELS.CATEGORIE.eq("Médecin"))
				.and(Tables.PERSONNELS.NOM_P.like(lnom)).fetchInto(Personnels.class);
	}
}
