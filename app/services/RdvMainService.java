package services;

import java.sql.Timestamp;
import java.util.List;

import javax.inject.Inject;

import models.public_.Tables;
import models.public_.tables.daos.RendezvousDao;
import models.public_.tables.pojos.Rendezvous;

/**
 * 
 * @author nasser
 *
 */
public class RdvMainService extends RendezvousDao {

	private final ConnectionHelper con;

	@Inject
	public RdvMainService(ConnectionHelper con) {
		super();
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	public String saveLogical(Rendezvous rdv, boolean b) {
		try {
			if (b)
				super.insert(rdv);
			else
				super.update(rdv);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public String suppression(Rendezvous rdv) {
		try {
			super.delete(rdv);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	
	/**
	 * retourn la liste des rdv a une date
	 * 
	 * @param dateToCheck
	 * @return
	 */
	public List<Rendezvous> listeRDV(Timestamp dateToCheck) {
		List<Rendezvous> c = con.connection().selectFrom(Tables.RENDEZVOUS)
				.where(Tables.RENDEZVOUS.ON_DELETED.isFalse()).and(Tables.RENDEZVOUS.DATE_RDV.eq(dateToCheck))
				.fetchInto(Rendezvous.class);
		con.connection().close();
		return c;
	}

	/**
	 * retourne la liste des rendezvous entre deux date
	 * 
	 * @param dateToCheck1
	 * @param dateToCheck2
	 * @return
	 */
	public List<Rendezvous> RdvBetween(Timestamp dateToCheck1, Timestamp dateToCheck2) {
		List<Rendezvous> c = con.connection().selectFrom(Tables.RENDEZVOUS)
				.where(Tables.RENDEZVOUS.ON_DELETED.isFalse())
				.and(Tables.RENDEZVOUS.DATE_RDV.between(dateToCheck1, dateToCheck2)).fetchInto(Rendezvous.class);
		con.connection().close();
		return c;
	}

	public List<Rendezvous> listeRdvAll() {
		List<Rendezvous> c = con.connection().selectFrom(Tables.RENDEZVOUS).where(Tables.RENDEZVOUS.ON_DELETED.isFalse())
				.fetchInto(Rendezvous.class);
		con.connection().close();
		return c;
	}
	/**
	 * return la liste des rdv supprimer
	 * 
	 * @return
	 */
	public List<Rendezvous> RdvSupprimer() {
		List<Rendezvous> c = con.connection().selectFrom(Tables.RENDEZVOUS).where(Tables.RENDEZVOUS.ON_DELETED.isTrue())
				.fetchInto(Rendezvous.class);
		con.connection().close();
		return c;
	}

}
