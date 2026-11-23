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
public class OrderItemTopping {
    private int id;
    private int orderItemId;
    private int toppingId;
    private int quantity;
    private BigDecimal unitPrice;
}
