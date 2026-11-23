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
public class Recipe {
    private int id;
    private int productSizeId;
    private int ingredientId;
    private BigDecimal quantityRequired;
}
