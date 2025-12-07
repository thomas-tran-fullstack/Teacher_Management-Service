package managebean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sessions.RevenueServiceLocal;

@Named("revenueMB")
@RequestScoped
public class RevenueMB {

    @EJB
    private RevenueServiceLocal revenueService;

    public String getInventoryLabelsJson() {
        List<Object[]> rows = revenueService.getInventoryByBrand();
        List<String> labels = new ArrayList<>();
        for (Object[] r : rows) {
            labels.add(escapeJs(r[0].toString()));
        }
        return toJsonArray(labels);
    }

    public String getInventoryValuesJson() {
        List<Object[]> rows = revenueService.getInventoryByBrand();
        List<Long> vals = new ArrayList<>();
        for (Object[] r : rows) {
            vals.add(((Number) r[1]).longValue());
        }
        return toJsonArray(vals);
    }

    public String getMonthlyRevenueLabelsJson() {
        List<Object[]> rows = revenueService.getMonthlyRevenue(12);
        List<String> labels = new ArrayList<>();
        for (Object[] r : rows) {
            Integer yr = (Integer) r[0];
            Integer mth = (Integer) r[1];
            labels.add('"' + monthLabel(mth) + ' ' + yr + '"');
        }
        // rows may be less than 12 if no data; ensure at least empty array
        return "[" + String.join(",", labels) + "]";
    }

    public String getMonthlyRevenueValuesJson() {
        List<Object[]> rows = revenueService.getMonthlyRevenue(12);
        List<String> vals = new ArrayList<>();
        for (Object[] r : rows) {
            BigDecimal v = (BigDecimal) r[2];
            vals.add(v.toString());
        }
        return "[" + String.join(",", vals) + "]";
    }

    public String getThisMonthRevenueStr() {
        BigDecimal v = revenueService.getThisMonthRevenue();
        return formatCurrency(v);
    }

    public String getYtdRevenueStr() {
        BigDecimal v = revenueService.getYtdRevenue();
        return formatCurrency(v);
    }

    public Long getProductsSoldThisMonth() {
        return revenueService.getProductsSoldThisMonth();
    }

    public Long getInventoryTotal() {
        return revenueService.getTotalInventoryCount();
    }

    private String escapeJs(String s) {
        return s.replace("\"", "\\\"");
    }

    private String toJsonArray(List<?> list) {
        List<String> parts = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof Number) parts.add(o.toString()); else parts.add('"' + escapeJs(o.toString()) + '"');
        }
        return "[" + String.join(",", parts) + "]";
    }

    private String monthLabel(int m) {
        String[] names = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        if (m >= 1 && m <= 12) return names[m-1];
        return Integer.toString(m);
    }

    private String formatCurrency(BigDecimal v) {
        if (v == null) return "$0";
        return "$" + String.format("%,.2f", v.doubleValue());
    }
}
