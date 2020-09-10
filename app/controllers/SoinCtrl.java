package controllers;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.inject.Inject;

import models.public_.tables.pojos.Ordonances;
import models.public_.tables.pojos.Partenaire;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Security;
import services.ConsultationsMainServices;
import services.OrdonanceMainServices;
import utils.Secured;
import utils.ViewMode;

/**
 * 
 * @author nasser
 *
 */
@Security.Authenticated(Secured.class)
public class SoinCtrl extends Controller {

	FormFactory formFactory;
	OrdonanceMainServices ordServices;
	ConsultationsMainServices consServices;

	@Inject
	public SoinCtrl(FormFactory formFactory, OrdonanceMainServices ordServices,
			ConsultationsMainServices consServices) {
		super();
		this.formFactory = formFactory;
		this.ordServices = ordServices;
		this.consServices = consServices;
	}

	public Result show(String subAction, Long idPart, Long idConsultation, Request request) {

//		if (!isAdmin()) {
//			return redirect(routes.AuthenticationCtrl.logout());
//		}
		String viewMode;
		Ordonances c;

		if (0 == idPart) {
			c = new Ordonances();
			viewMode = ViewMode.VIEW_MODE_CREATE;
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			c = ordServices.findById(idPart);
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			c = ordServices.findById(idPart);
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
			c = ordServices.findById(idPart);

		}
		return ok(views.html.soins.render(viewMode, ordServices.findSoinByConsultation(idConsultation),
				ordServices.findOrdonanceSupprimerByConsultation(idConsultation), c,
				consServices.findById(idConsultation), request));

	}

	public Result restaure(Long idPart, Request request) {
		Ordonances c = ordServices.findById(idPart);
		c.setIsDeleted(false);
		ordServices.update(c);
		return redirect(routes.SoinCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation()));
	}

	public Result save(Request request) {

		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");

		Form<Ordonances> uForm = formFactory.form(Ordonances.class).bindFromRequest(request);
		Ordonances c = uForm.get();

		LocalDateTime dateV = null; // valeur par défaut
		boolean dateError = false;

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		String dateSoin = formFactory.form().bindFromRequest(request).get("tmpDate");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		LocalDate localDate = LocalDate.parse(dateSoin, formatter);

		if (null != dateSoin && !dateSoin.trim().isEmpty()) {
			try {
				// System.out.println("la date est :"+dateV+tmpDate);

				dateV = LocalDateTime.of(localDate, LocalTime.of(0, 0));

			} catch (Exception e) {
				dateError = true;
			}
		}
		if (dateV == null)
			c.setWhenDone(new Timestamp(System.currentTimeMillis()));
		else
			c.setWhenDone(Timestamp.valueOf(dateV));

		c.setIsDeleted(false);
		c.setWhoDone(String.valueOf(request.session().get("login")));
		c.setIsSoin(true);

		if (c.getMontant() == null)
			c.setMontant(0L);

		if (c.getNombre() == null)
			c.setNombre(1L);

		System.out.println("Id Consultation :" + c.getConsultation());
		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {
			c.setTotal(c.getNombre() * c.getMontant());
			if (ordServices.saveLogical(c, true).equals("ok")) {

				// System.out.println("Ordonances : " + c);
				return redirect(routes.SoinCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation()))
						.flashing("success", "Soin " + c.getLibelle() + " ajouter avec success");
			} else {

				return redirect(routes.SoinCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation()))
						.flashing("error", "Soin " + c.getLibelle() + " non ajouter ");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			// a.setLogin(login);
			c.setTotal(c.getNombre() * c.getMontant());
			if (ordServices.saveLogical(c, false).equals("ok")) {

				return redirect(routes.SoinCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation()))
						.flashing("success", "Soin  modifier avec success");
			} else {

				return redirect(routes.SoinCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation()))
						.flashing("error", "Ordonances  non modifier");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_DELETE)) {
			c.setIsDeleted(true);
			c.setMontant(c.getMontant());
			if (ordServices.saveLogical(c, false).equals("ok")) {

				return redirect(routes.SoinCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation()))
						.flashing("success", "Soin  Supprimer avec success");
			} else {

				return redirect(routes.SoinCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation()))
						.flashing("error", "Soin  non supprimer");
			}
		}
		return redirect(routes.SoinCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L, c.getConsultation()));
	}
}
