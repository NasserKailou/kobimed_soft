/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.stock;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import models.stock.Tables;
import models.stock.tables.daos.ArticleDao;
import models.stock.tables.pojos.Article;
import models.stock.tables.pojos.VArticle;
import services.ConnectionHelper;

/**
 *
 * @author alpariss
 */
public class ArticleService extends ArticleDao implements IArticle {

	private final ConnectionHelper con;

	@Inject
	public ArticleService(ConnectionHelper con) {
		this.con = con;
		this.setConfiguration(con.connection().configuration());
	}

	@Override
	public String saveLogical(Article a, boolean b) {
		try {
			if (b) {
				super.insert(a);
			} else {
				super.update(a);
			}
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public List<VArticle> getAllArticle() {
		return con.connection().selectFrom(Tables.V_ARTICLE).fetchInto(VArticle.class);
	}

	@Override
	public VArticle findArticleById(Long id) {
		return con.connection().selectFrom(Tables.V_ARTICLE).where(Tables.V_ARTICLE.ID.eq(id))
				.fetchOneInto(VArticle.class);
	}

	@Override
	public String deleteArticle(Long id) {
		Article a = super.fetchOneById(id);
		a.setIsDeleted(true);
		try {
			super.update(a);
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public boolean isArticleExiste(String libelle) {
		List<Article> articles = super.fetchByLibelle(libelle);
		return articles.size() > 0;
	}

	@Override
	public List<VArticle> getAllArticleToBeTransfert() {
		return con.connection().selectFrom(Tables.V_ARTICLE).where(Tables.V_ARTICLE.BE_TRANSFERT.eq(true)).fetchInto(VArticle.class);
	}

	@Override
	public List<VArticle> getArticleByOwner(String owner) {
		List<VArticle> listes = new ArrayList<VArticle>();
		
		listes = con.connection().selectFrom(Tables.V_ARTICLE).where(Tables.V_ARTICLE.WHO_DONE.eq(owner))
				.fetchInto(VArticle.class);
		con.connection().close();
		
		return listes;
	}

}
