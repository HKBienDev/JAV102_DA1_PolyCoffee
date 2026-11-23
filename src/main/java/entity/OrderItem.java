package entity;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    private int id;
    private int orderId;
    private int productSizeId;
    private int quantity;
    private BigDecimal unitPrice;
    private String sugarLevel;
    private String iceLevel;
    private BigDecimal subtotal;
}
