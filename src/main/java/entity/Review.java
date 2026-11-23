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
public class Review {
    private int id;
    private int userId;
    private int productId;
    private int orderId;
    private int rating;
    private String comment;
    private Timestamp createdAt;
}
