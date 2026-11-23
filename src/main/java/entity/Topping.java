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
public class Topping {
    private int id;
    private String name;
    private BigDecimal price;
    private Integer ingredientId;
    private BigDecimal quantityRequired;
    private boolean available;
}
