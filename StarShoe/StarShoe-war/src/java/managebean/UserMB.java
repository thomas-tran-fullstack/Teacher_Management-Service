package managebean;

import entities.Users;
import entities.Roles;
import entities.Customers;
import entities.Employees;
import java.util.stream.Collectors;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.util.List;

import sessions.CustomersFacadeLocal;
import sessions.EmployeesFacadeLocal;
import sessions.UsersFacadeLocal;
import sessions.RolesFacadeLocal;

@Named("userMB")
@SessionScoped
public class UserMB implements Serializable {

    @Inject
    private EmployeeMB employeeMB;

    @EJB
    private UsersFacadeLocal usersFacade;
    @EJB
    private RolesFacadeLocal rolesFacade;
    @EJB
    private CustomersFacadeLocal customersFacade;
    @EJB
    private EmployeesFacadeLocal employeesFacade;

    private Users selectedUser = new Users();
    private Integer selectedRoleId;
    private String searchKeyword = "";
    private List<Users> userList;

    // Extra fields
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String position;

    // Getters / Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<Users> getUserList() {
        if (userList == null) {
            userList = usersFacade.findAll();
        }
        return userList;
    }

    public Users getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(Users selectedUser) {
        this.selectedUser = selectedUser;
    }

    public Integer getSelectedRoleId() {
        return selectedRoleId;
    }

    public void setSelectedRoleId(Integer selectedRoleId) {
        this.selectedRoleId = selectedRoleId;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public List<Roles> getRoleList() {
        return rolesFacade.findAll();
    }

    public void confirm() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            if (selectedUser.getUsername() == null || selectedUser.getUsername().isBlank()
                    || selectedUser.getPasswordHash() == null || selectedUser.getPasswordHash().isBlank()
                    || selectedUser.getEmail() == null || selectedUser.getEmail().isBlank()
                    || fullName == null || fullName.isBlank()
                    || phone == null || phone.isBlank()
                    || selectedRoleId == null) {

                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "All required fields must be filled.", null));
                return;
            }

            if (selectedRoleId == 3 && (address == null || address.isBlank())) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Address required for customer.", null));
                return;
            }

            if (selectedRoleId == 2 && (position == null || position.isBlank())) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Position required for employee.", null));
                return;
            }

            boolean usernameExists = usersFacade.findAll().stream()
                    .anyMatch(u -> u.getUsername().equalsIgnoreCase(selectedUser.getUsername()));

            boolean emailExists = usersFacade.findAll().stream()
                    .anyMatch(u -> u.getEmail() != null
                    && u.getEmail().equalsIgnoreCase(selectedUser.getEmail()));

            if (usernameExists) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Username already exists.", null));
                return;
            }
            if (emailExists) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Email already exists.", null));
                return;
            }

            boolean phoneExists
                    = customersFacade.findAll().stream()
                            .anyMatch(c -> c.getPhone() != null && c.getPhone().equalsIgnoreCase(phone))
                    || employeesFacade.findAll().stream()
                            .anyMatch(e -> e.getPhone() != null && e.getPhone().equalsIgnoreCase(phone));

            if (phoneExists) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Phone already exists.", null));
                return;
            }

            selectedUser.setRoleID(rolesFacade.find(selectedRoleId));
            usersFacade.create(selectedUser);

            if (selectedRoleId == 3) {
                Customers c = new Customers();
                c.setUserID(selectedUser);
                c.setFullName(fullName);
                c.setPhone(phone);
                c.setAddress(address);
                customersFacade.create(c);
            }

            if (selectedRoleId == 2) {
                Employees e = new Employees();
                e.setUserID(selectedUser);
                e.setFullName(fullName);
                e.setPhone(phone);
                e.setPosition(position);
                employeesFacade.create(e);
            }
            if (employeeMB != null) {
                employeeMB.refreshList();
            }

            ctx.addMessage(null, new FacesMessage("User created successfully!"));
            refreshList();
            resetForm();

        } catch (Exception ex) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Create failed", null));
            ex.printStackTrace();
        }
    }
    public void delete(Users u) {
        try {
            Users real = usersFacade.find(u.getUserID());

            customersFacade.findAll().stream()
                    .filter(c -> c.getUserID() != null && c.getUserID().equals(real))
                    .forEach(c -> customersFacade.remove(c));

            employeesFacade.findAll().stream()
                    .filter(e -> e.getUserID() != null && e.getUserID().equals(real))
                    .forEach(e -> employeesFacade.remove(e));

            usersFacade.remove(real);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Deleted!"));
            refreshList();
            if (employeeMB != null) {
                employeeMB.refreshList();
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Delete failed"));
        }
    }

    public void search() {
        if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
            userList = usersFacade.findAll();
            return;
        }

        String kw = searchKeyword.toLowerCase().trim();

        userList = usersFacade.findAll().stream()
                .filter(u
                        -> (u.getUsername() != null && u.getUsername().toLowerCase().contains(kw))
                || (u.getEmail() != null && u.getEmail().toLowerCase().contains(kw))
                )
                .collect(Collectors.toList());

        if (userList.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(
                    null, new FacesMessage("User not found.")
            );
        }
    }

    public void refreshList() {
        selectedUser = new Users();
        selectedRoleId = null;
        searchKeyword = "";
        fullName = "";
        phone = "";
        address = "";
        position = "";
        userList = usersFacade.findAll();
    }

    public void resetForm() {
        refreshList();
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getExternalContext().getFlash().setKeepMessages(true);

        try {
            ctx.getExternalContext().redirect("users.xhtml");
        } catch (Exception e) {
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
