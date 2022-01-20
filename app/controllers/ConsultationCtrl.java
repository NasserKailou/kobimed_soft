package controllers;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import models.public_.tables.pojos.Consultations;
import models.public_.tables.pojos.Examens;
import models.public_.tables.pojos.Ordonances;
import models.public_.tables.pojos.Patients;
import models.public_.tables.pojos.Personnels;
import models.public_.tables.pojos.Rendezvous;
import models.public_.tables.pojos.TypeConsultation;
import models.public_.tables.pojos.VConsultations;
import play.data.Form;
import play.data.FormFactory;
import play.filters.csrf.AddCSRFToken;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Security;
import services.ConsultationsMainServices;
import services.PartenaireMainServices;
import services.PatientsMainServices;
import services.PersonnelMainServices;
import services.RdvMainService;
import services.TypeConsultationsMainServices;
import utils.CallJasperReport;
import utils.Secured;
import utils.ViewMode;

/**
 * 
 * @author nasser
 *
 */
@Security.Authenticated(Secured.class)
public class ConsultationCtrl extends Controller {

	FormFactory formFactory;
	ConsultationsMainServices consultationServices;
	TypeConsultationsMainServices typeConServices;
	PatientsMainServices patientService;
	PartenaireMainServices partServices;
	PersonnelMainServices persServices;
	RdvMainService rdvService;
	CallJasperReport jasper;
	Long coutConsultation = 0L;

	@Inject
	public ConsultationCtrl(FormFactory formFactory, ConsultationsMainServices consultationServices,
			TypeConsultationsMainServices typeConServices, PatientsMainServices patientService,
			PartenaireMainServices partServices, RdvMainService rdvService, CallJasperReport jasper,
			PersonnelMainServices persServices) {
		super();
		this.formFactory = formFactory;
		this.consultationServices = consultationServices;
		this.typeConServices = typeConServices;
		this.patientService = patientService;
		this.partServices = partServices;
		this.rdvService = rdvService;
		this.jasper = jasper;
		this.persServices = persServices;

	}

	public Result show(Request request, String subAction, Long idConsul) {
		// System.out.println("les sessions sont login:"
		// +request.session().get("login")
		// +" droit :"+ request.session().get("droit") +" nonUser :" +
		// request.session().get("nomUser"));

		// if (!isAdmin()) {
		// return redirect(routes.AuthenticationCtrl.logout());
		// }
		// System.out.println("la liste des consultation du jour sont :"
		// +
		// consultationServices.listeConsultationsByOwnerToDate(session("login")));
		String viewMode;
		// Consultations c;
		VConsultations c;
		List<VConsultations> consultations = new ArrayList<VConsultations>();
		// consultationServices.listeConsultations();
		if (String.valueOf(request.session().get("droit").get()).equals("Gestionnaire"))
			consultations = consultationServices.listeConsultationsByDate();
		else
			consultations = consultationServices
					.listeConsultationsByOwnerToDate(String.valueOf(request.session().get("login").get()));
		// List<VConsultations> consultations =
		// consultationServices.listeConsultationsByOwnerToDate(session("login"));
		List<Personnels> medecins = persServices.listes("Medecin");
		if (0 == idConsul) {
			c = new VConsultations();
			viewMode = ViewMode.VIEW_MODE_CREATE;
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			c = consultationServices.findVById(idConsul);
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			c = consultationServices.findVById(idConsul);
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
			c = consultationServices.findVById(idConsul);

		}
		// System.out.println("la liste des consultations est :"+
		// consultations);
		return ok(views.html.consultations.render(viewMode, consultations, typeConServices.findAll(), medecins, c,
				request));

	}

	public Result restaure(Request request, Long idConsul) {
		Consultations c = consultationServices.findById(idConsul);
		c.setIsDeleted(false);
		consultationServices.update(c);

		VConsultations cc = new VConsultations();

		return ok(views.html.consultationsDeleted.render(ViewMode.VIEW_MODE_CREATE,
				consultationServices.listeConsultationsDeleted(), typeConServices.findAll(),
				persServices.listes("Medecin"), cc, request));

	}

	public Result consultationSupp(Request request) {
		VConsultations c = new VConsultations();

		return ok(views.html.consultationsDeleted.render(ViewMode.VIEW_MODE_CREATE,
				consultationServices.listeConsultationsDeleted(), typeConServices.findAll(),
				persServices.listes("Medecin"), c, request));

	}

