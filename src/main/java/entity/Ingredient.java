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
public class Ingredient {
    private int id;
    private String name;
    private String itemType;
    private String unit;
    private BigDecimal stockQuantity;
    private BigDecimal minStock;
}
