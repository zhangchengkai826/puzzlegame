package com.mikeriv.ssui_2016.puzzlegame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameBoard;
import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameState;
import com.mikeriv.ssui_2016.puzzlegame.model.PuzzleGameTile;
import com.mikeriv.ssui_2016.puzzlegame.util.PuzzleImageUtil;
import com.mikeriv.ssui_2016.puzzlegame.view.PuzzleGameTileView;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mScoreTextView = (TextView) findViewById(R.id.text_score);

        findViewById(R.id.btnNewGame).setOnClickListener(mNewGameButtonOnClickListener);

        // Initializes the game and updates the game state
        initGame();
        updateGameState();
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

        // TODO createPuzzleTileViews with the appropriate width, height
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        LinearLayout outerContainer = (LinearLayout)findViewById(R.id.outerContainer);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)
                outerContainer.getLayoutParams();
        int margin = lp.leftMargin;
        int minTileWidth = (screenWidth-2*margin)/mPuzzleBoardSize;
        int minTileHeight = (screenHeight-2*margin)/mPuzzleBoardSize;
        int minTileSize = (minTileHeight > minTileWidth) ? minTileWidth : minTileHeight;
        createPuzzleTileViews(minTileSize, minTileSize);
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
        LinearLayout outerContainer = (LinearLayout)findViewById(R.id.outerContainer);
        for(int r = 0; r < rowsCount; r++){
            for(int c = 0; c < colsCount; c++){
               PuzzleGameTile tile = mPuzzleGameBoard.getTile(r, c);
               if(!tile.isEmpty()){
                   PuzzleGameTileView tileView = new PuzzleGameTileView(getApplicationContext(),
                           r*mPuzzleBoardSize+c, minTileViewWidth, minTileViewHeight);
                   ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(minTileViewWidth,
                           minTileViewHeight);
                   tileView.setLayoutParams(lp);
                   tileView.setImageDrawable(tile.getDrawable());
                   outerContainer.addView(tileView);
               }
            }
        }
    }

    /**
     * Shuffles the puzzle tiles randomly such that tiles may only swap if they are swapping with
     * an empty tile to maintain solvability
     */
    private void shufflePuzzleTiles() {
        // TODO randomly shuffle the tiles such that tiles may only move spots if it is randomly
        // swapped with a neighboring empty tile

    }

    /**
     * Places the empty tile in the lower right corner of the grid
     */
    private void resetEmptyTileLocation() {
        // TODO
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