	@AddCSRFToken
	public Result save(Request request) throws IOException, InterruptedException{

		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");

		Form<Consultations> uForm = formFactory.form(Consultations.class).bindFromRequest(request);
		String agePatient = formFactory.form().bindFromRequest(request).get("age");
		// String structurePartenaire =
		// formFactory.form().bindFromRequest().get("structurePatient");
		final String telPatient = formFactory.form().bindFromRequest(request).get("telPatient");

		String idAssurreur = formFactory.form().bindFromRequest(request).get("partenaireid");
		String numero = null == telPatient ? "" : telPatient;

		Consultations c = uForm.get();
		Patients p = new Patients();

		// System.out.println("id du part est :" + idAssurreur);
		// controle du taux de couverture
		if (c.getTauxCouverture() == null)

			if (idAssurreur.isEmpty() || idAssurreur == null)
				p.setPartenaire(1L);
			else
				p.setPartenaire(Long.valueOf(idAssurreur.replace(" ", "")));
		// Chercher le patient correspondant
		p = patientService.findByTelNumber(numero);
		if (p != null) {
			c.setPatient(p.getId());

			if (ViewMode.VIEW_MODE_CREATE.equals(viewMode))
				p.setPartenaire(p.getPartenaire());
			if (ViewMode.VIEW_MODE_EDIT.equals(viewMode))
				p.setPartenaire(Long.valueOf(idAssurreur));

			patientService.saveLogical(p, false);
		} else {
			p = new Patients();

			if (idAssurreur.isEmpty() || idAssurreur == null)
				p.setPartenaire(1L);
			else
				p.setPartenaire(Long.valueOf(idAssurreur.replace(" ", "")));

			// System.out.println("les variables envoyés sont tel:"+ telPatient
			// +"age
			// :"+agePatient);
			p.setTelephone(telPatient);
			p.setAge(Long.valueOf(agePatient.replace(" ", "")));
			p.setNomPrenom(c.getNomPrenomPatient());
			p.setIsDeleted(false);
			p.setWhenDone(new Timestamp(System.currentTimeMillis()));
			p.setDateNaissance(new Timestamp(System.currentTimeMillis()));
			p.setPoid(0L);

			patientService.saveLogical(p, true);

			c.setPatient(patientService.findByTelNumber(numero).getId());
		}

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		LocalDateTime dateV = null; // valeur par défaut
		boolean dateError = false;
		// System.out.println("le droit est :" +session("droit"));
		if ((viewMode.equals(ViewMode.VIEW_MODE_EDIT) && "Admin".equals(request.session().get("droit").get()))
				|| "Asistant".equals(request.session().get("droit").get())) {
			// if (viewMode.equals(ViewMode.VIEW_MODE_EDIT) &&
			// "Asistant".equals(request.session().get("droit").get()) ) {

			String dateRDV = formFactory.form().bindFromRequest(request).get("tmpDate");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			LocalDate localDate = LocalDate.parse(dateRDV, formatter);

			if (null != dateRDV && !dateRDV.trim().isEmpty()) {
				try {
					// System.out.println("la date est :"+dateV+tmpDate);

					dateV = LocalDateTime.of(localDate, LocalTime.of(0, 0));
					System.out.println("la date est :" + dateV);
				} catch (Exception e) {
					dateError = true;
				}
			}
		}

		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {
			String num_consul = String.valueOf(timestamp.getTime());
			c.setNumeroConsul(num_consul);
			if (c.getStructurePatient().isEmpty() || c.getStructurePatient() == null)
				c.setStructurePatient("personnel");

			c.setWhenDone(new Timestamp(System.currentTimeMillis()));
			c.setIsDeleted(false);
			c.setWhoDone(String.valueOf(request.session().get("login").get()));
			c.setIsClosed(false);

			// coutConsultation = (long)
			// (typeConServices.findById(c.getTypeConsultation()).getPrix()
			// - (typeConServices.findById(c.getTypeConsultation()).getPrix()
			// * partServices.findById(p.getPartenaire()).getTauxCouverture()));
			if (c.getTauxCouverture() == null) {
				coutConsultation = (long) (typeConServices.findById(c.getTypeConsultation()).getPrix());
				c.setTauxCouverture(0.00);
			} else
				coutConsultation = (long) (typeConServices.findById(c.getTypeConsultation()).getPrixPcm()
						- (typeConServices.findById(c.getTypeConsultation()).getPrixPcm() * c.getTauxCouverture()));

			c.setCout(coutConsultation);
			c.setMontantEnLettre(consultationServices.getMontantLettre(coutConsultation));
			// System.out.println("les montant recu et cout : " +
			// c.getSommeRecu() + " - " +
			// coutConsultation);
			c.setSommeRemise(c.getSommeRecu() - coutConsultation);

			if (c.getSommeRecu() < coutConsultation) {

				return redirect(routes.ConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Montant reçu de  " + c.getSommeRecu() + " non suffissant !!! ");
			}
			if (consultationServices.saveLogical(c, true).equals("ok")) {

				// System.out.println("Consultations : " + c);
				return redirect(routes.ConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Une Consultations pour " + c.getNomPrenomPatient() + " a été ajouté avec success");
			} else {

				return redirect(routes.ConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Consultations pour " + c.getNomPrenomPatient() + " non ajouter ");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			// a.setLogin(login);
			if (c.getTauxCouverture() == null) {
				coutConsultation = (long) (typeConServices.findById(c.getTypeConsultation()).getPrix());
				c.setTauxCouverture(0.0);
			} else
				coutConsultation = (long) (typeConServices.findById(c.getTypeConsultation()).getPrix()
						- (typeConServices.findById(c.getTypeConsultation()).getPrix() * c.getTauxCouverture()));

			c.setCout(coutConsultation);
			c.setMontantEnLettre(consultationServices.getMontantLettre(coutConsultation));
			// System.out.println("les montant recu et cout : " +
			// c.getSommeRecu() + " - " +
			// coutConsultation);
			c.setSommeRemise(c.getSommeRecu() - coutConsultation);
			if ("Admin".equals(String.valueOf(request.session().get("droit").get()))
					|| "Asistant".equals(String.valueOf(request.session().get("droit").get())))
				c.setDateRdv(Timestamp.valueOf(dateV));
			// c.setWhenDone(new Timestamp(System.currentTimeMillis()));
			String dateModif = formFactory.form().bindFromRequest(request).get("dateTmp3");
			c.setWhenDone(consultationServices.getDateT(dateModif));
			c.setWhoDone(String.valueOf(request.session().get("login").get()));
			c.setIsClosed(false);
			if (consultationServices.saveLogical(c, false).equals("ok")) {
				// flash("success", "Consultations modifier avec success");
				return redirect(routes.ConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Consultations  modifier avec success");
			} else {
				// flash("error", "Consultations non modifier");
				return redirect(routes.ConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Consultations  non modifier");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_DELETE)) {
//			c.setCout(-c.getCout());
//			c.setMontantEnLettre("Moins "+consultationServices.getMontantLettre(coutConsultation));
			c.setIsDeleted(true);
			c.setWhenDone(new Timestamp(System.currentTimeMillis()));
			c.setWhoDone(String.valueOf(request.session().get("login").get()));
			System.out.println("Numero consul :"+c.getNumeroConsul() +"############");
			
			
			if (consultationServices.saveLogical(c, false).equals("ok")) {
				// flash("success", "Consultations Supprimer avec success");
				
				return redirect(routes.ConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Consultations  Supprimer avec success");
			} else {
				// flash("error", "Consultations non supprimer");
				return redirect(routes.ConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Consultations  non supprimer");
			}
		}
		return redirect(routes.ConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}

	/**
	 * ajouter des ordonnances,renoi la vu des ordonnances
	 * 
	 * @param idConsul
	 * @return
	 */
	public Result addExam(Request request, Long idConsul) {

		return redirect(routes.ExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, idConsul));

	}

	/**
	 * ajouter des ordonnances,renoi la vu des ordonnances
	 * 
	 * @param idConsul
	 * @return
	 */
	public Result addOrdonnance(Request request, Long idConsul) {
		return redirect(routes.OrdonanceCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, idConsul));
	}

	public Result addSoin(Request request, Long idConsul) {
		return redirect(routes.SoinCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, idConsul));
	}

	/**
	 * @author nasser methode impression recu
	 */
	public Result print(Request request, String numConsultation, String fileName)
			throws IOException, InterruptedException {

		// envoyer les données de la facture pour certification
		// Controller si la facture n'est pas deja certifier pour ne pas la
		// certifier deux fois
		
		LocalDateTime now = LocalDateTime.now();
		String now_string = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
		String templateDir = new File("").getAbsolutePath() + "/reports/spool/";
		try {
			// flash("success", "impression ok");

			jasper.generateReport(fileName, numConsultation);

			return ok(new java.io.File(templateDir + fileName + "_" + now_string + "_" + numConsultation + ".pdf"))
					.flashing("success", "impression ok");

		} catch (Exception e) {
			// flash("error", "erreur impression");
			// System.out.println(e.getMessage() +
			// "+++++++--**///////++++++++");
			return redirect(routes.ConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
					"Erreur d'impression");
		}
	}

	public Result showSituationView(Request request) {
		return ok(views.html.rapports.render(request));
	}

	public Result rapportJour(Request request) throws IOException {
		String dateD = formFactory.form().bindFromRequest(request).get("tmpDate");
		consultationServices.backup();
		String fileName = "rapport_journalier";
		LocalDateTime now = LocalDateTime.now();
		String now_string = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		String templateDir = new File("").getAbsolutePath() + "/reports/spool/";
		try {
			// flash("success", "impression ok");

			jasper.generateReport(fileName, consultationServices.getDateT(dateD),
					String.valueOf(request.session().get("login").get()));

			return ok(new java.io.File(templateDir + fileName + "_" + now_string + "_" + "" + ".pdf"))
					.flashing("success", "impression ok");

		} catch (Exception e) {
			// flash("error", "erreur impression");
			System.out.println(e.getMessage() + "+++++++--**///////++++++++");
			return ok(views.html.rapports.render(request)).flashing("error", "erreur impression");
		}
	}

	public Result rapportPartenaire(Request request) {
		String dateD1 = formFactory.form().bindFromRequest(request).get("tmpDate");
		String dateD2 = formFactory.form().bindFromRequest(request).get("tmpDate2");
		String idAssurreur = formFactory.form().bindFromRequest(request).get("partenaireid");

		String fileName = "rapport_journalier_p";
		LocalDateTime now = LocalDateTime.now();
		String now_string = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		String templateDir = new File("").getAbsolutePath() + "/reports/spool/";
		try {
			// flash("success", "impression ok");

			jasper.generateReport(fileName, consultationServices.getDateT(dateD1),
					consultationServices.getDateT(dateD2), idAssurreur.replace(" ", ""));

			return ok(new java.io.File(templateDir + fileName + "_" + now_string + "_" + "" + ".pdf"))
					.flashing("success", "Impression OK");

		} catch (Exception e) {
			// flash("error", "erreur impression");
			System.out.println(e.getMessage() + "+++++++--**///////++++++++");
			return ok(views.html.rapports.render(request)).flashing("error", " Erreur d'impression");
		}
	}

	public Result rapportPeriode(Request request) {
		String dateD1 = formFactory.form().bindFromRequest(request).get("tmpDate");
		String dateD2 = formFactory.form().bindFromRequest(request).get("tmpDate2");

		String fileName = "rapport_betwene";
		LocalDateTime now = LocalDateTime.now();
		String now_string = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		String templateDir = new File("").getAbsolutePath() + "/reports/spool/";
		try {
			// flash("success", "impression ok");

			jasper.generateReport(fileName, consultationServices.getDateT(dateD1),
					consultationServices.getDateT(dateD2), "");

			return ok(new java.io.File(templateDir + fileName + "_" + now_string + "_" + "" + ".pdf"))
					.flashing("success", " Impression OK");

		} catch (Exception e) {
			// flash("error", "erreur impression");
			System.out.println(e.getMessage() + "+++++++--**///////++++++++");
			return ok(views.html.rapports.render(request)).flashing("error", " Erreur d'impression");
		}
	}

	public Result viewFilter(Request request) {
		String dateD1 = formFactory.form().bindFromRequest(request).get("tmpDate");
		// String d1 = null == dateD1 ? "0000-00-00" : dateD1;
		String dateD2 = formFactory.form().bindFromRequest(request).get("tmpDate2");
		// String d2 = null == dateD2 ? "0000-00-00" : dateD2;
		String numTel = formFactory.form().bindFromRequest(request).get("telPatient");
		VConsultations c = new VConsultations();
		List<VConsultations> vcons = new ArrayList<>();
		if ((dateD1.isEmpty() || dateD1 == null) && (dateD2.isEmpty() || dateD2 == null))
			vcons = consultationServices.listeConsultationsFilter(numTel);
		else
			vcons = consultationServices.listeConsultationsFilter(consultationServices.getDateT(dateD1),
					consultationServices.getDateT(dateD2), numTel);

		return ok(views.html.consultations.render(ViewMode.VIEW_MODE_CREATE, vcons, typeConServices.findAll(),
				persServices.listes("Medecin"), c, request));
	}

	public Result rdv(Request request) {

		String dateD2 = formFactory.form().bindFromRequest(request).get("tmpDate2");
		// String d2 = null == dateD2 ? "0000-00-00" : dateD2;
		List<VConsultations> vcons = new ArrayList<>();
		if ((dateD2.isEmpty() || dateD2 == null))
			vcons = null;
		else
			vcons = consultationServices.listeRDV(consultationServices.getDateT(dateD2));

		return ok(views.html.rdv_consultations.render(ViewMode.VIEW_MODE_CREATE, vcons,
				rdvService.listeRDV(consultationServices.getDateT(dateD2)), request));
	}

	public Result viewRDV(Request request) {
		List<VConsultations> vcons = new ArrayList<>();
		List<Rendezvous> vrdv = new ArrayList<>();
		return ok(views.html.rdv_consultations.render(ViewMode.VIEW_MODE_CREATE, vcons, vrdv, request));
	}
}
