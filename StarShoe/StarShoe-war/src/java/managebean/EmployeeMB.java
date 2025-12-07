package managebean;

import entities.Employees;
import entities.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import sessions.CustomersFacadeLocal;
import sessions.EmployeesFacadeLocal;
import sessions.UsersFacadeLocal;

@Named("employeeMB")
@ViewScoped
public class EmployeeMB implements Serializable {

    @EJB private CustomersFacadeLocal customersFacade;
    @EJB private EmployeesFacadeLocal employeesFacade;
    @EJB private UsersFacadeLocal usersFacade;

    @Inject private UserMB userMB;

    // LUÔN KHỞI TẠO để tránh null EL
    private Employees selectedEmployee = new Employees();
    private Users selectedUser = new Users();
    private String searchKeyword = "";
    private List<Employees> employeeList;

    // === Init / list ===
    public void initList() {
        employeeList = employeesFacade.findAll();
    }

    public List<Employees> getEmployeeList() {
    return employeeList != null ? employeeList : employeesFacade.findAll();
}
    @PostConstruct
public void init() {
    refreshList();
    selectedEmployee = new Employees();
    selectedUser = new Users();
}



    // getters / setters
    public Employees getSelectedEmployee() { return selectedEmployee; }
    public void setSelectedEmployee(Employees selectedEmployee) { 
        this.selectedEmployee = selectedEmployee != null ? selectedEmployee : new Employees();
    }
    public Users getSelectedUser() { return selectedUser; }
    public void setSelectedUser(Users selectedUser) { 
        this.selectedUser = selectedUser != null ? selectedUser : new Users();
    }

    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }

    // === Prepare edit (khi bấm Edit trên table) ===
    public void prepareEdit(Employees e) {
    if (e == null) {
        selectedEmployee = new Employees();
        selectedUser = new Users();
        return;
    }

    selectedEmployee = employeesFacade.find(e.getEmployeeID());

    if (selectedEmployee == null) {
        selectedEmployee = new Employees();
        selectedUser = new Users();
        return;
    }

    selectedUser = selectedEmployee.getUserID() != null
            ? selectedEmployee.getUserID()
            : new Users();
}


    // === Confirm (edit only) ===
    public void confirm() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        // 1) nếu chưa chọn employee -> thông báo, không validate form
        if (selectedEmployee == null || selectedEmployee.getEmployeeID() == null) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "No employee selected.", null));
            return;
        }

        // 2) lấy record từ DB
        Employees dbEmp = employeesFacade.find(selectedEmployee.getEmployeeID());
        if (dbEmp == null) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Employee not found.", null));
            return;
        }

        // 3) server-side required check
        if (isBlank(selectedEmployee.getFullName()) ||
            isBlank(selectedEmployee.getPosition()) ||
            isBlank(selectedEmployee.getPhone()) ||
            isBlank(selectedUser.getEmail())) {

            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "All fields are required.", null));
            return;
        }

        // 4) phone duplicate (customer + other employees)
        String phone = selectedEmployee.getPhone().trim();
        boolean phoneExists =
                customersFacade.findAll().stream()
                        .anyMatch(c -> c.getPhone() != null && c.getPhone().equalsIgnoreCase(phone))
                ||
                employeesFacade.findAll().stream()
                        .anyMatch(ev -> ev.getPhone() != null
                                && ev.getPhone().equalsIgnoreCase(phone)
                                && !ev.getEmployeeID().equals(selectedEmployee.getEmployeeID()));

        if (phoneExists) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Phone already exists.", null));
            return;
        }

        // 5) email duplicate
        String email = selectedUser.getEmail().trim();
        boolean emailExists = usersFacade.findAll().stream()
                .anyMatch(u -> u.getEmail() != null
                        && u.getEmail().equalsIgnoreCase(email)
                        && !u.getUserID().equals(selectedUser.getUserID()));
        if (emailExists) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Email already exists.", null));
            return;
        }

        // 6) apply updates
        try {
            // update employee fields
            dbEmp.setFullName(selectedEmployee.getFullName().trim());
            dbEmp.setPosition(selectedEmployee.getPosition().trim());
            dbEmp.setPhone(phone);
            employeesFacade.edit(dbEmp);

            // update user email (if exists)
            if (dbEmp.getUserID() != null) {
                Users managedUser = usersFacade.find(dbEmp.getUserID().getUserID());
                if (managedUser != null) {
                    managedUser.setEmail(email);
                    usersFacade.edit(managedUser);
                }
            }

            ctx.addMessage(null, new FacesMessage("Employee updated successfully!"));

            // refresh lists everywhere
            refreshList();
            if (userMB != null) userMB.refreshList();

            // reset form and reload page to ensure immediate view update
            resetForm();
            ctx.getExternalContext().getFlash().setKeepMessages(true);
            try {
                ctx.getExternalContext().redirect("employees.xhtml");
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error updating employee!", null));
        }
    }

    // === Delete (reload page after) ===
    public void delete(Employees e) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            Employees emp = employeesFacade.find(e.getEmployeeID());
            if (emp != null) {
                Users u = emp.getUserID();
                employeesFacade.remove(emp);
                if (u != null) {
                    Users managed = usersFacade.find(u.getUserID());
                    if (managed != null) usersFacade.remove(managed);
                }
            }
            ctx.addMessage(null, new FacesMessage("Employee deleted successfully!"));
            // refresh both lists
            refreshList();
            if (userMB != null) userMB.refreshList();
            ctx.getExternalContext().getFlash().setKeepMessages(true);
            ctx.getExternalContext().redirect("employees.xhtml");
        } catch (Exception ex) {
            ex.printStackTrace();
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Delete failed", null));
        }
    }

    // === Search ===
    public void search() {
        String kw = (searchKeyword == null ? "" : searchKeyword.trim().toLowerCase());
        if (kw.isEmpty()) {
            refreshList();
            return;
        }
        employeeList = employeesFacade.findAll().stream()
                .filter(e -> (e.getFullName() != null && e.getFullName().toLowerCase().contains(kw))
                        || (e.getPhone() != null && e.getPhone().toLowerCase().contains(kw))
                        || (e.getPosition() != null && e.getPosition().toLowerCase().contains(kw)))
                .collect(Collectors.toList());

        if (employeeList.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Employee not found."));
        }
    }

    // === Reset / refresh ===
    public void resetForm() {
        selectedEmployee = new Employees();
        selectedUser = new Users();
    }

    public void refreshList() {
        employeeList = employeesFacade.findAll();
    }

    public void resetPage() {
    searchKeyword = "";
    selectedEmployee = new Employees();
    selectedUser = new Users();
    refreshList(); // load full list

    FacesContext ctx = FacesContext.getCurrentInstance();
    ctx.getExternalContext().getFlash().setKeepMessages(true);
    try {
        ctx.getExternalContext().redirect("employees.xhtml");
    } catch (Exception e) {}
}

    

    // small util
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
