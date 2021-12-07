package services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.inject.Inject;

import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;

import models.public_.Tables;
import models.public_.tables.daos.ConsultationsDao;
import models.public_.tables.pojos.Consultations;
import models.public_.tables.pojos.Examens;
import models.public_.tables.pojos.Factures;
import models.public_.tables.pojos.FacturesDetails;
import models.public_.tables.pojos.Ordonances;
import models.public_.tables.pojos.VConsultations;

/**
 * 
 * @author nasser
 *
 */
public class ConsultationsMainServices extends ConsultationsDao {

	private final ConnectionHelper con;
	
	private String fileDir = new File("").getAbsolutePath() + "/Niger_Driver/Niger_Driver/mcf/";
	private String fileDirReponse = new File("").getAbsolutePath() + "/Niger_Driver/Niger_Driver/mcf/get_info.txt";
	ExamenMainServices examServices;
	OrdonanceMainServices ordServices;
	FacturesMainServices facturesServices;
	FacturesDetailsMainServices factDetailsServices;

	@Inject
	public ConsultationsMainServices(ConnectionHelper con, ExamenMainServices examServices,
			OrdonanceMainServices ordServices, FacturesMainServices facturesServices,
			FacturesDetailsMainServices factDetailsServices) {
		super();
		this.con = con;
		setConfiguration(con.connection().configuration());

		this.examServices = examServices;
		this.ordServices = ordServices;
		this.facturesServices = facturesServices;
		this.factDetailsServices = factDetailsServices;
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

	/**
	 * return une consultation en fonction de son numero
	 * 
	 * @param num
	 * @return
	 */
	public Consultations findByNumConsultation(String num) {
		return con.connection().selectFrom(Tables.CONSULTATIONS).where(Tables.CONSULTATIONS.NUMERO_CONSUL.eq(num))
				.fetchOneInto(Consultations.class);
	}

	public VConsultations findByNumVConsultation(String num) {
		return con.connection().selectFrom(Tables.V_CONSULTATIONS).where(Tables.V_CONSULTATIONS.NUMERO_CONSUL.eq(num))
				.fetchOneInto(VConsultations.class);
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

	public String sendInfoCertification(String numConsultation, String fileName, String typeFact) throws IOException {

		File file = new File(fileDir + findByNumVConsultation(numConsultation).getId() + ".txt");

		// créer le fichier s'il n'existe pas
		if (!file.exists()) {
			System.out.println("Creation fichier");
			file.createNewFile();
		}
		Factures facture = new Factures();
		FacturesDetails factDetails = new FacturesDetails();
		String typeFacture, numm;
		numm = String.valueOf((new Timestamp(System.currentTimeMillis())).getTime());
		if (typeFact.equals("VENTE")) {
			typeFacture = "FV";
			facture.setNumFact(numConsultation);

		} else {
			typeFacture = "FA";

			facture.setNumFact(numm);
		}
		Long total = 0L;
		// renseigne les informations de la factures
		facture.setDateEdition(findByNumVConsultation(numConsultation).getWhenDone());
		facture.setTypeFacture(typeFact);
		facture.setDoit(findByNumVConsultation(numConsultation).getNomPatient() + " - "
				+ findByNumVConsultation(numConsultation).getTelPatient());
		facture.setIsDeleted(false);
		facture.setWhenDone(new Timestamp(System.currentTimeMillis()));
		facture.setWhoDone("");

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("@1Operator Kobi Multi-Services\n");
		bw.write("#NIF45292\n");
		bw.write("#RT" + typeFacture + "\n");
		// pour les facture de remboursement
		if (typeFact.equals("AVOIR"))
			bw.write("#RFED020000571" + "-" + findByNumVConsultation(numConsultation).getRNbrFact() + "-"
					+ findByNumVConsultation(numConsultation).getRNbrTotal() + "\n");

		bw.write("#VTTTC\n");
		bw.write("#ISFED020000571\n");
		bw.write("#FN" + findByNumVConsultation(numConsultation).getId() + "\n");
		// bw.write("#IFU-NIFKERa \n");
		// bw.write("#RTFV \n");
		// bw.write("#VTHT \n");
		// bw.write("#ISF -ED............ \n");
		if (fileName.equals("recu")) {
			
			
			facture.setMontantTotal(findByNumVConsultation(numConsultation).getPrixConsultation());
			facture.setMontantLettre(findByNumVConsultation(numConsultation).getMontantEnLettre());

			factDetails.setLibelle(findByNumVConsultation(numConsultation).getLibelleType());
			factDetails.setPrixUnitaire(findByNumVConsultation(numConsultation).getPrixConsultation());
			factDetails.setQuantie(1L);
			factDetails.setTotalTtc(findByNumVConsultation(numConsultation).getPrixConsultation());
			factDetails.setTauxTva(19L);
			factDetailsServices.saveLogical(factDetails, true);
			bw.write("%" + findByNumVConsultation(numConsultation).getLibelleType() + "^" + "B19.00%" + "^"
					+ findByNumVConsultation(numConsultation).getPrixConsultation() + "^" + "1" + "^"
					+ findByNumVConsultation(numConsultation).getPrixConsultation() + "\n");
		}
		if (fileName.equals("bulletin_exam")) {
			List<Examens> exs = examServices.findExamensByConsultation(findByNumVConsultation(numConsultation).getId());
			for (Examens e : exs) {
				bw.write("%" + e.getLibelle() + "^" + "B19.00%" + "^" + e.getCoutExamen() + "^" + "1" + "^"
						+ e.getCoutExamen() + "\n");
				total += e.getCoutExamen();
				// numFacture en fonction du type de facture
				if (typeFact.equals("VENTE"))
					factDetails.setFacture(numConsultation);
				else
					factDetails.setFacture(numm);

				factDetails.setLibelle(e.getLibelle());
				factDetails.setPrixUnitaire(e.getCoutExamen());
				factDetails.setQuantie(1L);
				factDetails.setTotalTtc(e.getCoutExamen());
				factDetails.setTauxTva(19L);
				factDetailsServices.saveLogical(factDetails, true);

			}
		}
		if (fileName.equals("etat_soin")) {
			List<Ordonances> ods = ordServices.findSoinByConsultation(findByNumVConsultation(numConsultation).getId());
			for (Ordonances o : ods) {
				bw.write("%" + o.getLibelle() + "^" + "B19.00%" + "^" + o.getMontant() * o.getNombre() + "^"
						+ o.getNombre() + "^" + o.getMontant() + "\n");

				total += o.getMontant() * o.getNombre();

				// numFacture en fonction du type de facture
				if (typeFact.equals("VENTE"))
					factDetails.setFacture(numConsultation);
				else
					factDetails.setFacture(numm);

				factDetails.setLibelle(o.getLibelle());
				factDetails.setPrixUnitaire(o.getMontant());
				factDetails.setQuantie(o.getNombre());
				factDetails.setTotalTtc(o.getMontant() * o.getNombre());
				factDetails.setTauxTva(19L);
				factDetailsServices.saveLogical(factDetails, true);
			}
		}

		if (fileName.equals("etat_soin") || fileName.equals("bulletin_exam")) {
			facture.setMontantTotal(total);
			facture.setMontantLettre(getMontantLettre(total));
		}
		
		//enregistrer la facture 
		facturesServices.saveLogical(facture, true);
		
		
		System.out.println("############# FIN ###########");
		bw.write("#EE0\n");
		bw.close();
		return "ok";
	}

	public String getInfosCertification(String num) throws InterruptedException {
		// Facture f = new Facture();
		System.out.println("############# " + fileDir + findByNumVConsultation(num).getId() + ".rep");
		try {
			// Le fichier d'entrée
			File file = new File(fileDir + findByNumVConsultation(num).getId() + ".rep");
			// Créer l'objet File Reader
			FileReader fr = new FileReader(file);
			// Créer l'objet BufferedReader
			BufferedReader br = new BufferedReader(fr);
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				// ajoute la ligne au buffer
				sb.append(line);
				String[] st = line.split(line, ',');

				sb.append("\n");

				// System.out.println("prenom = "+st[2]);
			}
			fr.close();
			Consultations c = this.findByNumConsultation(num);
			Factures facture = facturesServices.findByNumFact(num);
			// renseigner consultation
			if (this.findByNumConsultation(num).getRSignatureFact() == null
					|| this.findByNumConsultation(num).getRSignatureFact().isEmpty()) {
				//renseigner la consultation 
				c.setRNbrFact(extraireDonnees(sb.toString())[0]);
				c.setRNbrTotal(extraireDonnees(sb.toString())[1]);
				c.setRTypeFact(extraireDonnees(sb.toString())[2]);
				c.setRDateFact(extraireDonnees(sb.toString())[3]);
				c.setRNumDispositifFact(extraireDonnees(sb.toString())[4]);
				c.setRNifFact(extraireDonnees(sb.toString())[5]);
				c.setRSignatureFact(extraireDonnees(sb.toString())[6]);
				this.saveLogical(c, false);
			}
			// renseigner facture
			facture.setRNbrFact(extraireDonnees(sb.toString())[0]);
			facture.setRNbrTotal(extraireDonnees(sb.toString())[1]);
			facture.setRTypeFact(extraireDonnees(sb.toString())[2]);
			facture.setRDateFact(extraireDonnees(sb.toString())[3]);
			facture.setRNumDispositifFact(extraireDonnees(sb.toString())[4]);
			facture.setRNifFact(extraireDonnees(sb.toString())[5]);
			facture.setRSignatureFact(extraireDonnees(sb.toString())[6]);
			facturesServices.saveLogical(facture, false);

			System.out.println("Contenu du fichier: ");

			System.out.println(sb.toString());
			System.out.println("num :" + extraireDonnees(sb.toString())[0]);
			// Thread.sleep(4000);
			System.out.println("toal :" + extraireDonnees(sb.toString())[1]);
			// Thread.sleep(4000);
			System.out.println("type facture :" + extraireDonnees(sb.toString())[2]);
			// Thread.sleep(4000);
			System.out.println("date :" + extraireDonnees(sb.toString())[3]);
			System.out.println("dispositif mcf :" + extraireDonnees(sb.toString())[4]);
			System.out.println("nif :" + extraireDonnees(sb.toString())[5]);
			System.out.println("signature numerique :" + extraireDonnees(sb.toString())[6]);

			return "ok";
		} catch (IOException e) {
			System.out.println("erreur survenu :" + e.getMessage());
			return e.getMessage();
		}
	}

	public String[] extraireDonnees(String tmp) {

		if (tmp != null) {
			// Créer un outil qui découpe la chaine passée en paramètre (premier
			// paramètre)
			// en utilisant le point-virgule (second paramètre) pour séparer les
			// mots
			StringTokenizer st = new StringTokenizer(tmp, ",");
			int i = 0;
			// Créer un tableau à la taille du nombre de mots à extraireé
			String mot[] = new String[st.countTokens()];

			// Parcourir l'ensemble des mots à extraire
			while (st.hasMoreTokens()) {
				// Les mémoriser dans un tableau
				mot[i] = st.nextToken();
				i++;
			}
			// Retourner le tableau contenant les mots extraits
			return mot;
		} else
			return null;
	}
}
