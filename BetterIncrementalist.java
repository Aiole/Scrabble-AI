import java.io.StreamCorruptedException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class BetterIncrementalist implements ScrabbleAI {
    /**
     * When exchanging, always exchange everything.
     */
    private static final boolean[] ALL_TILES = {true, true, true, true, true, true, true};

    /**
     * The GateKeeper through which this Incrementalist accesses the Board.
     */
    private GateKeeper gateKeeper;

    @Override
    public void setGateKeeper(GateKeeper gateKeeper) {
        this.gateKeeper = gateKeeper;
    }

    @Override
    public ScrabbleMove chooseMove() {
        if (gateKeeper.getSquare(Location.CENTER) == Board.DOUBLE_WORD_SCORE) {
            return findStartingMove();
        }
        return findMove();
    }

    private class ScoreWordContainer {
        int score;
        String word;
        public ScoreWordContainer(int score, String word) {
            this.score = score;
            this.word = word;
        }
    };

    private ScoreWordContainer tryPermutations(char[] wordArr) {
        String bestWord = null;
        int bestScore = -1;
        PermutationIterator perm = new PermutationIterator(new String(wordArr));
        while (perm.hasNext()) {
            String word = perm.next();
            try {
                gateKeeper.verifyLegality(word, Location.CENTER, Location.HORIZONTAL);
                int score = gateKeeper.score(word, Location.CENTER, Location.HORIZONTAL);
                if (score > bestScore) {
                    bestScore = score;
                    bestWord = word;
                }
            } catch (IllegalMoveException e) {
                //wasn't legal, go to the next permutation
            }
        }
        return new ScoreWordContainer(bestScore, bestWord);
    }


    /**
     * This finds a move for the first turn, using four, five, or six letters
     */
    private ScrabbleMove findStartingMove() {
        ArrayList<Character> hand = gateKeeper.getHand();
        String bestWord = null;
        int bestScore = -1;
        for (int i = 0; i < hand.size(); i++) {
            for (int j = i + 1; j < hand.size(); j++) {
                for (int k = j + 1; k < hand.size(); k++) {
                    for (int l = k + 1; l < hand.size(); l++) {
                        for (int m = l + 1; m < hand.size(); m++) {
                            for (int n = m + 1; n < hand.size(); n++) {
                                //Make absolutely sure that we aren't using the same letter twice, and that we don't pick up the same combination of letters
                                if (i != j && i != k && i != l && i != m && i != n && j != k && j != l && j != m && j != n && k != l && k != m && k != n && l != m && l != n && m != n) {

                                    char A = hand.get(i);
                                    if (A == '_') {
                                        A = 'S'; // S was the best letter that we found for scoring better
                                    }
                                    char B = hand.get(j);
                                    if (B == '_') {
                                        B = 'S';
                                    }

                                    char E = hand.get(k);
                                    if (E == '_') {
                                        E = 'S';
                                    }

                                    char F = hand.get(l);
                                    if (F == '_') {
                                        F = 'S';
                                    }

                                    char G = hand.get(m);
                                    if (G == '_') {
                                        G = 'S';
                                    }

                                    char H = hand.get(n);
                                    if (H == '_') {
                                        H = 'S';
                                    }

                                    char[] pool = {A,B,E,F,G,H};

                                    for (int iNeedMoreIndices = pool.length; iNeedMoreIndices >= 4; iNeedMoreIndices--) {
                                        char[] poolSubRange = new char[iNeedMoreIndices];
                                        for (int dearGodWhy = 0; dearGodWhy < iNeedMoreIndices; dearGodWhy++) {
                                            poolSubRange[dearGodWhy] = pool[dearGodWhy];
                                        }
                                        ScoreWordContainer swc = tryPermutations(poolSubRange);
                                        if (swc.word != null) {
                                            bestScore = swc.score;
                                            bestWord = swc.word;
                                            break;
                                        }
                                    }

//                                    PermutationIterator perm6 = new PermutationIterator(new String(new char[]{A,B,E,F,G,H}));
//                                    while (perm6.hasNext()) {
//                                        String word = perm6.next();
//                                        try {
//                                            gateKeeper.verifyLegality(word, Location.CENTER, Location.HORIZONTAL);
//                                            int score = gateKeeper.score(word, Location.CENTER, Location.HORIZONTAL);
//                                            if (score > bestScore) {
//                                                bestScore = score;
//                                                bestWord = word;
//                                            }
//                                        } catch (IllegalMoveException e) {
//                                            //wasn't legal, go to the next combination
//                                        }
//                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
        if (bestScore > -1) {
            return new PlayWord(bestWord, Location.CENTER, Location.HORIZONTAL);
        }
        return new ExchangeTiles(ALL_TILES);
    }


    /**
     * Finds a move of one of the following specifications:
     * 1. A three letter word which makes incidental words with what is on the board
     * 2. A four letter word which includes one thing from the board
     * 3. A two letter word including a letter from the board
     */
    private ScrabbleMove findMove() {
        ArrayList<Character> hand = gateKeeper.getHand();
        PlayWord bestMove = null;
        int bestScore = -1;
        for (int i = 0; i < hand.size(); i++) {
            for (int j = i+1; j < hand.size(); j++) {
                for (int k = j+1; k < hand.size(); k++) {


                    if (i != j && i != k && j != k) {

                        char a = hand.get(i);
                        if (a == '_') {
                            a = 'S'; // This could be improved slightly by trying all possibilities for the blank
                        }
                        char b = hand.get(j);
                        if (b == '_') {
                            b = 'S';
                        }

                        char c = hand.get(k);
                        if (c == '_') {
                            c = 'S';
                        }

                        for (String word : new String[]{
                                //four letter words
                                c + a + b + " ", c + b + a + " ", a + c + b + " ", b + c + a + " ", a + b + c + " ", b + a + c + " ",
                                " " + a + b + c, " " + b + a + c, " " + c + b + a, " " + c + a + b, " " + a + c + b, " " + b + c + a,
                                a + c + " " + b, b + c + " " + a, c + a + " " + b, c + b + " " + a, b + a + " " + c, a + b + " " + c,
                                a + " " + b + c, a + " " + c + b, b + " " + a + c, b + " " + c + a, c + " " + a + b, c + " " + b + a,
                                //3-letter words
                                ""+a+b+c, ""+a+c+b, ""+b+c+a, ""+b+a+c, ""+c+a+b, ""+c+b+a,
                                //2-letter words
                                " "+a, " "+b, " "+c, c+" ", b+" ", a+" "

                        }) {
                            for (int row = 0; row < Board.WIDTH; row++) {//check all locations on the board
                                for (int col = 0; col < Board.WIDTH; col++) {
                                    Location location = new Location(row, col);
                                    for (Location direction : new Location[]{Location.HORIZONTAL, Location.VERTICAL}) {
                                        try {
                                            gateKeeper.verifyLegality(word, location, direction);
                                            int score = gateKeeper.score(word, location, direction);
                                            if (score > bestScore) {
                                                bestScore = score;
                                                bestMove = new PlayWord(word, location, direction);
                                            }
                                        } catch (IllegalMoveException e) {
                                            // It wasn't legal; go on to the next one
                                        }
                                    }

                                }
                            }

                        }
                    }

                }
            }
        }


        if (bestMove != null) {
            return bestMove;
        }
        return new ExchangeTiles(ALL_TILES);
    }
}
