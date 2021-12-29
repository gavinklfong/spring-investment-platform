package space.gavinklfong.invest.forex.dtos;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ForexPricing {

	private String baseCurrency;
	private String counterCurrency;
	private Integer buyPip;	
	private Integer sellPip;
	
	public Integer getSpread() {
		return buyPip - sellPip;
	}
	
}
