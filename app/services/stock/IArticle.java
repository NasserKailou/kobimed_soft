/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.stock;

import java.util.List;
import models.stock.tables.pojos.Article;
import models.stock.tables.pojos.VArticle;

/**
 * 
 * @author nasser
 *
 */
public interface IArticle {
    public String saveLogical(Article a, boolean b);
    public List<VArticle> getAllArticle();
    public List<VArticle> getArticleByOwner(String owner);
    public List<VArticle> getAllArticleToBeTransfert();
    public VArticle findArticleById(Long id);
    public String deleteArticle(Long id);
    public boolean isArticleExiste(String libelle);
    
}
