package managebean;

import entities.Products;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Comparator;
import sessions.ProductsFacadeLocal;

@Named("productClientMB")
@SessionScoped
public class ProductClientMB implements Serializable {

    @EJB
    private ProductsFacadeLocal productsFacade;
    private String searchByKeyword;
    private String keyword;
    private List<Products> searchResults;
    private List<Products> search;
    private List<Products> nikeProducts;
    private List<Products> adidasProducts;
    private List<Products> converseProducts;
    private List<Products> pumaProducts;
    private List<Products> jordanProducts;
    private List<Products> casualProducts;
    private List<Products> sportProducts;
    private List<Products> trainingProducts;
    private List<Products> runningProducts;
    private List<Products> sneakerProducts;
    
    private String sortOption = "A-Z";
    private String searchKeyword;
    public String search() {
        searchResults = productsFacade.searchByKeyword(searchKeyword);
        return "search?faces-redirect=true";
    }
    
    @PostConstruct
    public void init() {
        nikeProducts = productsFacade.getProductsByBrand("Nike");
        adidasProducts = productsFacade.getProductsByBrand("Adidas");
        converseProducts = productsFacade.getProductsByBrand("Converse");
        pumaProducts = productsFacade.getProductsByBrand("Puma");
        jordanProducts = productsFacade.getProductsByBrand("Jordan");
        sortProducts();
        casualProducts = productsFacade.getProductsByCategory("Casual");
        sportProducts = productsFacade.getProductsByCategory("Sport");
        trainingProducts = productsFacade.getProductsByCategory("Training");
        runningProducts = productsFacade.getProductsByCategory("Running");
        sneakerProducts = productsFacade.getProductsByCategory("Sneaker");
    }
    
    private void sortProducts() {
        if (nikeProducts == null) return;

        switch (sortOption) {
            case "A-Z":
                nikeProducts.sort(Comparator.comparing(Products::getProductName));
                break;
            case "Z-A":
                nikeProducts.sort(Comparator.comparing(Products::getProductName).reversed());
                break;
            case "Low-High":
                nikeProducts.sort(Comparator.comparing(Products::getPrice));
                break;
            case "High-Low":
                nikeProducts.sort(Comparator.comparing(Products::getPrice).reversed());
                break;
        }
    }

    public List<Products> getCasualProducts() {
        return casualProducts;
    }
    public List<Products> getSportProducts() {
        return sportProducts;
    }public List<Products> getTrainingProducts() {
        return trainingProducts;
    }public List<Products> getRunningProducts() {
        return runningProducts;
    }public List<Products> getSneakerProducts() {
        return sneakerProducts;
    }
    public List<Products> getPumaProducts() {
        return pumaProducts;
    }
    public List<Products> getJordanProducts() {
        return jordanProducts;
    }
    public List<Products> getNikeProducts() {
        return nikeProducts;
    }
    public List<Products> getAdidasProducts() {
        return adidasProducts;
    }
    public List<Products> getConverseProducts() {
        return converseProducts;
    }
    public String getSortOption() {
        return sortOption;
    }

    public void setSortOption(String sortOption) {
        this.sortOption = sortOption;
        sortProducts(); 
    }

    public void setSearch(List search) {
        this.search = search;
    }

    public List<Products> getSearch() {
        return searchResults;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<Products> getSearchResults() {
        return searchResults;
    }

    public String getSearchByKeyword() {
        return searchByKeyword;
    }

    public void setSearchByKeyword(String searchByKeyword) {
        this.searchByKeyword = searchByKeyword;
    }

    public String searchByKeyword() {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }

        searchResults = productsFacade.searchByKeyword(keyword.trim());
        return "/client/search.xhtml?faces-redirect=true&keyword=" + keyword.trim();
    }

    public void loadSearchFromURL() {
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchResults = productsFacade.searchByKeyword(keyword.trim());
        }
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }
}
