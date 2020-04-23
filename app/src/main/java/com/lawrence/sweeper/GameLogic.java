package com.lawrence.sweeper;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

//Needed to ignore "Replace for with Arrays.fill" due to object getting placed by reference instead
//of a new instance in function wipeBoard()
@SuppressWarnings("ALL")
public class GameLogic implements Parcelable {
    //[][0]: 0 = covered, 1 = uncovered, 2 = flagged. [][1] = 0 -> 8 = surrounding bombs, 9 = bomb
    private int[][] gridArray;
    //0: difficulty, 1: bombs, 2: width, 3: height
    private int[] difficultyStats;
    private int totalTiles;

    private Set<Integer> bombs;

    private boolean boardSet = false;
    private boolean gameOver = false;

    private int flaggedTiles = 0;

    private GameLogicListener listener;

    private tileIDs longPressID = tileIDs.FLAGGED;
    private tileIDs shortPressID = tileIDs.UNCOVERED;

    GameLogic(int difficulty){
        //Setup visual grid
        difficultyStats = new difficulties().getDifficulty(difficulty);
        totalTiles = difficultyStats[2]*difficultyStats[3];
        wipeBoard();
    }


    private GameLogic(Parcel in) {
        difficultyStats = in.createIntArray();
        totalTiles = in.readInt();
        boardSet = in.readByte() != 0;
        gameOver = in.readByte() != 0;
        flaggedTiles = in.readInt();
    }

    public static final Creator<GameLogic> CREATOR = new Creator<GameLogic>() {
        @Override
        public GameLogic createFromParcel(Parcel in) {
            return new GameLogic(in);
        }

        @Override
        public GameLogic[] newArray(int size) {
            return new GameLogic[size];
        }
    };

    private void generateGrid(int initialLocation){

        wipeBoard();

        Random random = new Random();
        bombs = new HashSet<>(difficultyStats[1]+1);
        bombs.add(initialLocation);
        while(bombs.size()< difficultyStats[1]+1) {
            //noinspection StatementWithEmptyBody
            while (!bombs.add(random.nextInt(totalTiles)));
        }
        bombs.remove(initialLocation);
        for (Integer loc : bombs) {
            gridArray[loc] = new int[]{0, 9};
        }
        for (Integer loc : bombs) {
            handleSurroundings(loc, false);

        }
    }

    void reset(){
        listener.gameStarted(false);
        totalTiles = difficultyStats[2]*difficultyStats[3];
        boardSet = false;
        gameOver = false;
        flaggedTiles = 0;
        listener.tileFlagged(getFlaggedTiles());
        wipeBoard();
    }


    @SuppressWarnings("SingleStatementInBlock")
    private void wipeBoard(){
        gridArray = new int[totalTiles][2];
        //Arrays.fill(gridArray, new int[]{0, 0});
        for(int x = 0; x < gridArray.length; x++){
            gridArray[x] = new int[]{0,0};
        }
    }

    public void updateBlock(int location, boolean longPress) {
        if (!boardSet) {
            //First press is never a bomb, places all bombs and starts game
            generateGrid(location);
            boardSet = true;
            listener.gameStarted(true);
        }

        if (gameOver || gridArray[location][0] == tileIDs.UNCOVERED.getId()) {
            //Don't let the player press more tiles
            return;
        }

        int[] locationInfo = gridArray[location];
        if(longPress){
            handlePress(location, locationInfo, longPressID);
        } else {
            handlePress(location, locationInfo, shortPressID);
        }

        listener.tileFlagged(getFlaggedTiles());

        if(locationInfo[0] == tileIDs.UNCOVERED.getId() && locationInfo[1] == 9){
            //check if user uncovered a bomb
            revealBombs();
            gameOver = true;
            listener.gameOver(false);
            return;
        }

        if(checkWin()){
            //Check if the user won
            listener.gameOver(true);
            gameOver = true;
        }
    }

    private void handlePress(int location, int[] locationInfo, tileIDs pressType){
        if(locationInfo[0] == tileIDs.FLAGGED.getId()){
            gridArray[location][0] = tileIDs.COVERED.getId();
            flaggedTiles--;
        } else {
            if (gridArray[location][1] == 0 && pressType == tileIDs.UNCOVERED) {
                revealSurrounding(location);
            } else {
                gridArray[location][0] = pressType.getId();
            }
        }
        if(pressType == tileIDs.FLAGGED){
            flaggedTiles++;
        }
    }

