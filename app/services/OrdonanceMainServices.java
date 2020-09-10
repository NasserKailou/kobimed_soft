
package services;

import java.util.List;

import javax.inject.Inject;

import models.public_.Tables;
import models.public_.tables.daos.OrdonancesDao;
import models.public_.tables.pojos.Ordonances;

/**
 * 
 * @author nasser
 *
 */
public class OrdonanceMainServices extends OrdonancesDao {

	private final ConnectionHelper con;

	@Inject
	public OrdonanceMainServices(ConnectionHelper con) {
		super();
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	public List<Ordonances> findLikeName(String query) {
		String lnom = null == query ? "" : query;
		lnom = lnom.endsWith("%") ? lnom : lnom + "%";
		return con.connection().selectFrom(Tables.ORDONANCES).where(Tables.ORDONANCES.LIBELLE.like(lnom))
				.fetchInto(Ordonances.class);
	}

	public String saveLogical(Ordonances ordonances, boolean b) {
		try {
			if (b)
				super.insert(ordonances);
			else
				super.update(ordonances);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public List<Ordonances> findAll() {
		List<Ordonances> c = con.connection().selectFrom(Tables.ORDONANCES)
				.where(Tables.ORDONANCES.IS_DELETED.isFalse()).fetchInto(Ordonances.class);
		con.connection().close();
		return c;
	}

	public List<Ordonances> findOrdonanceByConsultation(Long idConsultation) {
		List<Ordonances> c = con.connection().selectFrom(Tables.ORDONANCES)
				.where(Tables.ORDONANCES.IS_DELETED.isFalse()).and(Tables.ORDONANCES.IS_SOIN.isFalse())
				.and(Tables.ORDONANCES.CONSULTATION.eq(idConsultation)).fetchInto(Ordonances.class);
		con.connection().close();
		return c;
	}

	/**
	 * cette methode renvoie la liste des soins donné a un patiens
	 * 
	 * @param idConsultation
	 * @return
	 */
	public List<Ordonances> findSoinByConsultation(Long idConsultation) {
		List<Ordonances> c = con.connection().selectFrom(Tables.ORDONANCES)
				.where(Tables.ORDONANCES.IS_DELETED.isFalse()).and(Tables.ORDONANCES.IS_SOIN.eq(true))
				.and(Tables.ORDONANCES.CONSULTATION.eq(idConsultation)).fetchInto(Ordonances.class);
		con.connection().close();
		return c;
	}

	public List<Ordonances> findOrdonanceSupprimerByConsultation(Long idConsultation) {
		List<Ordonances> c = con.connection().selectFrom(Tables.ORDONANCES).where(Tables.ORDONANCES.IS_DELETED.isTrue())
				.and(Tables.ORDONANCES.CONSULTATION.eq(idConsultation)).fetchInto(Ordonances.class);
		con.connection().close();
		return c;
	}

	public Ordonances findById(Long id) {
		return super.findById(id);
	}
}
