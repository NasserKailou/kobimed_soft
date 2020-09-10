package services;

import java.util.List;

import javax.inject.Inject;

import models.public_.Tables;
import models.public_.tables.daos.PatientsDao;
import models.public_.tables.pojos.Patients;

/**
 * 
 * @author nasser
 *
 */
public class PatientsMainServices extends PatientsDao {

	private final ConnectionHelper con;

	@Inject
	public PatientsMainServices(ConnectionHelper con) {
		super();
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	public List<Patients> findLikeName(String query) {
		String lnom = null == query ? "" : query;
		lnom = lnom.endsWith("%") ? lnom : lnom + "%";
		return con.connection().selectFrom(Tables.PATIENTS).where(Tables.PATIENTS.NOM_PRENOM.like(lnom))
				.fetchInto(Patients.class);
	}

	public String saveLogical(Patients client, boolean b) {
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

	public List<Patients> findAll() {
		List<Patients> c = con.connection().selectFrom(Tables.PATIENTS).where(Tables.PATIENTS.IS_DELETED.isFalse())
				.fetchInto(Patients.class);
		con.connection().close();
		return c;
	}

	public List<Patients> listePatients() {
		List<Patients> c = con.connection().selectFrom(Tables.PATIENTS).where(Tables.PATIENTS.IS_DELETED.isFalse())
				.fetchInto(Patients.class);
		con.connection().close();
		return c;
	}

	public List<Patients> findPatientsupprimer() {
		List<Patients> c = con.connection().selectFrom(Tables.PATIENTS).where(Tables.PATIENTS.IS_DELETED.isTrue())
				.fetchInto(Patients.class);
		con.connection().close();
		return c;
	}

	public Patients findById(Long id) {
		return super.findById(id);
	}

	public Patients findByTelNumber(String num) {
		return con.connection().selectFrom(Tables.PATIENTS).where(Tables.PATIENTS.TELEPHONE.eq(num))
				.fetchOneInto(Patients.class);
	}

	/**
	 * for autocompletion
	 * 
	 * @param query
	 * @return
	 */
	public List<Patients> findLikePatient(String query) {
		String detail = null == query ? "" : query;
		detail = detail.endsWith("%") ? detail : detail + "%";
		List<Patients> detailsList = con.connection().selectFrom(Tables.PATIENTS)
				.where(Tables.PATIENTS.TELEPHONE.like(detail)).fetchInto(Patients.class);
		con.connection().close();
		return detailsList;
	}
}
