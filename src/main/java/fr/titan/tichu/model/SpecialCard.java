package fr.titan.tichu.model;

/**
 *
 */
public class SpecialCard extends Card {

    public SpecialCard(CardType cardType,int value){
        super(cardType);
    }

    enum SpecialCardFactory {
        Phoenix{
            Card get(){
                return new SpecialCard(CardType.PHOENIX,-25);
            }
        },Dogs{
            Card get(){
                return new SpecialCard(CardType.DOGS,0);
            }
        },Mahjong{
            Card get(){
                return new SpecialCard(CardType.MAHJONG,0);
            }
        },Dragon{
            Card get(){
                return new SpecialCard(CardType.DRAGON,25);
            }
        };


        abstract Card get();
    }
}
