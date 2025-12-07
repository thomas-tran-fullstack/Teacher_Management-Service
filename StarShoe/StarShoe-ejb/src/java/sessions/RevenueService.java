package sessions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class RevenueService implements RevenueServiceLocal {

    @PersistenceContext(unitName = "StarShoe-ejbPU")
    private EntityManager em;

    @Override
    public List<Object[]> getMonthlyRevenue(int monthsBack) {
        // Compute cutoff date in Java to avoid DB-specific date functions and named parameter issues in native SQL
        java.time.LocalDate cutoffLocal = java.time.LocalDate.now().minusMonths(monthsBack - 1).withDayOfMonth(1);
        java.sql.Date cutoff = java.sql.Date.valueOf(cutoffLocal);

        String sql = "SELECT YEAR(OrderDate) yr, MONTH(OrderDate) mth, COALESCE(SUM(TotalAmount),0) total "
                + "FROM Orders "
                + "WHERE OrderDate >= ? "
                + "GROUP BY YEAR(OrderDate), MONTH(OrderDate) "
                + "ORDER BY YEAR(OrderDate), MONTH(OrderDate)";

        @SuppressWarnings("unchecked")
        List<Object[]> raw = em.createNativeQuery(sql)
                .setParameter(1, cutoff)
                .getResultList();

        List<Object[]> out = new ArrayList<>();
        for (Object r : raw) {
            Object[] row = (Object[]) r;
            out.add(new Object[]{((Number) row[0]).intValue(), ((Number) row[1]).intValue(), (row[2] == null ? BigDecimal.ZERO : new BigDecimal(row[2].toString()))});
        }
        return out;
    }

    @Override
    public List<Object[]> getInventoryByBrand() {
        String sql = "SELECT b.BrandName, COALESCE(SUM(p.Quantity),0) qty "
                + "FROM Brands b LEFT JOIN Products p ON p.BrandID = b.BrandID "
                + "GROUP BY b.BrandName";
        @SuppressWarnings("unchecked")
        List<Object[]> raw = em.createNativeQuery(sql).getResultList();
        List<Object[]> out = new ArrayList<>();
        for (Object r : raw) {
            Object[] row = (Object[]) r;
            String name = row[0] == null ? "Unknown" : row[0].toString();
            Long qty = row[1] == null ? 0L : ((Number) row[1]).longValue();
            out.add(new Object[]{name, qty});
        }
        return out;
    }

    @Override
    public Long getProductsSoldThisMonth() {
        java.time.LocalDate start = java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate next = start.plusMonths(1);
        java.sql.Timestamp startTs = java.sql.Timestamp.valueOf(start.atStartOfDay());
        java.sql.Timestamp nextTs = java.sql.Timestamp.valueOf(next.atStartOfDay());

        String sql = "SELECT COALESCE(SUM(od.Quantity),0) FROM OrderDetails od "
            + "JOIN Orders o ON od.OrderID = o.OrderID "
            + "WHERE o.OrderDate >= ? AND o.OrderDate < ?";
        Object res = em.createNativeQuery(sql).setParameter(1, startTs).setParameter(2, nextTs).getSingleResult();
        return res == null ? 0L : ((Number) res).longValue();
    }

    @Override
    public Long getTotalInventoryCount() {
        String sql = "SELECT COALESCE(SUM(p.Quantity),0) FROM Products p";
        Object res = em.createNativeQuery(sql).getSingleResult();
        return res == null ? 0L : ((Number) res).longValue();
    }

    @Override
    public BigDecimal getThisMonthRevenue() {
        java.time.LocalDate start = java.time.LocalDate.now().withDayOfMonth(1);
        java.time.LocalDate next = start.plusMonths(1);
        java.sql.Timestamp startTs = java.sql.Timestamp.valueOf(start.atStartOfDay());
        java.sql.Timestamp nextTs = java.sql.Timestamp.valueOf(next.atStartOfDay());
        String sql = "SELECT COALESCE(SUM(o.TotalAmount),0) FROM Orders o WHERE o.OrderDate >= ? AND o.OrderDate < ?";
        Object res = em.createNativeQuery(sql).setParameter(1, startTs).setParameter(2, nextTs).getSingleResult();
        return res == null ? BigDecimal.ZERO : new BigDecimal(res.toString());
    }

    @Override
    public BigDecimal getYtdRevenue() {
        java.time.LocalDate start = java.time.LocalDate.now().withDayOfYear(1);
        java.sql.Timestamp startTs = java.sql.Timestamp.valueOf(start.atStartOfDay());
        String sql = "SELECT COALESCE(SUM(o.TotalAmount),0) FROM Orders o WHERE o.OrderDate >= ?";
        Object res = em.createNativeQuery(sql).setParameter(1, startTs).getSingleResult();
        return res == null ? BigDecimal.ZERO : new BigDecimal(res.toString());
    }

}
