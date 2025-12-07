package managebean;

import entities.Customers;
import entities.Users;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.List;
import jakarta.inject.Inject;
import sessions.CustomersFacadeLocal;
import sessions.UsersFacadeLocal;

@Named("customerMB")
@ViewScoped
public class CustomerMB implements Serializable {

    @Inject
    private UserMB userMB;

    @EJB
    private CustomersFacadeLocal customersFacade;

    @EJB
    private UsersFacadeLocal usersFacade;

    private List<Customers> customerList;
    private String searchKeyword = "";

    public List<Customers> getCustomerList() {
        if (customerList == null) {
            customerList = customersFacade.findAll();
        }
        return customerList;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public void search() {
        String kw = searchKeyword == null ? "" : searchKeyword.trim().toLowerCase();

        if (kw.isEmpty()) {
            customerList = customersFacade.findAll();
            return;
        }

        customerList = customersFacade.findAll().stream()
                .filter(c -> (c.getFullName() != null && c.getFullName().toLowerCase().contains(kw))
                || (c.getPhone() != null && c.getPhone().toLowerCase().contains(kw)))
                .collect(java.util.stream.Collectors.toList());

        if (customerList.isEmpty()) {
            FacesContext.getCurrentInstance()
                    .addMessage(null, new FacesMessage("Customer not found."));
        }
    }

    public void resetSearch() {
        searchKeyword = "";
        customerList = customersFacade.findAll();
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getExternalContext().getFlash().setKeepMessages(true);
        try {
            ctx.getExternalContext().redirect("customers.xhtml");
        } catch (Exception e) {
        }
    }

    public void delete(Customers c) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            Customers real = customersFacade.find(c.getCustomerID());
            if (real == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Customer not found!", null));
                return;
            }

            Users linkedUser = real.getUserID();

            real.setUserID(null);
            customersFacade.edit(real);
            customersFacade.remove(real);
            if (linkedUser != null) {
                Users managedUser = usersFacade.find(linkedUser.getUserID());
                if (managedUser != null) {
                    usersFacade.remove(managedUser);
                }
            }
            refreshList();
            if (userMB != null) {
                userMB.refreshList();
            }

            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Customer deleted successfully!", null));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error deleting customer!", null));
        }
    }

    @PostConstruct
    public void refreshList() {
        customerList = customersFacade.findAll();
    }
}
