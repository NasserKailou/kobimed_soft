package services;

import java.util.List;

import javax.inject.Inject;

import models.public_.Tables;
import models.public_.tables.daos.PartenaireDao;
import models.public_.tables.pojos.Partenaire;
import models.public_.tables.pojos.SConsultations;

/**
 * 
 * @author nasser
 *
 */
public class PartenaireMainServices extends PartenaireDao {

	private final ConnectionHelper con;

	@Inject
	public PartenaireMainServices(ConnectionHelper con) {
		super();
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	public List<Partenaire> findLikeName(String query) {
		String lnom = null == query ? "" : query;
		lnom = lnom.endsWith("%") ? lnom : lnom + "%";
		return con.connection().selectFrom(Tables.PARTENAIRE).where(Tables.PARTENAIRE.LIBELLE.like(lnom))
				.fetchInto(Partenaire.class);
	}

	public String saveLogical(Partenaire client, boolean b) {
		try {
			if (b)
				super.insert(client);
			else
				super.update(client);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public List<Partenaire> findAll() {
		List<Partenaire> c = con.connection().selectFrom(Tables.PARTENAIRE)
				.where(Tables.PARTENAIRE.IS_DELETED.isFalse()).fetchInto(Partenaire.class);
		con.connection().close();
		return c;
	}

	public List<Partenaire> listePartenaire() {
		List<Partenaire> c = con.connection().selectFrom(Tables.PARTENAIRE)
				.where(Tables.PARTENAIRE.IS_DELETED.isFalse()).fetchInto(Partenaire.class);
		con.connection().close();
		return c;
	}

	public List<SConsultations> consultationParPArtenaire() {
		List<SConsultations> c = con.connection().selectFrom(Tables.S_CONSULTATIONS).fetchInto(SConsultations.class);
		con.connection().close();
		return c;
	}

	public List<Partenaire> findPartenaireupprimer() {
		List<Partenaire> c = con.connection().selectFrom(Tables.PARTENAIRE).where(Tables.PARTENAIRE.IS_DELETED.isTrue())
				.fetchInto(Partenaire.class);
		con.connection().close();
		return c;
	}

	public Partenaire findById(Long id) {
		return super.findById(id);
	}
}
