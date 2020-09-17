package services;

import java.util.List;

import javax.inject.Inject;

import models.public_.Tables;
import models.public_.tables.daos.BasSoinsDao;
import models.public_.tables.pojos.BasSoins;

/**
 * 
 * @author nasser
 *
 */
public class BaseSoinServices extends BasSoinsDao {

	private final ConnectionHelper con;

	@Inject
	public BaseSoinServices(ConnectionHelper con) {
		super();
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	public List<BasSoins> findLikeName(String query) {
		String lnom = null == query ? "" : query;
		lnom = lnom.endsWith("%") ? lnom : lnom + "%";
		return con.connection().selectFrom(Tables.BAS_SOINS).where(Tables.BAS_SOINS.IS_DELETED.isFalse())
				.and(Tables.BAS_SOINS.LIBELLE.like(lnom)).fetchInto(BasSoins.class);
	}
}
