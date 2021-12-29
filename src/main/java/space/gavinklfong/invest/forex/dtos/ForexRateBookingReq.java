package space.gavinklfong.invest.forex.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForexRateBookingReq {

	@NotEmpty
	private String baseCurrency;

	@NotEmpty
	private String counterCurrency;

	@NotNull
	@Positive
	private BigDecimal baseCurrencyAmount;

	@NotNull
	private TradeAction tradeAction;

	@NotNull
	private Long customerId;


}
