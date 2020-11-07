package services;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
		Timestamp current = new Timestamp(System.currentTimeMillis());
		// System.out.println("curent :"+ current);
		Timestamp curent2 = this.getDateT(String.valueOf(current).substring(0, 10));
		List<Rendezvous> c = con.connection().selectFrom(Tables.RENDEZVOUS)
				.where(Tables.RENDEZVOUS.ON_DELETED.isFalse()).and(Tables.RENDEZVOUS.DATE_RDV.greaterThan(curent2))
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

	public Timestamp getDateT(String d) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate localDate = LocalDate.parse(d, formatter);
		LocalDateTime dateV = null;

		if (null != d && !d.trim().isEmpty()) {
			try {
				// System.out.println("la date est :"+dateV+tmpDate);

				dateV = LocalDateTime.of(localDate, LocalTime.of(0, 0));

			} catch (Exception e) {
				e.getMessage();
			}
		}

		return Timestamp.valueOf(dateV);

	}
}
