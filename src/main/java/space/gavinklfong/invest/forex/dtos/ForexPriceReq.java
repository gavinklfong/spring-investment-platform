package space.gavinklfong.invest.forex.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForexPriceReq {

	private String baseCurrency;
	private String counterCurrency;
	private Double rate;
	
}
