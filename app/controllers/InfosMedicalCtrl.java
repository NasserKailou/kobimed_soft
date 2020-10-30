package controllers;

import java.sql.Timestamp;
import java.util.List;

import javax.inject.Inject;

import models.public_.tables.pojos.InfosMedicale;
import models.public_.tables.pojos.Partenaire;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.Http.Request;
import services.InfosMedicalMainServices;
import services.PatientsMainServices;
import utils.Secured;
import utils.ViewMode;

/**
 * 
 * @author nasser
 *
 */
@Security.Authenticated(Secured.class)
public class InfosMedicalCtrl extends Controller {
	FormFactory formFactory;
	InfosMedicalMainServices infoServices;
	PatientsMainServices patienService;

	@Inject
	public InfosMedicalCtrl(FormFactory formFactory, InfosMedicalMainServices infoServices,
			PatientsMainServices patienService) {
		super();
		this.formFactory = formFactory;
		this.infoServices = infoServices;
		this.patienService = patienService;
	}

	public Result show(String subAction, Long idInfo, Long idPatient, Request request) {

		String viewMode;
		InfosMedicale c;
		List<InfosMedicale> infos = infoServices.findAllByPatient(idPatient);

		if (0 == idInfo) {
			c = new InfosMedicale();
			viewMode = ViewMode.VIEW_MODE_CREATE;
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			c = infoServices.findById(idInfo);
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			c = infoServices.findById(idInfo);
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
			c = infoServices.findById(idInfo);

		}
		return ok(views.html.infoMedicales.render(viewMode, infos, patienService.findById(idPatient), c, request));

	}

	public Result save(Request request) {

		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");

		Form<InfosMedicale> uForm = formFactory.form(InfosMedicale.class).bindFromRequest(request);
		InfosMedicale c = uForm.get();
		c.setWhenDone(new Timestamp(System.currentTimeMillis()));
		c.setIsDeleted(false);
		c.setWhoDone(String.valueOf(request.session().get("login").get()));

		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {

			if (infoServices.saveLogical(c, true).equals("ok")) {

				return redirect(routes.InfosMedicalCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getPatient()))
						.flashing("success", " Info " + c.getLibelle() + " ajouter avec success");
			} else {
				System.out.println("msg :" + infoServices.saveLogical(c, true));
				// flash("error", " Info " + c.getLibelle() + " non ajouter ");
				return redirect(routes.InfosMedicalCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getPatient()))
						.flashing("error", " Info " + c.getLibelle() + " non ajouter ");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			// a.setLogin(login);
			if (infoServices.saveLogical(c, false).equals("ok")) {
				// flash("success", " Info modifier avec success");
				return redirect(routes.InfosMedicalCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getPatient()))
						.flashing("success", " Info  modifier avec success");
			} else {
				// flash("error", "Infos non modifier");
				return redirect(routes.InfosMedicalCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getPatient()))
						.flashing("error", "Infos  non modifier");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_DELETE)) {
			//c.setIsDeleted(true);
			if (infoServices.deletedInfo(infoServices.findById(c.getId())).equals("ok")) {
				// flash("success", " Info Supprimer avec success");
				return redirect(routes.InfosMedicalCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getPatient()))
						.flashing("success", " Info  Supprimer avec success");
			} else {

				return redirect(routes.InfosMedicalCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getPatient()))
						.flashing("error", "Ligne Info  non supprimer");
			}
		}
		return redirect(routes.InfosMedicalCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getPatient()));
	}
}
