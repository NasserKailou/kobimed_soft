package services;

import java.util.List;

import javax.inject.Inject;

import models.public_.Tables;
import models.public_.tables.daos.TypeConsultationDao;
import models.public_.tables.pojos.TypeConsultation;

/**
 * 
 * @author nasser
 *
 */
public class TypeConsultationsMainServices extends TypeConsultationDao {

	private final ConnectionHelper con;

	@Inject
	public TypeConsultationsMainServices(ConnectionHelper con) {
		super();
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	public List<TypeConsultation> findLikeName(String query) {
		String lnom = null == query ? "" : query.replace(" ", "");
		// lnom = lnom.endsWith("%") ? lnom : lnom + "%";
		return con.connection().selectFrom(Tables.TYPE_CONSULTATION)
				.where(Tables.TYPE_CONSULTATION.ID.eq(Long.valueOf(lnom))).fetchInto(TypeConsultation.class);
	}

	public String saveLogical(TypeConsultation client, boolean b) {
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

	public List<TypeConsultation> findAll() {
		List<TypeConsultation> c = con.connection().selectFrom(Tables.TYPE_CONSULTATION)
				.where(Tables.TYPE_CONSULTATION.IS_DELETED.isFalse()).orderBy(Tables.TYPE_CONSULTATION.LIBELLE.asc())
				.fetchInto(TypeConsultation.class);
		con.connection().close();
		return c;
	}

	public List<TypeConsultation> listeTypeConsultation() {
		List<TypeConsultation> c = con.connection().selectFrom(Tables.TYPE_CONSULTATION)
				.where(Tables.TYPE_CONSULTATION.IS_DELETED.isFalse()).orderBy(Tables.TYPE_CONSULTATION.LIBELLE.asc())
				.fetchInto(TypeConsultation.class);
		con.connection().close();
		return c;
	}

	public List<TypeConsultation> findTypeConsultationupprimer() {
		List<TypeConsultation> c = con.connection().selectFrom(Tables.TYPE_CONSULTATION)
				.where(Tables.TYPE_CONSULTATION.IS_DELETED.isTrue()).fetchInto(TypeConsultation.class);
		con.connection().close();
		return c;
	}

	public TypeConsultation findById(Long id) {
		return super.findById(id);
	}
}
