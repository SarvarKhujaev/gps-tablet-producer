package com.ssd.mvd.gpstabletsservice.AssomidinService;

import org.springframework.messaging.rsocket.RSocketRequester;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import reactor.core.publisher.Mono;
import lombok.Data;

@Data
public class AssomidinService {
    private static AssomidinService assomidinService = new AssomidinService();
    private final RSocketRequester requester = RSocketRequester.builder().tcp( "10.254.1.1", 5050 );

    public static AssomidinService getInstance() { return assomidinService != null ? assomidinService : ( assomidinService = new AssomidinService() ); }

    public Mono< Card > getCard ( Long cardId ) { return this.requester.route( "getCardById" ).data( cardId ).retrieveMono( Card.class ); }
}
