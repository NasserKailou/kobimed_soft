package services;

import java.util.List;

import javax.inject.Inject;

import models.public_.Tables;
import models.public_.tables.daos.BasExamensDao;
import models.public_.tables.pojos.BasExamens;
import models.public_.tables.pojos.TypeConsultation;

/**
 * 
 * @author nasser
 *
 */
public class BaseExamenServices extends BasExamensDao {

	private final ConnectionHelper con;

	@Inject
	public BaseExamenServices(ConnectionHelper con) {
		super();
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	public List<BasExamens> findLikeName(String query) {

		String lnom = null == query ? "" : query;
		lnom = lnom.endsWith("%") ? lnom : lnom + "%";

		return con.connection().selectFrom(Tables.BAS_EXAMENS).where(Tables.BAS_EXAMENS.IS_DELETED.isFalse())
				.and(Tables.BAS_EXAMENS.LIBELLE.like(lnom)).fetchInto(BasExamens.class);
	}

	public String saveLogical(BasExamens exam, boolean b) {
		try {
			if (b)
				super.insert(exam);
			else
				super.update(exam);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public List<BasExamens> findAll() {
		List<BasExamens> c = con.connection().selectFrom(Tables.BAS_EXAMENS)
				.where(Tables.BAS_EXAMENS.IS_DELETED.isFalse()).fetchInto(BasExamens.class);
		con.connection().close();
		return c;
	}
	
	public BasExamens findById(Long id) {
		return super.findById(id);
	}
}
