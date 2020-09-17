package controllers;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.inject.Inject;


import utils.CallJasperReport;
import utils.Secured;
import utils.ViewMode;
import models.public_.tables.pojos.Personnels;
import models.public_.tables.pojos.Users;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Security;
import services.PersonnelMainServices;

/**
 * 
 * @author nasser
 *
 */

@Security.Authenticated(Secured.class)
public class PersonnelCtrl extends Controller {

	FormFactory formFactory;
	PersonnelMainServices personnelServices;
	private final CallJasperReport jasper;

	@Inject
	public PersonnelCtrl(FormFactory formFactory, PersonnelMainServices personnelServices, CallJasperReport jasper) {
		super();
		this.formFactory = formFactory;
		this.personnelServices = personnelServices;
		this.jasper = jasper;
	}

	public Result show(String subAction, Long idPersonnel, Request request) {

//		if (!isAdmin()) {
//			return redirect(routes.AuthenticationCtrl.logout());
//		}
		String viewMode;
		Personnels p;
		List<Personnels> personnels = personnelServices.findAll();
		if (0 == idPersonnel) {
			p = new Personnels();
			viewMode = ViewMode.VIEW_MODE_CREATE;
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			p = personnelServices.findById(idPersonnel);
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			p = personnelServices.findById(idPersonnel);
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
			p = personnelServices.findById(idPersonnel);

		}
		return ok(views.html.personels.render(viewMode, personnels, p, request));

	}

	public Result save(Request request) {
//		if (!isAdmin() || !isLogisticien()) {
//			return redirect(routes.AuthenticationCtrl.logout());
//		}
		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");
		// final String login = formFactory.form().bindFromRequest().get("log");
		Form<Personnels> uForm = formFactory.form(Personnels.class).bindFromRequest(request);
		Personnels p = uForm.get();
//        if(personnelServices.isUserExist(a.getLogin())){
//            flash("error", "utilisateur avec login "+a.getLogin()+" existe déja ");
//            return redirect(routes.UserCtrl.show(ViewHelper.VIEW_MODE_CREATE, ""));
//        }
		p.setWhenDone(new Timestamp(System.currentTimeMillis()));
		p.setIsDeleted(false);

		// p.setWhoDone(session("login"));
		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {

			if (personnelServices.saveLogical(p, true).equals("ok")) {

				// System.out.println("personne : "+p);
				return redirect(routes.PersonnelCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success","Agent " + p.getNomP() + " ajouter avec success");
			} else {

				return redirect(routes.PersonnelCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"utilisateur " + p.getNomP() + " non ajouter ");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			// a.setLogin(login);
			if (personnelServices.saveLogical(p, false).equals("ok")) {

				return redirect(routes.PersonnelCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Agent  modifier avec success");
			} else {

				return redirect(routes.PersonnelCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Agent  non modifier");
			}
		}
		return redirect(routes.PersonnelCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}

	public Result printListePersonnel(Request request) {

		String fileName = "Personnels";
		LocalDateTime now = LocalDateTime.now();
		String now_string = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		String templateDir = new File("").getAbsolutePath() + "/reports/spool/";
		try {
		

			jasper.generateReport(fileName, "");

			return ok(new java.io.File(templateDir + fileName + "_" + now_string + "_" + ".pdf")).flashing("success", "impression ok");

		} catch (Exception e) {
			
			// System.out.println(e.getMessage() + "+++++++--**///////++++++++");
			return redirect(routes.PersonnelCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error", "erreur impression");
		}
	}

	public Result printSituationPersonnelAll(Request request) {

		String fileName = "situation_chauffeur";
		LocalDateTime now = LocalDateTime.now();
		String now_string = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		String templateDir = new File("").getAbsolutePath() + "/reports/spool/";
		try {
			

			jasper.generateReport(fileName, "");

			return ok(new java.io.File(templateDir + fileName + "_" + now_string + "_" + "" + ".pdf")).flashing("success", "impression ok");

		} catch (Exception e) {
			
			// System.out.println(e.getMessage() + "+++++++--**///////++++++++");
			return redirect(routes.PersonnelCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error", "erreur impression");
		}
	}

}
