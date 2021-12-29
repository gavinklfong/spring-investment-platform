package space.gavinklfong.invest.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import space.gavinklfong.invest.forex.clients.ForexClient;
import space.gavinklfong.invest.forex.dtos.ForexRate;

@Slf4j
@RestController
@RequestMapping("/forex")
public class ForexController {

    @Autowired
    private ForexClient forexClient;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/rates")
    public Flux<ForexRate> getRates() {
        return forexClient.getLatestRates();
    }

}
