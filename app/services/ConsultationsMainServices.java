package services;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;

import models.public_.Tables;
import models.public_.tables.daos.ConsultationsDao;
import models.public_.tables.pojos.Consultations;
import models.public_.tables.pojos.VConsultations;

/**
 * 
 * @author nasser
 *
 */
public class ConsultationsMainServices extends ConsultationsDao {

	private final ConnectionHelper con;

	@Inject
	public ConsultationsMainServices(ConnectionHelper con) {
		super();
		this.con = con;
		setConfiguration(con.connection().configuration());
	}

	public String saveLogical(Consultations consultation, boolean b) {
		try {
			if (b)
				super.insert(consultation);
			else
				super.update(consultation);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public List<VConsultations> listeConsultations() {
		List<VConsultations> c = con.connection().selectFrom(Tables.V_CONSULTATIONS)
				.where(Tables.V_CONSULTATIONS.IS_DELETED.isFalse()).orderBy(Tables.V_CONSULTATIONS.ID.desc())
				.fetchInto(VConsultations.class);
		con.connection().close();
		return c;
	}

	/**
	 * cette fonction retour la liste des consultations par user
	 * 
	 * @param owner
	 * @return
	 */
	public List<VConsultations> listeConsultations(String owner) {
		List<VConsultations> c = con.connection().selectFrom(Tables.V_CONSULTATIONS)
				.where(Tables.V_CONSULTATIONS.IS_DELETED.isFalse()).and(Tables.V_CONSULTATIONS.WHO_DONE.eq(owner))
				.orderBy(Tables.V_CONSULTATIONS.ID.desc()).fetchInto(VConsultations.class);
		con.connection().close();
		return c;
	}

	/**
	 * cette fonction retour la liste des consultations par user
	 * 
	 * @param owner
	 * @return
	 */
	public List<VConsultations> listeConsultationsByOwnerToDate(String owner) {
		Timestamp current = new Timestamp(System.currentTimeMillis());
		// System.out.println("curent :"+ current);
		Timestamp curent2 = this.getDateT(String.valueOf(current).substring(0, 10));
		// System.out.println("curent :"+ curent2);
		List<VConsultations> c = con.connection().selectFrom(Tables.V_CONSULTATIONS)
				.where(Tables.V_CONSULTATIONS.IS_DELETED.isFalse()).and(Tables.V_CONSULTATIONS.WHO_DONE.eq(owner))
				.and(Tables.V_CONSULTATIONS.WHEN_DONE.greaterThan(curent2)).orderBy(Tables.V_CONSULTATIONS.ID.desc())
				.fetchInto(VConsultations.class);
		con.connection().close();
		return c;
	}

	/**
	 * cette fonction retour la liste des pour le gestionnaire
	 * 
	 * @param owner
	 * @return
	 */
	public List<VConsultations> listeConsultationsByDate() {
		Timestamp current = new Timestamp(System.currentTimeMillis());
		// System.out.println("curent :"+ current);
		Timestamp curent2 = this.getDateT(String.valueOf(current).substring(0, 10));
		// System.out.println("curent :"+ curent2);
		List<VConsultations> c = con.connection().selectFrom(Tables.V_CONSULTATIONS)
				.where(Tables.V_CONSULTATIONS.IS_DELETED.isFalse())
				.and(Tables.V_CONSULTATIONS.WHEN_DONE.greaterThan(curent2)).orderBy(Tables.V_CONSULTATIONS.ID.desc())
				.fetchInto(VConsultations.class);
		con.connection().close();
		return c;
	}

	public List<VConsultations> listeConsultationsDeleted() {
		List<VConsultations> c = con.connection().selectFrom(Tables.V_CONSULTATIONS)
				.where(Tables.V_CONSULTATIONS.IS_DELETED.isTrue()).orderBy(Tables.V_CONSULTATIONS.ID.desc())
				.fetchInto(VConsultations.class);
		con.connection().close();
		return c;
	}

	public List<Object> listeConsultation() {
		List<Object> c = con.connection().selectFrom(Tables.V_CONSULTATIONS)
				.where(Tables.V_CONSULTATIONS.IS_DELETED.isFalse()).orderBy(Tables.V_CONSULTATIONS.ID.desc())
				.fetchInto(VConsultations.class);
		con.connection().close();
		return c;
	}

	public List<Consultations> listeConsultationsByDate(Timestamp dateToCheck) {
		List<Consultations> c = con.connection().selectFrom(Tables.CONSULTATIONS)
				.where(Tables.CONSULTATIONS.IS_DELETED.isFalse()).and(Tables.CONSULTATIONS.WHEN_DONE.eq(dateToCheck))
				.fetchInto(Consultations.class);
		con.connection().close();
		return c;
	}

	public List<Consultations> listeConsultationsBetween(Timestamp dateToCheck1, Timestamp dateToCheck2) {
		List<Consultations> c = con.connection().selectFrom(Tables.CONSULTATIONS)
				.where(Tables.CONSULTATIONS.IS_DELETED.isFalse())
				.and(Tables.CONSULTATIONS.WHEN_DONE.between(dateToCheck1, dateToCheck2)).fetchInto(Consultations.class);
		con.connection().close();
		return c;
	}

	public List<VConsultations> listeConsultationsFilter(Timestamp dateToCheck1, Timestamp dateToCheck2,
			String numTel) {
		List<VConsultations> c = con.connection().selectFrom(Tables.V_CONSULTATIONS)
				.where(Tables.V_CONSULTATIONS.IS_DELETED.isFalse())
				.and((Tables.V_CONSULTATIONS.TEL_PATIENT.like(numTel))
						.or(Tables.V_CONSULTATIONS.WHEN_DONE.between(dateToCheck1, dateToCheck2)))
				.fetchInto(VConsultations.class);
		con.connection().close();
		return c;
	}

	public List<VConsultations> listeConsultationsFilter(String numTel) {
		String tel = null == numTel ? "" : numTel;
		tel = tel.endsWith("%") ? tel : tel + "%";

		List<VConsultations> c = con.connection().selectFrom(Tables.V_CONSULTATIONS)
				.where(Tables.V_CONSULTATIONS.IS_DELETED.isFalse()).and(Tables.V_CONSULTATIONS.TEL_PATIENT.like(tel))
				.fetchInto(VConsultations.class);
		con.connection().close();
		return c;
	}

	public List<VConsultations> listeRDV(Timestamp dateToCheck) {
		List<VConsultations> c = con.connection().selectFrom(Tables.V_CONSULTATIONS)
				.where(Tables.V_CONSULTATIONS.IS_DELETED.isFalse()).and(Tables.V_CONSULTATIONS.DATE_RDV.eq(dateToCheck))
				.fetchInto(VConsultations.class);
		con.connection().close();
		return c;
	}

	public List<Consultations> listeConsultationsByPatient(Long idPatients) {
		List<Consultations> c = con.connection().selectFrom(Tables.CONSULTATIONS)
				.where(Tables.CONSULTATIONS.IS_DELETED.isFalse()).and(Tables.CONSULTATIONS.PATIENT.eq(idPatients))
				.fetchInto(Consultations.class);
		con.connection().close();
		return c;
	}

	/**
	 * return la liste des consultations pour un patient
	 * 
	 * @param idPatients
	 * @return
	 */
	public List<VConsultations> listeConsultationsPatient(Long idPatients) {
		List<VConsultations> c = con.connection().selectFrom(Tables.V_CONSULTATIONS)
				.where(Tables.V_CONSULTATIONS.IS_DELETED.isFalse()).and(Tables.V_CONSULTATIONS.PATIENT.eq(idPatients))
				.fetchInto(VConsultations.class);
		con.connection().close();
		return c;
	}

	public Consultations findById(Long id) {
		return super.findById(id);
	}

	public VConsultations findVById(Long id) {
		VConsultations c = con.connection().selectFrom(Tables.V_CONSULTATIONS).where(Tables.V_CONSULTATIONS.ID.eq(id))
				.fetchOneInto(VConsultations.class);
		con.connection().close();
		return c;
	}

	/**
	 * 
	 * @param montant
	 * @return
	 */
	public String getMontantLettre(Long montant) {

		String montantLettres = "";

		NumberFormat formatter = new RuleBasedNumberFormat(Locale.FRANCE, RuleBasedNumberFormat.SPELLOUT);
		String montantL = formatter.format(montant);
		montantLettres = montantL.substring(0, 1).toUpperCase() + montantL.substring(1);

		return montantLettres;

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

	public void backup() throws IOException {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		try {
			Runtime.getRuntime().exec("/usr/bin/pg_dump --file \"/home/ins/Bureau/backups/" + timestamp
					+ "test_dicko.backup\" --host \"127.0.0.1\" --port \"5432\" --username \"postgres\" --no-password --verbose --role \"postgres\" --format=c --blobs --encoding \"UTF8\" \"dicko\"");
			System.out.println("backup effectuer");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
}
