
package services;

import java.util.List;

import javax.inject.Inject;

import models.public_.Tables;
import models.public_.Tables.*;
import models.public_.tables.daos.BasMedicamentsDao;
import models.public_.tables.pojos.BasMedicaments;

/**
 * 
 * @author nasser
 *
 */
public class BaseMedicamentMainServices extends  BasMedicamentsDao{

	private final ConnectionHelper con;

	@Inject
	public BaseMedicamentMainServices(ConnectionHelper con) {
		super();
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	public List<BasMedicaments> findLikeName(String query) {
		String lnom = null == query ? "" : query;
		lnom = lnom.endsWith("%") ? lnom : lnom + "%";
		return con.connection().selectFrom(Tables.BAS_MEDICAMENTS).where(Tables.BAS_MEDICAMENTS.LIBELLE.like(lnom))
				.fetchInto(BasMedicaments.class);
	}

	public String saveLogical(BasMedicaments med, boolean b) {
		try {
			if (b)
				super.insert(med);
			else
				super.update(med);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public List<BasMedicaments> findAll() {
		List<BasMedicaments> c = con.connection().selectFrom(Tables.BAS_MEDICAMENTS)
				.where(Tables.BAS_MEDICAMENTS.IS_DELETED.isFalse()).fetchInto(BasMedicaments.class);
		con.connection().close();
		return c;
	}

	public List<BasMedicaments> listeBaseMedicaments() {
		List<BasMedicaments> c = con.connection().selectFrom(Tables.BAS_MEDICAMENTS)
				.where(Tables.BAS_MEDICAMENTS.IS_DELETED.isFalse()).fetchInto(BasMedicaments.class);
		con.connection().close();
		return c;
	}

	public List<BasMedicaments> findTypeConsultationupprimer() {
		List<BasMedicaments> c = con.connection().selectFrom(Tables.BAS_MEDICAMENTS).where(Tables.BAS_MEDICAMENTS.IS_DELETED.isTrue())
				.fetchInto(BasMedicaments.class);
		con.connection().close();
		return c;
	}

	public BasMedicaments findById(Long id) {
		return super.findById(id);
	}
}
