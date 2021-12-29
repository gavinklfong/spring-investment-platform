package space.gavinklfong.invest.forex.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForexTradeDeal {

	private Long id;
	
	private String dealRef;
	
	private LocalDateTime timestamp;
		
	private String baseCurrency;
	
	private String counterCurrency;
	
	private Double rate;

	private TradeAction tradeAction;

	private BigDecimal baseCurrencyAmount;

	private Long customerId;

}
