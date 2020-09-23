package controllers;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.Years;

import models.public_.tables.pojos.Patients;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Security;
import services.PatientsMainServices;
//import utils.Commons;
import utils.Secured;
import utils.ViewMode;

/**
 * 
 * @author nasser
 *
 */
@Security.Authenticated(Secured.class)
public class PatientCtrl extends Controller {

	FormFactory formFactory;
	PatientsMainServices patientServices;

	//public static Commons common;

	@Inject
	public PatientCtrl(FormFactory formFactory, PatientsMainServices patientServices) {
		super();
		this.formFactory = formFactory;
		this.patientServices = patientServices;
	}

	public Result show(String subAction, Long idPart, Request request) {

//		if (!isAdmin()) {
//			return redirect(routes.AuthenticationCtrl.logout());
//		}
		String viewMode;
		Patients c;
		List<Patients> patients = patientServices.findAll();
		List<Patients> patientsDeleted = patientServices.findPatientsupprimer();
		if (0 == idPart) {
			c = new Patients();
			viewMode = ViewMode.VIEW_MODE_CREATE;
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			c = patientServices.findById(idPart);
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			c = patientServices.findById(idPart);
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
			c = patientServices.findById(idPart);

		}
		return ok(views.html.patients.render(viewMode, patients, patientsDeleted, c, request));

	}

	public Result restaure(Long idPart, Request request) {
		Patients c = patientServices.findById(idPart);
		c.setIsDeleted(false);
		patientServices.update(c);
		return redirect(routes.PatientCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}

	public Result save(Request request) {

		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");
		LocalDateTime dateV = null; // valeur par défaut
		boolean dateError = false;

		Form<Patients> uForm = formFactory.form(Patients.class).bindFromRequest(request);

		String partenaire = formFactory.form().bindFromRequest(request).get("partenaire");
		String dateNaisse = formFactory.form().bindFromRequest(request).get("tmpDate");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		LocalDate localDate = LocalDate.parse(dateNaisse, formatter);

		if (null != dateNaisse && !dateNaisse.trim().isEmpty()) {
			try {
				// System.out.println("la date est :"+dateV+tmpDate);

				dateV = LocalDateTime.of(localDate, LocalTime.of(0, 0));

			} catch (Exception e) {
				dateError = true;
			}
		}

		// String partenaire = formFactory.form().bindFromRequest().get("partenaireid");

//		System.out.println("la date saisie est :" + dateNaisse + "date convertie :" + Timestamp.valueOf(dateV)
//				+ " le partenaire :" + Long.valueOf(partenaire));
//		//int yearsCount = Years.yearsBetween(Timestamp.valueOf(dateV) ,new Timestamp(System.currentTimeMillis())).getYears();
		Patients c = uForm.get();
		c.setWhenDone(new Timestamp(System.currentTimeMillis()));
		c.setIsDeleted(false);
		c.setWhoDone(String.valueOf(request.session().get("login").get()));

		c.setDateNaissance(Timestamp.valueOf(dateV));
		c.setAge(
				Long.valueOf(new Timestamp(System.currentTimeMillis()).getYear() - Timestamp.valueOf(dateV).getYear()));

		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {
			c.setPartenaire(Long.valueOf(partenaire));
			if (patientServices.saveLogical(c, true).equals("ok")) {

				// System.out.println("Patient : " + c);
				return redirect(routes.PatientCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Patient " + c.getNomPrenom() + " ajouter avec success");
			} else {

				return redirect(routes.PatientCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Patient " + c.getNomPrenom() + " non ajouter ");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			// a.setLogin(login);
			if (patientServices.saveLogical(c, false).equals("ok")) {

				return redirect(routes.PatientCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Patient  modifier avec success");
			} else {

				return redirect(routes.PatientCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Patient  non modifier");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_DELETE)) {
			c.setIsDeleted(true);
			if (patientServices.saveLogical(c, false).equals("ok")) {

				return redirect(routes.PatientCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Patient  Supprimer avec success");
			} else {

				return redirect(routes.PatientCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Patient  non supprimer");
			}
		}
		return redirect(routes.PatientCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}

}
