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
public class InventoryTransaction {
    private int id;
    private int ingredientId;
    private Integer userId;
    private Integer orderId;
    private String transactionType;
    private BigDecimal quantityChange;
    private String note;
    private Timestamp createdAt;
}
