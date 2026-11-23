package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderVoucher {
    private int id;
    private int voucherId;
    private int orderId;
    private BigDecimal discountApplied;
    private Timestamp appliedAt;
}
