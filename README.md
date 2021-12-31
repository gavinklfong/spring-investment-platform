# Reactive Feign Demo


This project comes with an API client `ForexClient` which integrates with Forex API for forex rates retrieval and booking.

The Forex API service provides the following endpoints:

- [GET] /rates/latest
- [GET] /rates/latest/{base-currency/{counter-currency}
- [POST] /rates/book

The purpose of this project is to demonstrate the creation of API client using [Reactive Feign](https://github.com/Playtika/feign-reactive)

Automated unit test `ForexClientTest` is provided with stub creation using [WireMock](http://wiremock.org/).