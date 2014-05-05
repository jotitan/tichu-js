package fr.titan.tichu.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.titan.tichu.model.AnnonceType;
import fr.titan.tichu.model.ws.Fold;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.ws.ChangeCards;
import fr.titan.tichu.model.ws.RequestWS;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.service.cache.game.GameCache;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Lead message between server and gamers
 */
@Singleton
public class MessageService {

    @Inject
    private GameService gameService;

    @Inject
    private GameCache gameCache;

    public void treatMessage(String token, String message) {
        ObjectMapper om = new ObjectMapper();
        try {
            RequestWS request = om.reader(RequestWS.class).readValue(message);
            switch (request.getType()) {
            case SUITE_CARDS:
                gameService.getSuiteCards(token);
                break;
            case ANNONCE:
                gameService.makeAnnonce(token, AnnonceType.valueOf(request.getValue()));
                break;
            case CALL:
                gameService.callTurn(token);
                break;
            case CHANGE_CARDS:
                ChangeCards cards = (ChangeCards) readObject(request.getValue(), ChangeCards.class);
                gameService.playerChangeCard(token, cards);
                break;
            case BOMB:
                Fold bomb = (Fold) readObject(request.getValue(), Fold.class);
                gameService.playBomb(token, bomb);
                break;
            case FOLD:
                Fold fold = (Fold) readObject(request.getValue(), Fold.class);
                gameService.playFold(token, fold);
                break;
            case HEARTBEAT:
                gameCache.heartbeat(token);
                break;
            }
        } catch (Exception e) {
        }
    }



    private Object readObject(String value, Class c) throws Exception {
        ObjectMapper om = new ObjectMapper();
        return om.reader(c).readValue(value);
    }

}
