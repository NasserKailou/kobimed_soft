package services;

import java.util.List;

import javax.inject.Inject;

import org.python.antlr.PythonParser.return_stmt_return;

import models.public_.Tables;
import models.public_.tables.daos.ExamensDao;
import models.public_.tables.pojos.Examens;
import models.public_.tables.pojos.VExamens;

/**
 * 
 * @author nasser
 *
 */
public class ExamenMainServices extends ExamensDao {

	private final ConnectionHelper con;

	@Inject
	public ExamenMainServices(ConnectionHelper con) {
		super();
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	public List<Examens> findLikeName(String query) {
		String lnom = null == query ? "" : query;
		lnom = lnom.endsWith("%") ? lnom : lnom + "%";
		return con.connection().selectFrom(Tables.EXAMENS).where(Tables.EXAMENS.LIBELLE.like(lnom))
				.fetchInto(Examens.class);
	}

	public String saveLogical(Examens examen, boolean b) {
		try {
			if (b)
				super.insert(examen);
			else
				super.update(examen);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public List<Examens> findAll() {
		List<Examens> c = con.connection().selectFrom(Tables.EXAMENS).where(Tables.EXAMENS.IS_DELETED.isFalse())
				.fetchInto(Examens.class);
		con.connection().close();
		return c;
	}

	public List<Examens> findExamensByConsultation(Long idConsultation) {
		List<Examens> c = con.connection().selectFrom(Tables.EXAMENS).where(Tables.EXAMENS.IS_DELETED.isFalse())
				.and(Tables.EXAMENS.CONSULTATION.eq(idConsultation)).fetchInto(Examens.class);
		con.connection().close();
		return c;
	}

	public List<Examens> findExamenSupprimerByConsultation(Long idConsultation) {
		List<Examens> c = con.connection().selectFrom(Tables.EXAMENS).where(Tables.EXAMENS.IS_DELETED.isTrue())
				.and(Tables.EXAMENS.CONSULTATION.eq(idConsultation)).fetchInto(Examens.class);
		con.connection().close();
		return c;
	}

	/**
	 * Renvoi la liste des examens lié a un patient
	 * @param idPatient
	 * @return
	 */
	public List<VExamens> ListExamensPatiens(Long idPatient) {
		List<VExamens> listExam = con.connection().selectFrom(Tables.V_EXAMENS)
				.where(Tables.V_EXAMENS.IS_DELETED.isFalse()).and(Tables.V_EXAMENS.ID_PATIENT.eq(idPatient))
				.fetchInto(VExamens.class);
		con.connection().close();
		return listExam;
	}
	
	/**
	 * 
	 * @param telPatient
	 * @return
	 */
	public List<VExamens> ListExamensPatiensByTel(String telPatient) {
		List<VExamens> listExam = con.connection().selectFrom(Tables.V_EXAMENS)
				.where(Tables.V_EXAMENS.IS_DELETED.isFalse()).and(Tables.V_EXAMENS.TEL_PATIENT.eq(telPatient))
				.fetchInto(VExamens.class);
		con.connection().close();
		return listExam;
	}

	public Examens findById(Long id) {
		return super.findById(id);
	}
}
