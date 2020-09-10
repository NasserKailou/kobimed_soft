/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import com.google.inject.Inject;
import java.util.List;
import models.public_.Tables;
import models.public_.tables.daos.UsersDao;
import models.public_.tables.pojos.Users;

/**
 * 
 * @author nasser
 *
 */
public class UserService extends UsersDao implements IUser {

	private final ConnectionHelper con;

	@Inject
	public UserService(ConnectionHelper con) {
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	@Override
	public String saveLogical(Users u, boolean b) {
		try {
			if (b)
				super.insert(u);
			else
				super.update(u);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public String deleteUser(String login) {
		try {
			super.delete(getUserByLogin(login));
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}

	}

	@Override
	public String ChangeEtat(String login, boolean etat) {
		Users u = getUserByLogin(login);
		u.setEtat(etat);
		try {
			super.update(u);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public List<Users> getAllUser() {
		return super.findAll();
	}

	@Override
	public Users getUserByLogin(String login) {
		return con.connection().selectFrom(Tables.USERS).where(Tables.USERS.LOGIN.eq(login)).fetchOneInto(Users.class);
	}

	@Override
	public Users authentification(String login, String password) {

		return con.connection().selectFrom(Tables.USERS).where(Tables.USERS.LOGIN.eq(login))
				.and(Tables.USERS.PASSE.eq(password)).and(Tables.USERS.ETAT.eq(true)).fetchOneInto(Users.class);

	}

	@Override
	public boolean isUserExist(String libelle) {
		List<Users> users = super.fetchByLogin(libelle);
		return users.size() > 0;
	}

}
