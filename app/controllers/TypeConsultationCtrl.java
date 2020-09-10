package controllers;

import java.sql.Timestamp;

import javax.inject.Inject;

import models.public_.tables.pojos.TypeConsultation;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Security;
import services.TypeConsultationsMainServices;
import utils.Secured;
import utils.ViewMode;

/**
 * 
 * @author nasser
 *
 */
@Security.Authenticated(Secured.class)
public class TypeConsultationCtrl extends Controller {

	FormFactory formFactory;
	TypeConsultationsMainServices typeConsultationService;

	@Inject
	public TypeConsultationCtrl(FormFactory formFactory, TypeConsultationsMainServices typeConsultationService) {
		super();
		this.formFactory = formFactory;
		this.typeConsultationService = typeConsultationService;
	}

	public Result show(String subAction, Long idTypeCons, Request request) {

//		if (!isAdmin()) {
//			return redirect(routes.AuthenticationCtrl.logout());
//		}
		String viewMode;
		TypeConsultation c;

		if (0 == idTypeCons) {
			c = new TypeConsultation();
			viewMode = ViewMode.VIEW_MODE_CREATE;
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			c = typeConsultationService.findById(idTypeCons);
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			c = typeConsultationService.findById(idTypeCons);
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
			c = typeConsultationService.findById(idTypeCons);

		}
		return ok(views.html.typeConsultation.render(viewMode, typeConsultationService.listeTypeConsultation(),
				typeConsultationService.findTypeConsultationupprimer(), c, request));

	}

	public Result restaure(Long idTypeCons, Request request) {
		TypeConsultation c = typeConsultationService.findById(idTypeCons);
		c.setIsDeleted(false);
		typeConsultationService.update(c);
		return redirect(routes.TypeConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}

	public Result save(Request request) {

		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");

		Form<TypeConsultation> uForm = formFactory.form(TypeConsultation.class).bindFromRequest(request);
		TypeConsultation c = uForm.get();
		c.setWhenDone(new Timestamp(System.currentTimeMillis()));
		c.setIsDeleted(false);
		c.setWhoDone(String.valueOf( request.session().get("login")));

		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {

			if (typeConsultationService.saveLogical(c, true).equals("ok")) {

				// System.out.println("TypeConsultation : " + c);
				return redirect(routes.TypeConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L))
						.flashing("success", "Type de consultation " + c.getLibelle() + " ajouter avec success");
			} else {

				return redirect(routes.TypeConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L))
						.flashing("error", "Type de consultation " + c.getLibelle() + " non ajouter ");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			// a.setLogin(login);
			if (typeConsultationService.saveLogical(c, false).equals("ok")) {

				return redirect(routes.TypeConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L))
						.flashing("success", "Type de consultation  modifier avec success");
			} else {

				return redirect(routes.TypeConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L))
						.flashing("error", "TypeConsultation  non modifier");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_DELETE)) {
			c.setIsDeleted(true);
			if (typeConsultationService.saveLogical(c, false).equals("ok")) {

				return redirect(routes.TypeConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L))
						.flashing("success", "Type de consultation  Supprimer avec success");
			} else {

				return redirect(routes.TypeConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L))
						.flashing("error", "Type de consultation  non supprimer");
			}
		}
		return redirect(routes.TypeConsultationCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}
}
