package managebean;

import entities.Orders;
import entities.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import sessions.OrdersFacadeLocal;
import sessions.UsersFacadeLocal;

@Named ("accountMB")
@SessionScoped
public class AccountMB implements Serializable {

    @EJB
    private UsersFacadeLocal userFacade;

    @EJB
    private OrdersFacadeLocal orderFacade;

    private Users user; // current logged-in user
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
    private List<Orders> orderHistory;

    @PostConstruct
    public void init() {
        user = (Users) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("currentUser");

        if (user != null) {
            orderHistory = orderFacade.getOrdersByUser(user.getUserID());
        }
    }

    // Getters & setters
    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public List<Orders> getOrderHistory() { return orderHistory; }

    // Cập nhật thông tin cá nhân
    public String updateProfile() {
        try {
            userFacade.edit(user);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Profile updated successfully", null));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Update failed", null));
        }
        return null;
    }

    public String changePassword() {
        if (!user.getPasswordHash().equals(currentPassword)) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Current password is incorrect", null));
            return null;
        }
        if (!newPassword.equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "New passwords do not match", null));
            return null;
        }
        user.setPasswordHash(newPassword);
        userFacade.edit(user);
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Password changed successfully", null));
        return null;
    }

    // Xóa tài khoản
    public String deleteAccount() {
        try {
            userFacade.remove(user);
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            return "/index.xhtml?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
