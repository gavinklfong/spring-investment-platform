package space.gavinklfong.invest.forex.clients;

import org.springframework.web.bind.annotation.*;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import space.gavinklfong.invest.forex.clients.config.ForexClientConfig;
import space.gavinklfong.invest.forex.dtos.*;

import java.util.Optional;

@ReactiveFeignClient(name="forex-client", url="${app.forex.service.url}", configuration = ForexClientConfig.class)
public interface ForexClient {

    @RequestMapping(method = RequestMethod.GET, value = "/rates/latest")
    Flux<ForexRate> getLatestRates();

    @RequestMapping(method = RequestMethod.GET, value = "/rates/latest/{baseCurrency}/{counterCurrency}")
    Mono<ForexRate> getLatestRateByCurrency(@PathVariable String baseCurrency, @PathVariable String counterCurrency);

    @RequestMapping(method = RequestMethod.POST, value = "/rates/book")
    Mono<ForexRateBooking> bookRate(@RequestBody ForexRateBookingReq req);

    @RequestMapping(method = RequestMethod.POST, value = "/deals")
    Mono<ForexTradeDeal> submitDeal(@RequestBody ForexTradeDealReq req);

    @RequestMapping(method = RequestMethod.GET, value = "/deals")
    Flux<ForexTradeDeal> getDeals(@RequestParam Long customerId, @RequestParam Optional<String> dealRef);

}