    private void revealBombs(){
        for (Integer loc : bombs) {
            gridArray[loc][0] = tileIDs.UNCOVERED.getId();
        }
    }

    private void revealSurrounding(int loc){
        //if 0, check more. if not 0, reveal and stop
        if(gridArray[loc][0] != tileIDs.UNCOVERED.getId()) {
            if(gridArray[loc][0] == tileIDs.FLAGGED.getId()){
                flaggedTiles--;
            }
            gridArray[loc][0] = tileIDs.UNCOVERED.getId();
            if (gridArray[loc][1] == 0) {
                handleSurroundings(loc, true);
            }
        }
    }

    //reveal: true = reveal, false = bomb placement
    private void handleSurroundings(int loc, boolean reveal){
        int gridLength = difficultyStats[2];
        int topLeft = loc - gridLength - 1;
        int top = loc - gridLength;
        int topRight = loc - gridLength + 1;
        int left = loc - 1;
        int right = loc + 1;
        int bottomLeft = loc + gridLength - 1;
        int bottom = loc + gridLength;
        int bottomRight = loc + gridLength + 1;

        handleTile(topLeft, reveal, topLeft%gridLength<gridLength-1);
        handleTile(left, reveal, left%gridLength<gridLength-1);
        handleTile(bottomLeft, reveal, bottomLeft%gridLength<gridLength-1);

        handleTile(topRight, reveal, topRight%gridLength>0);
        handleTile(right, reveal, right%gridLength>0);
        handleTile(bottomRight, reveal, bottomRight%gridLength>0);

        handleTile(top, reveal, true);
        handleTile(bottom, reveal, true);
    }

    private void handleTile(int pos, boolean reveal, boolean statement){
        if (pos>=0 && pos<gridArray.length && gridArray[pos][1] != 9 && statement) {
            if(reveal) {
                revealSurrounding(pos);
            } else {
                gridArray[pos][1]++;
            }
        }
    }

    private boolean checkWin(){
        int flaggedBombs = 0;
        for(Integer loc : bombs){
            if(gridArray[loc][0] == 2){
                flaggedBombs++;
            }
        }
        if(flaggedBombs == difficultyStats[1]){
            return true;
        }
        int revealedTiles = 0;
        for (int[] ints : gridArray) {
            if (ints[0] == 1) {
                revealedTiles++;
            } else if (ints[1] != 9) {
                return false;
            }
        }
        return revealedTiles == (totalTiles) - bombs.size();
    }
    boolean isGameOver(){
        return gameOver;
    }
    boolean gameOn() {
        return boardSet;
    }

    public int[][] getGridArray(){
        return gridArray;
    }
    void setDifficulty(int difficulty){
        difficultyStats = new difficulties().getDifficulty(difficulty);
    }
    public int getGridWidth(){
        return difficultyStats[2];
    }
    public int getGridHeight(){
        return difficultyStats[3];
    }
    int getDifficulty(){
        return difficultyStats[0];
    }
    int getFlaggedTiles(){
        return difficultyStats[1] - flaggedTiles;
    }
    void changeLongPress(boolean flag){
        if(flag){
            longPressID = tileIDs.UNCOVERED;
            shortPressID = tileIDs.FLAGGED;
        } else {
            longPressID = tileIDs.FLAGGED;
            shortPressID = tileIDs.UNCOVERED;

        }
    }

    void setGameLogicListener(GameLogicListener listener){
        this.listener = listener;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeIntArray(difficultyStats);
        parcel.writeInt(totalTiles);
        parcel.writeByte((byte) (boardSet ? 1 : 0));
        parcel.writeByte((byte) (gameOver ? 1 : 0));
        parcel.writeInt(flaggedTiles);
    }


    public interface GameLogicListener {
        void gameStarted(boolean started);
        void gameOver(boolean win);
        void tileFlagged(int flags);
    }

    enum tileIDs {
        COVERED(0), UNCOVERED(1), FLAGGED(2);
        private int id;
        tileIDs(int i) {
            this.id = i;
        }
        int getId(){
            return this.id;
        }
    }
}
