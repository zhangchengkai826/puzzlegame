package com.mikeriv.ssui_2016.puzzlegame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameBoard;
import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameState;
import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameTile;
import com.mikeriv.ssui_2016.puzzlegame.util.PuzzleImageUtil;
import com.mikeriv.ssui_2016.puzzlegame.view.PuzzleGameTileView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PuzzleGameActivity extends AppCompatActivity {

    // The default grid size to use for the puzzle game 4 => 4x4 grid
    private static final int DEFAULT_PUZZLE_BOARD_SIZE = 4;

    // The id of the image to use for our puzzle game
    private static final int TILE_IMAGE_ID = R.drawable.kitty;

    /**
     * Button Listener that starts a new game - this must be attached to the new game button
     */
    private final View.OnClickListener mNewGameButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // TODO start a new game if a new game button is clicked
            mGameState = PuzzleGameState.PLAYING;
            AlertDialog alertDialog = new AlertDialog.Builder(PuzzleGameActivity.this).create();
            alertDialog.setTitle("Alert");
            alertDialog.setMessage("New Game");
            alertDialog.show();
        }
    };

    /**
     * Click Listener that Handles Tile Swapping when we click on a tile that is
     * neighboring the empty tile - this must be attached to every tileView in the grid
     */
    private final View.OnClickListener mGameTileOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
           // TODO handle swapping tiles and updating the tileViews if there is a valid swap
            // with an empty tile
            // If any changes happen, be sure to update the state of the game to check for a win
            // condition
        }
    };

    // Game State - what the game is currently doin
    private PuzzleGameState mGameState = PuzzleGameState.NONE;

    // The size of our puzzle board (mPuzzleBoardSize x mPuzzleBoardSize grid)
    private int mPuzzleBoardSize = DEFAULT_PUZZLE_BOARD_SIZE;

    // The puzzleboard model
    private PuzzleGameBoard mPuzzleGameBoard;

    // Views
    private TextView mScoreTextView;

    // The views for the puzzleboardtile models
    private PuzzleGameTileView[][] mPuzzleTileViews =
            new PuzzleGameTileView[mPuzzleBoardSize][mPuzzleBoardSize];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_game);

        mScoreTextView = (TextView) findViewById(R.id.text_score);

        findViewById(R.id.btnNewGame).setOnClickListener(mNewGameButtonOnClickListener);

        // Initializes the game and updates the game state
        initGame();
    }

    /**
     * Creates the puzzleboard and the PuzzleGameTiles that serve as the model for the game. It
     * does image slicing to get the appropriate bitmap subdivisions of the TILE_IMAGE_ID. It
     * then creates a set for PuzzleGameTileViews that are used to display the information in models
     */
    private void initGame() {
        mPuzzleGameBoard = new PuzzleGameBoard(mPuzzleBoardSize, mPuzzleBoardSize);

        // Get the original image bitmap
        Bitmap fullImageBitmap = BitmapFactory.decodeResource(getResources(), TILE_IMAGE_ID);
        // Now scale the bitmap so it fits out screen dimensions and change aspect ratio (scale) to
        // fit a square
        int fullImageWidth = fullImageBitmap.getWidth();
        int fullImageHeight = fullImageBitmap.getHeight();
        int squareImageSize = (fullImageWidth > fullImageHeight) ? fullImageWidth : fullImageHeight;
        fullImageBitmap = Bitmap.createScaledBitmap(
                fullImageBitmap,
                squareImageSize,
                squareImageSize,
                false);

        // TODO calculate the appropriate size for each puzzle tile
        int tileSize = fullImageBitmap.getWidth() / mPuzzleBoardSize;

        // TODO create the PuzzleGameTiles for the PuzzleGameBoard using sections of the bitmap.
        // You may find PuzzleImageUtil helpful for getting sections of the bitmap
        // Also ensure the last tile (the bottom right tile) is set to be an "empty" tile
        // (i.e. not filled with an section of the original image)
        for(int r = 0; r < mPuzzleGameBoard.getRowsCount(); r++){
            for(int c = 0; c < mPuzzleGameBoard.getColumnsCount(); c++){
                Bitmap bitmapSection = PuzzleImageUtil.getSubdivisionOfBitmap(fullImageBitmap,
                        tileSize, tileSize, r, c);
                Drawable drawable = new BitmapDrawable(getApplicationContext().getResources(),
                        bitmapSection);
                boolean isEmpty = false;
                if(r == mPuzzleBoardSize-1 && c == mPuzzleBoardSize-1)
                    isEmpty = true;
                PuzzleGameTile tile = new PuzzleGameTile(r*mPuzzleBoardSize+c,
                        drawable, isEmpty);
                mPuzzleGameBoard.setTile(tile, r, c);
            }
        }

        final LinearLayout rootView = (LinearLayout)findViewById(R.id.layout_game_display);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        // TODO createPuzzleTileViews with the appropriate width, height
                        int screenWidth = rootView.getWidth();
                        int screenHeight = rootView.getHeight();

                        int resid = (screenHeight - screenWidth) / 2;
                        float residWeight = (float)resid / screenHeight;
                        float mainContentWeight = 1 - 2*residWeight;

                        LinearLayout boardContainer = (LinearLayout)findViewById(R.id.boardContainer);
                        LinearLayout btnScoreContainer = (LinearLayout)findViewById(R.id.btnScoreContainer);
                        LinearLayout topPadding = (LinearLayout)findViewById(R.id.topPadding);

                        LinearLayout.LayoutParams lpBoardContainer = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                0,
                                mainContentWeight
                        );
                        boardContainer.setLayoutParams(lpBoardContainer);

                        LinearLayout.LayoutParams lpBtnScoreContainer = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                0,
                                residWeight
                        );
                        btnScoreContainer.setLayoutParams(lpBtnScoreContainer);

                        LinearLayout.LayoutParams lpTopPadding = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                0,
                                residWeight
                        );
                        topPadding.setLayoutParams(lpTopPadding);

                        int minTileSize = screenWidth / mPuzzleBoardSize;
                        createPuzzleTileViews(minTileSize, minTileSize);

                        shufflePuzzleTiles();

                        updateGameState();
                    }
                });
    }

    /**
     * Creates a set of tile views based on the tileWidth and height
     * @param minTileViewWidth the minimum width of the tile
     * @param minTileViewHeight the minimum height of the tile
     */

    private void createPuzzleTileViews(int minTileViewWidth, int minTileViewHeight) {
        int rowsCount = mPuzzleGameBoard.getRowsCount();
        int colsCount = mPuzzleGameBoard.getColumnsCount();
        // TODO Set up TileViews (that will be what the user interacts with)
        // Make sure each tileView gets a click listener for interaction
        // Be sure to set the appropriate LayoutParams so that your tileViews
        // So that they fit your gameboard properly\
        LinearLayout boardContainer = (LinearLayout)findViewById(R.id.boardContainer);
        for(int r = 0; r < rowsCount; r++){
            LinearLayout rowContainer = new LinearLayout(getApplicationContext());
            rowContainer.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lpRowContainer = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    0.25f
            );
            rowContainer.setLayoutParams(lpRowContainer);

            for(int c = 0; c < colsCount; c++){
                PuzzleGameTile tile = mPuzzleGameBoard.getTile(r, c);
                PuzzleGameTileView tileView = new PuzzleGameTileView(getApplicationContext(),
                       r*mPuzzleBoardSize+c, minTileViewWidth, minTileViewHeight);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(minTileViewWidth,
                       minTileViewHeight);
                tileView.setLayoutParams(lp);
                tileView.setImageDrawable(tile.getDrawable());
                if(tile.isEmpty())
                   tileView.setVisibility(View.INVISIBLE);

                LinearLayout imageContainer = new LinearLayout(getApplicationContext());
                imageContainer.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams lpImageContainer = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0.25f
                );
                imageContainer.setLayoutParams(lpImageContainer);
                imageContainer.addView(tileView);

                rowContainer.addView(imageContainer);
            }
            boardContainer.addView(rowContainer);
        }
    }

    /**
     * Shuffles the puzzle tiles randomly such that tiles may only swap if they are swapping with
     * an empty tile to maintain solvability
     */
    private void shufflePuzzleTiles() {
        // TODO randomly shuffle the tiles such that tiles may only move spots if it is randomly
        // swapped with a neighboring empty tile
        int i = mPuzzleBoardSize * mPuzzleBoardSize - 1;
        while(i > 0) {
            int j = (int)Math.floor(Math.random() * i);
            int ci = i % mPuzzleBoardSize;
            int ri = i / mPuzzleBoardSize;
            int cj = j % mPuzzleBoardSize;
            int rj = j / mPuzzleBoardSize;
            mPuzzleGameBoard.swapTiles(ri, ci, rj, cj);
            --i;
        }
        resetEmptyTileLocation();
        forceSolvable();
    }

    /**
     * Places the empty tile in the lower right corner of the grid
     */
    private void resetEmptyTileLocation() {
        // TODO
        int rowCount = mPuzzleGameBoard.getRowsCount();
        int colCount = mPuzzleGameBoard.getColumnsCount();
        for(int r = 0; r < rowCount; r++){
            for(int c = 0; c < colCount; c++){
                if(mPuzzleGameBoard.isEmptyTile(r, c)){
                    mPuzzleGameBoard.swapTiles(r, c,
                            rowCount-1, colCount-1);
                    break;
                }
            }
        }
    }

    private int sumInversions() {
        int rowCount = mPuzzleGameBoard.getRowsCount();
        int colCount = mPuzzleGameBoard.getColumnsCount();
        ArrayList<Integer> prev = new ArrayList<>();
        int sum = 0;
        for(int r = 0; r < rowCount; r++){
            for(int c = 0; c < colCount; c++){
                int id = mPuzzleGameBoard.getTile(r, c).getOrderIndex();
                prev.add(id);
                int inv = id;
                for(Integer i : prev){
                    if(i < id){
                        inv--;
                    }
                }
                sum += inv;
            }
        }
        return sum;
    }

    private boolean isSolvable(){
        return sumInversions() % 2 == 0;
    }

    private void forceSolvable() {
        if(isSolvable())
            return;
        mPuzzleGameBoard.swapTiles(0, 0, 0, 1);
    }

    /**
     * Updates the game state by checking if the user has won. Also triggers the tileViews to update
     * their visuals based on the gameboard
     */
    private void updateGameState() {
        // TODO refresh tiles and handle winning the game and updating score

    }

    private void refreshGameBoardView() {
        // TODO update the PuzzleTileViews with the data stored in the PuzzleGameBoard
    }


    /**
     * Checks the game board to see if the tile indices are in proper increasing order
     * @return true if the tiles are in correct order and the game is won
     */
    private boolean hasWonGame() {
        // TODO check if the user has won the game
        return false;

    }

    /**
     * Updates the score displayed in the text view
     */
    private void updateScore() {
        // TODO update a score to be displayed to the user
    }

    /**
     * Begins a new game by shuffling the puzzle tiles, changing the game state to playing
     * and showing a start message
     */
    private void startNewGame() {
        // TODO - handle starting a new game by shuffling the tiles and showing a start message,
        // and updating the game state
    }


}
