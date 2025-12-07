package managebean;

import entities.Products;
import entities.Reviews;
import entities.Customers;
import entities.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Date;
import sessions.ReviewsFacadeLocal;
import sessions.ProductsFacadeLocal;

@Named("productDetailMB")
@ViewScoped
public class ProductDetailMB implements Serializable {

    @EJB
    private ProductsFacadeLocal productFacade;

    @EJB
    private ReviewsFacadeLocal reviewsFacade;

    private Products selectedProduct;
    private Integer productID;

    private Reviews myReview;
    private boolean hasReviewed = false;

    private List<Integer> sizes;
    private Integer selectedSize = 40;
    private int quantity = 1;

    private List<Reviews> reviews;
    @Inject
    private CartMB cartMB;

    public void increaseQty() {
        quantity++;
    }

    public void decreaseQty() {
        if (quantity > 1) {
            quantity--;
        }
    }

    public String buyNow() {
        if (selectedProduct == null) {
            return null;
        }
        cartMB.addToCart(selectedProduct, quantity, selectedSize);
        return "/client/checkout.xhtml?faces-redirect=true";
    }

    private List<Products> suggestedProducts;
    private Integer newRating = 5;
    private String newComment = "";
    private boolean editMode = false;

    public void selectSize(int s) {
        this.selectedSize = s;
    }

    public void addToCart() {
        cartMB.addToCart(selectedProduct, quantity, selectedSize);
    }

    public String starsFor(int rating) {
        int r = Math.max(0, Math.min(rating, 5));
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= r ? "&#9733;" : "&#9734;");
        }
        return sb.toString();
    }

    public String starsFor(Integer rating) {
        return starsFor(rating == null ? 0 : rating.intValue());
    }

    @PostConstruct
    public void init() {
        try {
            productID = Integer.parseInt(FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getRequestParameterMap()
                    .get("id"));

            selectedProduct = productFacade.find(productID);
            sizes = Arrays.asList(36, 37, 38, 39, 40, 41, 42, 43, 44);
            selectedSize = sizes.get(0);

            reviews = reviewsFacade.findByProduct(productID);

            suggestedProducts = productFacade.findRandomProducts(10, productID);

            Customers cus = (Customers) FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap()
                    .get("currentCustomer");

            if (cus != null) {
                for (Reviews rv : reviews) {
                    if (rv.getCustomerID() != null && rv.getCustomerID().getCustomerID().equals(cus.getCustomerID())) {
                        myReview = rv;
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chooseSize(Integer s) {
        this.selectedSize = s;
    }

    public void startEditReview(Reviews r) {
        newRating = r.getRating();
        newComment = r.getComment();
        myReview = r;
        editMode = true;
    }

    public void deleteReview(Integer reviewID) {
        try {
            Reviews rv = reviewsFacade.find(reviewID);
            if (rv != null) {
                reviewsFacade.remove(rv);
            }

            // reload
            reviews = reviewsFacade.findByProduct(productID);
            myReview = null;
            newRating = null;
            newComment = "";

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void submitReview() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Users u = (Users) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("loggedUser");

        if (u == null) {
            ctx.addMessage(null, new FacesMessage("Please login first"));
            return;
        }

        Customers cus = u.getCustomersCollection().iterator().next();

        if (!editMode && myReview != null) {
            ctx.addMessage(null, new FacesMessage("You already reviewed this product"));
            return;
        }

        if (editMode) {
            myReview.setRating(newRating);
            myReview.setComment(newComment);
            reviewsFacade.edit(myReview);
            editMode = false;
        } else {
            Reviews r = new Reviews();
            r.setProductID(selectedProduct);
            r.setCustomerID(cus);
            r.setRating(newRating);
            r.setComment(newComment);
            r.setCreatedAt(new Date());
            reviewsFacade.create(r);
        }

        reviews = reviewsFacade.findByProduct(productID);
        newRating = 0;
        newComment = "";
    }

    // getters ==================================
    public Products getSelectedProduct() {
        return selectedProduct;
    }

    public List<Integer> getSizes() {
        return sizes;
    }

    public Integer getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(Integer selectedSize) {
        this.selectedSize = selectedSize;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int q) {
        quantity = q;
    }

    public List<Reviews> getReviews() {
        return reviews;
    }

    public Integer getNewRating() {
        return newRating;
    }

    public void setNewRating(Integer newRating) {
        this.newRating = newRating;
    }

    public String getNewComment() {
        return newComment;
    }

    public void setNewComment(String newComment) {
        this.newComment = newComment;
    }

    public Reviews getMyReview() {
        return myReview;
    }

    public void setMyReview(Reviews myReview) {
        this.myReview = myReview;
    }

    public boolean isHasReviewed() {
        return hasReviewed;
    }

    public void setHasReviewed(boolean hasReviewed) {
        this.hasReviewed = hasReviewed;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public List<Products> getSuggestedProducts() {
        return suggestedProducts;
    }

    public void setSuggestedProducts(List<Products> suggestedProducts) {
        this.suggestedProducts = suggestedProducts;
    }

}
