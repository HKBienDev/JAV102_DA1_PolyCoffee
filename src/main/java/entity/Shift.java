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
public class Shift {
    private int id;
    private int userId;
    private String shiftName;
    private Timestamp startTime;
    private Timestamp endTime;
    private BigDecimal initialCash;
    private BigDecimal actualCash;
    private String status;
}
