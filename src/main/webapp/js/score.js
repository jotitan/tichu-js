/* Display the score */

var Scorer = {
    div:null,
    divScore1:null,
    divScore2:null,
    init:function(){
        this.div = $('#idScoreDiv')
    },
    addResult:function(result){
        this._showScore($('.scoreLeft',this.div),$('.totalScoreLeft',this.div),result.score1,true);
        this._showScore($('.scoreRight',this.div),$('.totalScoreRight',this.div),result.score2,false);
    },
    showWinner:function(teamWin){
        if(teamWin == 0){
            this._displayWinner($('.totalScoreLeft',this.div));
        }else{
            this._displayWinner($('.totalScoreRight',this.div));
        }
    },
    _displayWinner:function(div){
        div.append("<br/>" + Win);
    },
    _showScore:function(divResult,divTotalScore,score,onLeft){
        var div = $('<div></div>');
        if(score.capot){
            div.append('-');
        }else{
            div.append('<span>' + score.score + '</span>');
            if(score.annonce!=null){
                var annonce = $('<span>' + ((score.annonce == 'TICHU')?'  T  ':'  GT  ') + '</span>');
                if(!score.win){
                    annonce.css('text-decoration','line-through');
                }
                onLeft ? div.prepend(annonce) : div.append(annonce);
            }
        }
        divResult.append(div);

        divTotalScore.html(score.cumulateScore);
    },
    newgame:function(){
        this.div.empty();
    }
}