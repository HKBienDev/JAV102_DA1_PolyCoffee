package entity;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    private int id;
    private int categoryId;
    private String name;
    private String description;
    private String imageUrl;
    private boolean available;
    private Timestamp createdAt;
}
