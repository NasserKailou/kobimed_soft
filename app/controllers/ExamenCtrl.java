package controllers;

import java.sql.Timestamp;

import javax.inject.Inject;

import models.public_.tables.pojos.Examens;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Security;
import services.ConsultationsMainServices;
import services.ExamenMainServices;
import utils.Secured;
import utils.ViewMode;

/**
 * 
 * @author nasser
 *
 */
@Security.Authenticated(Secured.class)
public class ExamenCtrl extends Controller {

	FormFactory formFactory;
	ExamenMainServices examenService;
	ConsultationsMainServices consServices;

	@Inject
	public ExamenCtrl(FormFactory formFactory, ExamenMainServices examenService,
			ConsultationsMainServices consServices) {
		super();
		this.formFactory = formFactory;
		this.examenService = examenService;
		this.consServices = consServices;
	}

	public Result show(String subAction, Long idPart, Long idConsultation,Request request) {

//		if (!isAdmin()) {
//			return redirect(routes.AuthenticationCtrl.logout());
//		}
		String viewMode;
		Examens c;

		if (0 == idPart) {
			c = new Examens();
			viewMode = ViewMode.VIEW_MODE_CREATE;
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			c = examenService.findById(idPart);
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_TRAITE.equals(subAction)) {
			c = examenService.findById(idPart);
			viewMode = ViewMode.VIEW_MODE_TRAITE;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			c = examenService.findById(idPart);
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
			c = examenService.findById(idPart);

		}
		return ok(views.html.examens.render(viewMode, examenService.findExamensByConsultation(idConsultation),
				examenService.findExamenSupprimerByConsultation(idConsultation), c,
				consServices.findById(idConsultation),request));

	}

	public Result restaure(Long idPart,Request request) {
		Examens c = examenService.findById(idPart);
		c.setIsDeleted(false);
		examenService.update(c);
		return redirect(routes.ExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation()));
	}

	public Result save(Request request) {

		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");

		Form<Examens> uForm = formFactory.form(Examens.class).bindFromRequest(request);
		Examens c = uForm.get();
		c.setWhenDone(new Timestamp(System.currentTimeMillis()));
		c.setIsDeleted(false);
		c.setWhoDone(String.valueOf(request.session().get("login")));

		if (c.getCoutExamen() == null)
			c.setCoutExamen(0L);
//		if (!consServices.existsById(c.getConsultation())) {
//			c.setConsultation(0L);
//			
//		}
		// if(c.getResultat() == null || c.getResultat().isEmpty()) c.setResultat("en
		// attente de résultat");
		// System.out.println("Id Consultation :"+ c.getConsultation());
		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {

			if (examenService.saveLogical(c, true).equals("ok")) {
				//flash("success", " Examen " + c.getLibelle() + " ajouter avec success");
				// System.out.println("Examens : " + c);
				return redirect(routes.ExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation())).flashing("success", " Examen " + c.getLibelle() + " ajouter avec success");
			} else {
				System.out.println("msg :" + examenService.saveLogical(c, true));
				//flash("error", " Examen " + c.getLibelle() + " non ajouter ");
				return redirect(routes.ExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation())).flashing("error", " Examen " + c.getLibelle() + " non ajouter ");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			// a.setLogin(login);
			if (examenService.saveLogical(c, false).equals("ok")) {
				//flash("success", " Examen  modifier avec success");
				return redirect(routes.ExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation())).flashing("success", " Examen  modifier avec success");
			} else {
				//flash("error", "Examens  non modifier");
				return redirect(routes.ExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation())).flashing("error", "Examens  non modifier");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_TRAITE)) {
			// a.setLogin(login);
			c.setObservation(c.getObservation().trim());
			if (examenService.saveLogical(c, false).equals("ok")) {
				
				return redirect(routes.ExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation())).flashing("success", " Examen  traiter avec success");
			} else {
				//flash("error", "Echec lors du traitement de l'examen");
				return redirect(routes.ExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation())).flashing("error", "Echec lors du traitement de l'examen");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_DELETE)) {
			c.setIsDeleted(true);
			if (examenService.saveLogical(c, false).equals("ok")) {
				//flash("success", " Examen  Supprimer avec success");
				return redirect(routes.ExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation())).flashing("success", " Examen  Supprimer avec success");
			} else {
				
				return redirect(routes.ExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation())).flashing("error", "Ligne Examen  non supprimer");
			}
		}
		return redirect(routes.ExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation()));
	}

}
