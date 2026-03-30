package services;

import java.util.List;

import javax.inject.Inject;

import models.public_.Tables;
import models.public_.tables.daos.InfosMedicaleDao;
import models.public_.tables.pojos.Examens;
import models.public_.tables.pojos.InfosMedicale;

public class InfosMedicalMainServices extends InfosMedicaleDao {

	private final ConnectionHelper con;

	@Inject
	public InfosMedicalMainServices(ConnectionHelper con) {
		super();
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	public String saveLogical(InfosMedicale info, boolean b) {
		try {
			if (b)
				super.insert(info);
			else
				super.update(info);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	
	public String deletedInfo(InfosMedicale info) {
		try {
			super.delete(info);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public List<InfosMedicale> findAll() {
		List<InfosMedicale> c = con.connection().selectFrom(Tables.INFOS_MEDICALE)
				.where(Tables.INFOS_MEDICALE.IS_DELETED.isFalse()).fetchInto(InfosMedicale.class);
		con.connection().close();
		return c;
	}

	public List<InfosMedicale> findAllByPatient(Long idPatient) {
		
		List<InfosMedicale> c = con.connection().selectFrom(Tables.INFOS_MEDICALE)
				.where(Tables.INFOS_MEDICALE.IS_DELETED.isFalse()).and(Tables.INFOS_MEDICALE.PATIENT.eq(idPatient))
				.fetchInto(InfosMedicale.class);
		con.connection().close();
		return c;
	}
}
