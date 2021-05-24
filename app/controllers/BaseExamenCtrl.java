package controllers;

import java.sql.Timestamp;

import javax.inject.Inject;

import models.public_.tables.pojos.BasExamens;
import models.public_.tables.pojos.TypeConsultation;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Security;
import services.BaseExamenServices;
import services.TypeConsultationsMainServices;
import utils.Secured;
import utils.ViewMode;

/**
 * 
 * @author nasser
 *
 */
@Security.Authenticated(Secured.class)
public class BaseExamenCtrl extends Controller {

	FormFactory formFactory;
	BaseExamenServices examenServices;

	@Inject
	public BaseExamenCtrl(FormFactory formFactory, BaseExamenServices examenServices) {
		super();
		this.formFactory = formFactory;
		this.examenServices = examenServices;
	}

	public Result show(String subAction, Long idTypeCons, Request request) {

//		if (!isAdmin()) {
//			return redirect(routes.AuthenticationCtrl.logout());
//		}
		String viewMode;
		BasExamens c;

		if (0 == idTypeCons) {
			c = new BasExamens();
			viewMode = ViewMode.VIEW_MODE_CREATE;
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			c = examenServices.findById(idTypeCons);
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			c = examenServices.findById(idTypeCons);
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
			c = examenServices.findById(idTypeCons);

		}
		return ok(views.html.baseExamen.render(viewMode, examenServices.findAll(), c, request));

	}

	public Result restaure(Long idTypeCons, Request request) {
		BasExamens c = examenServices.findById(idTypeCons);
		c.setIsDeleted(false);
		examenServices.update(c);
		return redirect(routes.BaseExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}

	public Result save(Request request) {

		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");

		Form<BasExamens> uForm = formFactory.form(BasExamens.class).bindFromRequest(request);
		BasExamens c = uForm.get();
		c.setWhenDone(new Timestamp(System.currentTimeMillis()));
		c.setIsDeleted(false);
		c.setWhoDone(String.valueOf(request.session().get("login").get()));

		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {

			if (examenServices.saveLogical(c, true).equals("ok")) {

				// System.out.println("TypeConsultation : " + c);
				return redirect(routes.BaseExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Examen " + c.getLibelle() + " ajouter avec success");
			} else {

				return redirect(routes.BaseExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Examen " + c.getLibelle() + " non ajouter ");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			// a.setLogin(login);
			if (examenServices.saveLogical(c, false).equals("ok")) {

				return redirect(routes.BaseExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Examen  modifier avec success");
			} else {

				return redirect(routes.BaseExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"TypeConsultation  non modifier");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_DELETE)) {
			c.setIsDeleted(true);
			if (examenServices.saveLogical(c, false).equals("ok")) {

				return redirect(routes.BaseExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Examen  Supprimer avec success");
			} else {

				return redirect(routes.BaseExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Examen  non supprimer");
			}
		}
		return redirect(routes.BaseExamenCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}
}
