package sa.gov.saip.workflow.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class CamundaWebClientConfig {

    @Bean
    WebClient camundaWebClient(CamundaProperties properties) {
        Duration connectTimeout = properties.connectTimeout() == null ? Duration.ofSeconds(5) : properties.connectTimeout();
        Duration readTimeout = properties.readTimeout() == null ? Duration.ofSeconds(30) : properties.readTimeout();

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectTimeout.toMillis()))
                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(readTimeout.toMillis(), TimeUnit.MILLISECONDS)));

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(properties.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                        .build());

        if (properties.username() != null && !properties.username().isBlank()) {
            builder.defaultHeaders(headers -> headers.setBasicAuth(properties.username(), properties.password() == null ? "" : properties.password()));
        }

        return builder.build();
    }
}
