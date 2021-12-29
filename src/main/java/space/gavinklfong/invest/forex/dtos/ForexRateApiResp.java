package space.gavinklfong.invest.forex.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class ForexRateApiResp {

	private String id;

	private Map<String, Double> rates;

	private String base;

	private LocalDate date;

	public ForexRateApiResp() {
		super();
	}

	public ForexRateApiResp(String base, LocalDate date, Map<String, Double> rates) {
		super();
		this.base = base;
		this.date = date;
		this.rates = rates;
	}


}
