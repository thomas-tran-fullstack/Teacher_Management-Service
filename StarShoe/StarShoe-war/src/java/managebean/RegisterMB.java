package managebean;

import entities.Customers;
import entities.Users;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Date;
import sessions.CustomersFacadeLocal;
import sessions.RolesFacadeLocal;
import sessions.UsersFacadeLocal;

@Named("registerMB")
@RequestScoped
public class RegisterMB implements Serializable {

    @EJB
    private UsersFacadeLocal usersFacade;

    @EJB
    private CustomersFacadeLocal customersFacade;

    @EJB
    private RolesFacadeLocal rolesFacade;

    @Inject
    private UserMB userMB;

    @Inject
    private CustomerMB customerMB;

    private String fullName;
    private String username;
    private String email;
    private String password;
    private String phone;
    private String address;

    public void register() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        try {
            boolean usernameExists = usersFacade.findAll().stream()
                    .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
            boolean emailExists = usersFacade.findAll().stream()
                    .anyMatch(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email));
            boolean phoneExists = customersFacade.findAll().stream()
                    .anyMatch(c -> c.getPhone() != null && c.getPhone().equalsIgnoreCase(phone));

            if (usernameExists) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Username already exists.", null));
                return;
            }
            if (emailExists) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Email already exists.", null));
                return;
            }
            if (phoneExists) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Phone number already exists.", null));
                return;
            }

            Users u = new Users();
            u.setUsername(username);
            u.setPasswordHash(password);
            u.setEmail(email);
            u.setCreatedAt(new Date());
            u.setRoleID(rolesFacade.find(3));
            usersFacade.create(u);

            Customers c = new Customers();
            c.setUserID(u);
            c.setFullName(fullName);
            c.setPhone(phone);
            c.setAddress(address);
            customersFacade.create(c);

            if (userMB != null) userMB.refreshList();
            if (customerMB != null) customerMB.refreshList();

            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Registration successful! You can now log in.", null));
            clearForm();

        } catch (Exception e) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error during registration: " + e.getMessage(), null));
        }
    }

    private void clearForm() {
        fullName = username = email = password = phone = address = "";
    }

    // Getters & setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
