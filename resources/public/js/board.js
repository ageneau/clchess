function someFunc() {
    console.log("This is a test");
}

function someNewFunc(other) {
    console.log("This is a test2");
    other();
    console.log("This is a test3");
}

var chess;
var ground;


function chessToDests() {
    var dests = {};
    chess.SQUARES.forEach(function(s) {
        var ms = chess.moves({square: s, verbose: true});
        if (ms.length) dests[s] = ms.map(function(m) { return m.to; });
    });
    return dests;
}

function chessToColor() {
    return (chess.turn() == "w") ? "white" : "black";
}

function onMove(orig, dest) {
    console.log("onMove3!");
    chess.move({from: orig, to: dest});
    color = chessToColor();
    dests = chessToDests();
    console.log("color:" + color + " dests:" + JSON.stringify(dests));
    ground.set({
        turnColor: color,
        movable: {
            color: color,
            dests: dests
        }
    });
    console.log(ground.getFen());
};

function initParams (colorFunc, destSquares, onMoveFunc) {
    return {
        viewOnly: false,
        turnColor: 'white',
        animation: {
            duration: 500
        },
        movable: {
            free: false,
            color: colorFunc(),
            premove: true,
            dests: destSquares(),
            events: {
                after: onMoveFunc
            }
        },
        drawable: {
            enabled: true
        }
    }
}

function initBoard (chessObj, groundObj, onMoveFunc, colorFunc, destSquares) {
    console.log("initBoard");
    chess = chessObj;
    // ground = Chessground(groundObj, {
    //     viewOnly: false,
    //     turnColor: 'white',
    //     animation: {
    //         duration: 500
    //     },
    //     movable: {
    //          free: false,
    //          color: colorFunc(),
    //          premove: true,
    //          dests: destSquares(),
    //          events: {
    //          after: onMoveFunc
    //          }
    //     },
    //     drawable: {
    //         enabled: true
    //     }
    // });
    ground = Chessground(groundObj, initParams(colorFunc, destSquares, onMoveFunc));
    window.cg6 = ground;
}

function initBoard2 (groundObj, initParams) {
    console.log("initBoard2");
    ground = Chessground(groundObj, initParams);
    window.cg6 = ground;
}

function initBoard4 (ground) {
    console.log("initBoard4");
    window.cg6 = ground;
}
