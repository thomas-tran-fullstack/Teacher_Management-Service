package managebean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import entities.Products;
import entities.Brands;
import entities.Categories;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Named("productMB")
@ViewScoped
public class ProductMB implements Serializable {

    @EJB
    private sessions.ProductsFacadeLocal productsFacade;
    @EJB
    private sessions.BrandsFacadeLocal brandsFacade;
    @EJB
    private sessions.CategoriesFacadeLocal categoriesFacade;
    private List<Products> productList;
    private List<Brands> brandList;
    private List<Categories> categoryList;
    private Part uploadedFile;
    private Products selectedProduct;
    private Integer selectedBrandId;
    private Integer selectedCategoryId;
    private String searchKeyword;
    private List<Products> hotProducts;
    private List<Products> allProducts;
    private List<Products> currentPageProducts;
    private List<entities.Products> pagedProducts;
    private int currentPage = 1;
    private int pageSize = 15;
    private String sortOption = "AZ";

    private void updatePageData() {
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, allProducts.size());
        currentPageProducts = allProducts.subList(start, end);
    }

    @PostConstruct
    public void init() {
        hotProducts = productsFacade.getHotProducts();
        allProducts = productsFacade.findAll();
        updatePageData();
        loadAll();
        applySort();
        buildPage();
        refreshList();
        brandList = brandsFacade.findAll();
        categoryList = categoriesFacade.findAll();
        // If this page received a productId param (from products list edit link), load that product
        try {
            String pid = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("productId");
            if (pid != null && !pid.isEmpty()) {
                try {
                    Integer id = Integer.parseInt(pid);
                    selectedProduct = productsFacade.find(id);
                    if (selectedProduct != null) {
                        selectedBrandId = (selectedProduct.getBrandID() != null) ? selectedProduct.getBrandID().getBrandID() : null;
                        selectedCategoryId = (selectedProduct.getCategoryID() != null) ? selectedProduct.getCategoryID().getCategoryID() : null;
                    }
                } catch (NumberFormatException nfe) {
                    selectedProduct = new Products();
                }
            } else {
                selectedProduct = new Products();
            }
        } catch (Exception e) {
            selectedProduct = new Products();
        }
        System.out.println(">>> INIT: products = " + (productList != null ? productList.size() : 0));
    }

    public void loadAll() {
        allProducts = productsFacade.findAll(); // load full list
    }

    public void onSortChange() {
        // f:ajax listener
        applySort();
        currentPage = 1;
        buildPage();
        updatePageData();
    }

    private void applySort() {
        if (allProducts == null) {
            return;
        }

        switch (sortOption) {
            case "AZ" ->
                allProducts.sort(Comparator.comparing(p -> p.getProductName().toLowerCase()));
            case "ZA" ->
                allProducts.sort(Comparator.comparing((Products p) -> p.getProductName().toLowerCase()).reversed());
            case "PRICE_ASC" ->
                allProducts.sort(Comparator.comparing(p -> p.getPrice() == null ? BigDecimal.ZERO : p.getPrice()));
            case "PRICE_DESC" ->
                allProducts.sort(Comparator.comparing((Products p) -> p.getPrice() == null ? BigDecimal.ZERO : p.getPrice()).reversed());
        }
        updatePageData();
    }

    private void buildPage() {
        int from = (currentPage - 1) * pageSize;
        int to = Math.min(from + pageSize, allProducts.size());

        pagedProducts = new ArrayList<>(allProducts.subList(from, to));
    }

    public void goToPage(int page) {
        this.currentPage = page;
        buildPage();
    }

    public List<Integer> getPageNumbers() {
        int pages = (int) Math.ceil((double) allProducts.size() / pageSize);
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= pages; i++) {
            list.add(i);
        }
        return list;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void changePage(int p) {
        currentPage = p;
        updatePageData();
    }

    public String getSortOption() {
        return sortOption;
    }

    public void setSortOption(String sortOption) {
        this.sortOption = sortOption;
    }

    public void sortProducts() {
        switch (sortOption) {
            case "az":
                allProducts.sort(Comparator.comparing(Products::getProductName));
                break;
            case "za":
                allProducts.sort(Comparator.comparing(Products::getProductName).reversed());
                break;
            case "low":
                allProducts.sort(Comparator.comparing(Products::getPrice));
                break;
            case "high":
                allProducts.sort(Comparator.comparing(Products::getPrice).reversed());
                break;
        }
        updatePageData();
    }

    public void reload() {
        loadAll();
        applySort();
        updatePageData();
        refreshClient();
    }

    public void refreshClient() {
        allProducts = productsFacade.findAll();
        applySort();
        buildPage();
    }

    public void confirm() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (selectedProduct == null) {
            return;
        }
        if (selectedProduct.getProductName() == null || selectedProduct.getProductName().trim().isEmpty()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Product name is required.", null));
            return;
        }
        if (selectedProduct.getPrice() == null || selectedProduct.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Price must be greater than 0.", null));
            return;
        }
        if (selectedProduct.getQuantity() == null || selectedProduct.getQuantity() < 0) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Quantity must be >= 0.", null));
            return;
        }
        if (selectedProduct.getProductID() == null) {
            List<Products> existing = productsFacade.findByName(selectedProduct.getProductName());
            if (existing != null && !existing.isEmpty()) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Product already exists!", null));
                return;
            }
        }
        if (selectedBrandId != null) {
            selectedProduct.setBrandID(brandsFacade.find(selectedBrandId));
        }
        if (selectedCategoryId != null) {
            selectedProduct.setCategoryID(categoriesFacade.find(selectedCategoryId));
        }
        try {
            if (uploadedFile != null) {
                String fileName = uploadedFile.getSubmittedFileName();
                String uploadDir = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/resources/images/products");
                File targetFile = new File(uploadDir, fileName);
                try (InputStream input = uploadedFile.getInputStream(); FileOutputStream out = new FileOutputStream(targetFile)) {
                    input.transferTo(out);
                }
                selectedProduct.setImage(fileName);
            }
        } catch (IOException e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error saving image.", null));
            return;
        }
        if (selectedProduct.getProductID() == null) {
            productsFacade.create(selectedProduct);
            try {
                websocket.ProductNotifier.notifyClients();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ctx.addMessage(null, new FacesMessage("Product added successfully!"));
        } else {
            productsFacade.edit(selectedProduct);
            try {
                websocket.ProductNotifier.notifyClients();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ctx.addMessage(null, new FacesMessage("Product updated successfully!"));
        }
        refreshList();
        resetForm();
        resetAll();
        loadAll();
    }

    public void prepareEdit(Products p) {
        selectedProduct = productsFacade.find(p.getProductID());
        selectedBrandId = (selectedProduct.getBrandID() != null) ? selectedProduct.getBrandID().getBrandID() : null;
        selectedCategoryId = (selectedProduct.getCategoryID() != null) ? selectedProduct.getCategoryID().getCategoryID() : null;
    }

    public void delete(Products p) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        productsFacade.remove(productsFacade.find(p.getProductID()));
        try {
            websocket.ProductNotifier.notifyClients();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ctx.addMessage(null, new FacesMessage("Product deleted."));
        refreshList();
        resetForm();
        resetAll();
        loadAll();
    }

    public void search() {
        if ((searchKeyword == null || searchKeyword.trim().isEmpty()) && selectedBrandId == null && selectedCategoryId == null) {
            refreshList();
            return;
        }
        String kw = (searchKeyword == null ? "" : searchKeyword.toLowerCase().trim());
        List<Products> result = new ArrayList<>();
        for (Products p : productsFacade.findAll()) {
            boolean match = true;
            if (!kw.isEmpty()) {
                String name = (p.getProductName() != null ? p.getProductName().toLowerCase() : "");
                String brandName = (p.getBrandID() != null ? p.getBrandID().getBrandName().toLowerCase() : "");
                if (!(name.contains(kw) || brandName.contains(kw))) {
                    match = false;
                }
            }
            if (match && selectedBrandId != null && (p.getBrandID() == null || !p.getBrandID().getBrandID().equals(selectedBrandId))) {
                match = false;
            }
            if (match && selectedCategoryId != null && (p.getCategoryID() == null || !p.getCategoryID().getCategoryID().equals(selectedCategoryId))) {
                match = false;
            }
            if (match) {
                result.add(p);
            }
        }
        productList = result;
        if (productList.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Product not found."));
        }
    }

    public void resetForm() {
        selectedProduct = new Products();
        selectedBrandId = null;
        selectedCategoryId = null;
    }

    public void refreshList() {
        productList = productsFacade.findAll();
        System.out.println(">>> Refresh products = " + productList.size());
    }

    public void resetAll() {
        searchKeyword = "";
        selectedBrandId = null;
        selectedCategoryId = null;

        selectedProduct = new Products();
        uploadedFile = null;

        refreshList();

        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getExternalContext().getFlash().setKeepMessages(true);
        try {
            ctx.getExternalContext().redirect("products.xhtml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<Products> getHotProducts() {
        return hotProducts;
    }

    public List<Products> getProductList() {
        return productList;
    }

    public List<Brands> getBrandList() {
        return brandsFacade.findAll();
    }

    public List<Categories> getCategoryList() {
        return categoriesFacade.findAll();
    }

    public Products getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(Products selectedProduct) {
        this.selectedProduct = selectedProduct;
    }

    public Integer getSelectedBrandId() {
        return selectedBrandId;
    }

    public void setSelectedBrandId(Integer selectedBrandId) {
        this.selectedBrandId = selectedBrandId;
    }

    public Integer getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public void setSelectedCategoryId(Integer selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public List<Products> getCurrentPageProducts() {
        return currentPageProducts;
    }

    public List<entities.Products> getPagedProducts() {
        if (pagedProducts == null) {
            buildPage();
        }
        return pagedProducts;
    }
}
