package managebean;

import entities.Customers;
import entities.Employees;
import entities.Users;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import sessions.UsersFacadeLocal;

@Named("loginMB")
@SessionScoped
public class LoginMB implements Serializable {

    @EJB
    private UsersFacadeLocal usersFacade;

    private String username;
    private String password;
    private Users loggedUser;

    public String getDisplayName() {
        if (loggedUser == null) {
            return "Unknown";
        }

        // If admin has full name, show it
        if (loggedUser.getFullName() != null && !loggedUser.getFullName().isEmpty()) {
            return loggedUser.getFullName();
        }

        // employee
        if (loggedUser.getEmployeesCollection() != null && !loggedUser.getEmployeesCollection().isEmpty()) {
            return loggedUser.getEmployeesCollection().iterator().next().getFullName();
        }

        // customer
        if (loggedUser.getCustomersCollection() != null && !loggedUser.getCustomersCollection().isEmpty()) {
            return loggedUser.getCustomersCollection().iterator().next().getFullName();
        }

        return loggedUser.getUsername();
    }

    public String login() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (username == null || username.trim().isEmpty() || password == null) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid username or password.", null));
            return null;
        }

        String uname = username.trim();
        Users user = usersFacade.findByUsername(uname);

        if (user == null) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid username or password.", null));
            return null;
        }

        // Compare password (DB currently stores plaintext in your sql sample)
        if (!user.getPasswordHash().equals(password)) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid username or password.", null));
            return null;
        }

        loggedUser = user;
        FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap()
                .put("loggedUser", loggedUser);

        // put logged customer into session so other page can use
        Customers cus = null;
        if (user.getCustomersCollection() != null && !user.getCustomersCollection().isEmpty()) {
            cus = user.getCustomersCollection().iterator().next();
        }

        FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .put("currentCustomer", cus);

        String role = (user.getRoleID() != null) ? user.getRoleID().getRoleName().toLowerCase() : "customer";
        if ("admin".equals(role)) {
            return "/admin/products.xhtml?faces-redirect=true";
        } else if ("employee".equals(role)) {
            return "/employee/products.xhtml?faces-redirect=true";
        } else {
            return "/client/index.xhtml?faces-redirect=true";
        }
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/client/index.xhtml?faces-redirect=true";
    }

    // getters / setters
    public List<Customers> getCustomerList() {
        if (loggedUser != null && loggedUser.getCustomersCollection() != null) {
            return new java.util.ArrayList<>(loggedUser.getCustomersCollection());
        }
        return java.util.Collections.emptyList();
    }

    public List<Employees> getEmployeeList() {
        if (loggedUser != null && loggedUser.getEmployeesCollection() != null) {
            return new java.util.ArrayList<>(loggedUser.getEmployeesCollection());
        }
        return java.util.Collections.emptyList();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Users getLoggedUser() {
        return loggedUser;
    }

    public void setLoggedUser(Users loggedUser) {
        this.loggedUser = loggedUser;
    }
}
