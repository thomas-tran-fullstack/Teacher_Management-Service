package managebean;

import entities.Brands;
import entities.Products;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import sessions.BrandsFacadeLocal;
import sessions.ProductsFacadeLocal;

@Named("brandMB")
@SessionScoped
public class BrandMB implements Serializable {

    @EJB
    private BrandsFacadeLocal brandsFacade;
    @EJB
    private ProductsFacadeLocal productsFacade;

    private Brands selectedBrand;
    private List<Brands> brandList;
    private String searchKeyword;

    @PostConstruct
    public void init() {
        brandList = brandsFacade.findAll();
        selectedBrand = new Brands();
    }

    public Brands getSelectedBrand() {
        return selectedBrand;
    }

    public void setSelectedBrand(Brands selectedBrand) {
        this.selectedBrand = selectedBrand;
    }

    public List<Brands> getBrandList() {
        return brandList;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    // ====== ACTIONS ======
    public void confirm() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (selectedBrand == null || selectedBrand.getBrandName() == null || selectedBrand.getBrandName().trim().isEmpty()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Brand name is required.", null));
            return;
        }

        // check duplicate
        boolean exists = brandList.stream()
                .anyMatch(b -> b.getBrandName().equalsIgnoreCase(selectedBrand.getBrandName())
                && (selectedBrand.getBrandID() == null || !b.getBrandID().equals(selectedBrand.getBrandID())));

        if (exists) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Brand name already exists.", null));
            return;
        }

        try {
            if (selectedBrand.getBrandID() == null) {
                // ADD
                brandsFacade.create(selectedBrand);
                ctx.addMessage(null, new FacesMessage("Brand added successfully."));
            } else {
                Brands existing = brandsFacade.find(selectedBrand.getBrandID());
                if (existing != null) {
                    existing.setBrandName(selectedBrand.getBrandName());
                    brandsFacade.edit(existing);
                    ctx.addMessage(null, new FacesMessage("Brand updated successfully."));
                }
            }

            brandList = brandsFacade.findAll();
            resetForm();

        } catch (Exception e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error saving brand.", null));
        }
    }

    public void delete(Brands brand) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            // Find or create "Unknown" brand for orphaned products
            List<Brands> unknownList = brandsFacade.findAll().stream()
                    .filter(b -> b.getBrandName().equalsIgnoreCase("Unknown"))
                    .collect(Collectors.toList());
            Brands unknownBrand = !unknownList.isEmpty() ? unknownList.get(0) : null;
            
            // Update all products with this brand to "Unknown"
            if (brand.getProductsCollection() != null && !brand.getProductsCollection().isEmpty()) {
                for (Products p : brand.getProductsCollection()) {
                    if (unknownBrand != null) {
                        p.setBrandID(unknownBrand);
                    } else {
                        p.setBrandID(null);
                    }
                    productsFacade.edit(p);
                }
            }
            
            // Now delete the brand
            brandsFacade.remove(brand);
            brandList = brandsFacade.findAll();
            ctx.addMessage(null, new FacesMessage("Brand deleted successfully."));
        } catch (Exception e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error deleting brand.", null));
            e.printStackTrace();
        }
        resetForm();
    }

    public void prepareEdit(Brands brand) {
        this.selectedBrand = new Brands();
        this.selectedBrand.setBrandID(brand.getBrandID());
        this.selectedBrand.setBrandName(brand.getBrandName());
    }

    public void resetForm() {
        searchKeyword = "";
        selectedBrand = new Brands();
        brandList = brandsFacade.findAll();

        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getExternalContext().getFlash().setKeepMessages(true);
        try {
            ctx.getExternalContext().redirect("brands.xhtml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void search() {
    FacesContext ctx = FacesContext.getCurrentInstance();

    String kw = (searchKeyword == null) ? "" : searchKeyword.trim().toLowerCase();

    if (kw.isEmpty()) {
        brandList = brandsFacade.findAll();
        return;
    }

    List<Brands> result = brandsFacade.findAll().stream()
            .filter(b -> b.getBrandName() != null 
                      && b.getBrandName().toLowerCase().contains(kw))
            .collect(Collectors.toList());

    if (result.isEmpty()) {
        brandList = new ArrayList<>(); 
        ctx.addMessage(null, new FacesMessage("Brand not found."));
        return;
    }

    brandList = result;
}

}
