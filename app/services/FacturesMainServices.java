package services;

import java.util.List;

import javax.inject.Inject;

import models.public_.Tables;
import models.public_.tables.daos.FacturesDao;
import models.public_.tables.daos.FacturesDetailsDao;
import models.public_.tables.pojos.BasExamens;
import models.public_.tables.pojos.Consultations;
import models.public_.tables.pojos.Factures;
import models.stock.tables.pojos.Facture;

public class FacturesMainServices extends FacturesDao {

	private final ConnectionHelper con;

	@Inject
	public FacturesMainServices(ConnectionHelper con) {
		super();
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}
	
	
	public String saveLogical(Factures fact, boolean b) {
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

	public Factures findByNumFact(String num) {
		return con.connection().selectFrom(Tables.FACTURES).where(Tables.FACTURES.NUM_FACT.eq(num))
				.fetchOneInto(Factures.class);
	}
}
