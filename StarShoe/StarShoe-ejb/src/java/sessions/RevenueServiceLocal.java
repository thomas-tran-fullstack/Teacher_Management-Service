package sessions;

import java.math.BigDecimal;
import java.util.List;

public interface RevenueServiceLocal {
    // Returns list of Object[]{Integer year, Integer month, BigDecimal total}
    List<Object[]> getMonthlyRevenue(int monthsBack);

    // Returns list of Object[]{String brandName, Long quantity}
    List<Object[]> getInventoryByBrand();

    // Returns total products sold in current month
    Long getProductsSoldThisMonth();

    // Returns total inventory count (sum of product quantities)
    Long getTotalInventoryCount();

    // Returns this month revenue
    BigDecimal getThisMonthRevenue();

    // Returns YTD revenue
    BigDecimal getYtdRevenue();
}
