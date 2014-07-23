package tichu.model;

import fr.titan.tichu.model.ws.CardWS;

/**
 *
 */
public class SpecialCard extends Card {
    private int score;
    private int code;
    private boolean isPhoenix = false;

    public SpecialCard(CardType cardType, int code, int score) {
        super(cardType);
        this.score = score;
        this.code = code;
    }

    public int getValue() {
        return code;
    }

    public int getScore() {
        return score;
    }

    enum SpecialCardFactory {
        Phoenix {
            Card get() {
                return new SpecialCard(CardType.PHOENIX, 16, -25);
            }
        },
        Dogs {
            Card get() {
                return new SpecialCard(CardType.DOGS, 15, 0);
            }
        },
        Mahjong {
            Card get() {
                return new SpecialCard(CardType.MAHJONG, 1, 0);
            }
        },
        Dragon {
            Card get() {
                return new SpecialCard(CardType.DRAGON, 17, 25);
            }
        };

        abstract Card get();
    }

    @Override
    public CardWS toCardWS() {
        return new CardWS(this.code, "white");
    }

    public String toString() {
        return this.getType().toString();
    }
}
