
package services;

import java.util.List;

import javax.inject.Inject;

import models.public_.Tables;
import models.public_.tables.daos.FacturesDetailsDao;
import models.public_.tables.pojos.FacturesDetails;

public class FacturesDetailsMainServices extends FacturesDetailsDao {

	private final ConnectionHelper con;

	@Inject
	public FacturesDetailsMainServices(ConnectionHelper con) {
		super();
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	public String saveLogical(FacturesDetails fact, boolean b) {
		try {
			if (b)
				super.insert(fact);
			else
				super.update(fact);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public List<FacturesDetails> findAllByFacture(Long num_fact) {
		List<FacturesDetails> c = con.connection().selectFrom(Tables.FACTURES_DETAILS)
				.where(Tables.FACTURES_DETAILS.IS_DELETED.isFalse()).and(Tables.FACTURES_DETAILS.FACTURE.eq(num_fact))
				.fetchInto(FacturesDetails.class);
		con.connection().close();
		return c;
	}
}
