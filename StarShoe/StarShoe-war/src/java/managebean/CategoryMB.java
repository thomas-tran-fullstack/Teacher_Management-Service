package managebean;

import entities.Categories;
import entities.Products;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import sessions.CategoriesFacadeLocal;
import sessions.ProductsFacadeLocal;

@Named("categoryMB")
@SessionScoped
public class CategoryMB implements Serializable {

    @EJB
    private CategoriesFacadeLocal categoriesFacade;
    @EJB
    private ProductsFacadeLocal productsFacade;

    private Categories selectedCategory;
    private List<Categories> categoryList;
    private String searchKeyword;

    @PostConstruct
    public void init() {
        categoryList = categoriesFacade.findAll();
        selectedCategory = new Categories();
    }

    public Categories getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(Categories selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public List<Categories> getCategoryList() {
        return categoryList;
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

        if (selectedCategory == null || selectedCategory.getCategoryName() == null || selectedCategory.getCategoryName().trim().isEmpty()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Category name is required.", null));
            return;
        }

        boolean exists = categoryList.stream()
                .anyMatch(c -> c.getCategoryName().equalsIgnoreCase(selectedCategory.getCategoryName())
                && (selectedCategory.getCategoryID() == null || !c.getCategoryID().equals(selectedCategory.getCategoryID())));

        if (exists) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Category name already exists.", null));
            return;
        }

        try {
            if (selectedCategory.getCategoryID() == null) {
                categoriesFacade.create(selectedCategory);
                ctx.addMessage(null, new FacesMessage("Category added successfully."));
            } else {
                Categories existing = categoriesFacade.find(selectedCategory.getCategoryID());
                if (existing != null) {
                    existing.setCategoryName(selectedCategory.getCategoryName());
                    categoriesFacade.edit(existing);
                    ctx.addMessage(null, new FacesMessage("Category updated successfully."));
                }
            }

            categoryList = categoriesFacade.findAll();
            resetForm();

        } catch (Exception e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error saving category.", null));
        }
    }

    public void delete(Categories category) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            // Find or create "Unknown" category for orphaned products
            List<Categories> unknownList = categoriesFacade.findAll().stream()
                    .filter(c -> c.getCategoryName().equalsIgnoreCase("Unknown"))
                    .collect(Collectors.toList());
            Categories unknownCategory = !unknownList.isEmpty() ? unknownList.get(0) : null;
            
            // Update all products with this category to "Unknown"
            if (category.getProductsCollection() != null && !category.getProductsCollection().isEmpty()) {
                for (Products p : category.getProductsCollection()) {
                    if (unknownCategory != null) {
                        p.setCategoryID(unknownCategory);
                    } else {
                        p.setCategoryID(null);
                    }
                    productsFacade.edit(p);
                }
            }
            
            // Now delete the category
            categoriesFacade.remove(category);
            categoryList = categoriesFacade.findAll();
            ctx.addMessage(null, new FacesMessage("Category deleted successfully."));
        } catch (Exception e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error deleting category.", null));
            e.printStackTrace();
        }
    }

    public void prepareEdit(Categories category) {
        this.selectedCategory = new Categories();
        this.selectedCategory.setCategoryID(category.getCategoryID());
        this.selectedCategory.setCategoryName(category.getCategoryName());
    }

    public void resetForm() {
        selectedCategory = new Categories();
        searchKeyword = "";
        categoryList = categoriesFacade.findAll();

        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getExternalContext().getFlash().setKeepMessages(true);
        try {
            ctx.getExternalContext().redirect("categories.xhtml");
        } catch (Exception e) {
        }
    }

    public void search() {
        if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
            categoryList = categoriesFacade.findAll();
            return;
        }

        String kw = searchKeyword.toLowerCase().trim();
        categoryList = categoriesFacade.findAll().stream()
                .filter(c -> c.getCategoryName() != null
                && c.getCategoryName().toLowerCase().contains(kw))
                .collect(Collectors.toList());

        if (categoryList.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Category not found."));
        }
    }

}
