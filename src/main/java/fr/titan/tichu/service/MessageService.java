package fr.titan.tichu.service;

import fr.titan.tichu.model.ws.Fold;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.ws.ChangeCards;
import fr.titan.tichu.model.ws.RequestWS;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Lead message between server and gamers
 */
public class MessageService {

    private GameService gameService = new GameService();

    public void treatMessage(Player player, String message) {
        ObjectMapper om = new ObjectMapper();
        try {
            RequestWS request = om.reader(RequestWS.class).readValue(message);
            switch (request.getType()) {
            case CALL:
                gameService.callTurn(player);
                break;
            case CHANGE_CARDS:
                ChangeCards cards = (ChangeCards) readObject(request.getValue(), ChangeCards.class);
                gameService.playerChangeCard(player, cards);
                break;
            case BOMB:
                Fold bomb = (Fold) readObject(request.getValue(), Fold.class);
                bomb.setPlayer(player.getOrientation());
                gameService.playBomb(player, bomb);
                break;
            case FOLD:
                Fold fold = (Fold) readObject(request.getValue(), Fold.class);
                fold.setPlayer(player.getOrientation());
                gameService.playFold(player, fold);
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
