package com.gesturedefence.util;

/**
 * @author Michael Watts
 * @since 22:28:21 - 16 Jun 2011
 */

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.util.HorizontalAlign;

import android.app.Activity;
import android.widget.Toast;

import com.gesturedefence.GestureDefence;
import com.openfeint.api.resource.Leaderboard;
import com.openfeint.api.resource.Score;
import com.openfeint.api.ui.Dashboard;

public class ScreenManager {
	
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
	private GestureDefence base;
	public Scene MainMenu;
	public Scene GameScreen;
	public Scene NewWaveScene;
	public Scene EndWaveScene;
	public Scene GameOverScene;
	public Scene PauseScreen;
	
	private ChangeableText scorebits;
	
	// ========================================
	// Constructors
	// ========================================
	
	// ========================================
	// Getter & Setter
	// ========================================
	
	public ScreenManager(GestureDefence base)
	{
		this.base = base;
	}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
	
	// ========================================
	// Methods
	// ========================================
	
	public void loadMainMenu()
	{
		if(MainMenu == null)
		{
			MainMenu = new Scene(1);
		
			/* Setup the scrolling background, can be removed, was just trying it out */
			
			MainMenu.setBackground(base.autoParallaxBackground);
			/* End of scrolling Background */
			
			int buttonX = (base.getCameraWidth() / 2) - (base.getStartButtonRegion().getWidth() / 2);
			int buttonY = (base.getCameraHeight() / 2)  - (base.getStartButtonRegion().getHeight() / 2);
			
			Sprite startButton = new Sprite(buttonX, buttonY, base.getStartButtonRegion()) {
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.ButtonPress(1);
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			buttonX = (base.getCameraWidth() / 2) - (base.getQuitButtonRegion().getWidth() / 2);
			buttonY += base.getStartButtonRegion().getHeight();
			
			Sprite quitButton = new Sprite(buttonX, buttonY, base.getQuitButtonRegion()) {
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.ButtonPress(99);
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			Text LOADGame = new Text(10, 10, base.mFont2, "Load Game Save")
			{
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.loadSaveFile();					
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			Text openFeintOption = new Text(10, base.getCameraHeight() - LOADGame.getHeight(), base.mFont2, "Load Game Save")
			{
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					Dashboard.open();
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			MainMenu.attachChild(startButton);
			MainMenu.attachChild(quitButton);
			MainMenu.attachChild(LOADGame);
			MainMenu.attachChild(openFeintOption);
			MainMenu.registerTouchArea(startButton);
			MainMenu.registerTouchArea(quitButton);
			MainMenu.registerTouchArea(LOADGame);
			MainMenu.registerTouchArea(openFeintOption);
			MainMenu.setTouchAreaBindingEnabled(true);
			
			base.ambient.setLooping(true);
			base.ambient.play();
		}
		
		if (base.gethud() != null)
			if (base.gethud().isVisible())
				base.gethud().setVisible(false);
		base.getEngine().setScene(MainMenu);
	}
	
	public void GameScreen()
	{
		if (GameScreen == null)
		{
			GameScreen = new Scene(1);
		
			GameScreen.setBackground(base.autoParallaxBackground);
			GameScreen.registerUpdateHandler(base.sRemoveStuff);
			
			base.loadCastle(base.getCameraWidth() - (base.getCastleTexture().getWidth()), base.getCameraHeight() - 60 - base.getCastleTexture().getHeight());
			base.loadHud();
			
			base.sm.GameScreen.registerUpdateHandler(new IUpdateHandler() {
				@Override
				public void onUpdate(float pSecondsElapsed) {
					/* On every update */
					if (base.gameLoaded)
					{
						base.gameLoaded = false;
						//Remove? Cause a crash, can't work out why.. annoying!
						//Toast.makeText(base.getApplication(), "Game Loaded!", Toast.LENGTH_SHORT).show();
					}					
					
					if (base.sPreviousWaveNum != base.theWave.getWaveNumber() && base.sKillCount != base.sPreviousKillCount)
						if ((base.sKillCount - base.sPreviousKillCount) == base.theWave.getNumberEnemysToSpawn())
						{
							/* Oh they all dead */
							base.theWave.mCashAmountItem.setText("CASH : " + base.sMoney);
							base.theWave.mBuyMenuItem.setText("HEALTH : " + base.sCastle.getCurrentHealth() + "/ " + base.sCastle.getMaxHealth());
							base.sEndWaveActive = true;
							base.sPreviousWaveNum = base.theWave.getWaveNumber();
							base.sPreviousKillCount += base.theWave.getNumberEnemysToSpawn();
							base.sm.EndWaveScreen();
						}
					if (base.lightning != null)
						if (base.lightning.isAnimationRunning() == false)
						{
							base.sm.GameScreen.detachChild(base.lightning);
							if (base.mLightningBolt == true)
								base.sm.GameScreen.registerUpdateHandler(new TimerHandler(1 / 3.0f, true, new ITimerCallback() {
									@Override
									public void onTimePassed(TimerHandler pTimerHandler) {
										base.sm.GameScreen.unregisterUpdateHandler(pTimerHandler);
										base.mLightningBolt = false;
										base.mLightningBoltX = 0;
										base.mLightningBoltY = 0;
									}
								}));
						}
				}

				@Override
				public void reset() {
					// TODO Auto-generated method stub
					
				}
			});
		}
		
		if (base.gethud() != null)
			if (base.gethud().isVisible() == false)
				base.gethud().setVisible(true);
		base.getEngine().setScene(GameScreen);
	}
	
	public void NewWaveScreen()
	{
		if (NewWaveScene == null)
		{
			NewWaveScene = new Scene(1);
			
			base.theWave.mWaveNumberMenuItem = new ChangeableTextMenuItem(GestureDefence.MENU_WAVE_NUMBER, base.mFont, "WAVE : " + base.theWave.getWaveNumber(), ("WAVE : XXXX").length());
			NewWaveScene.attachChild(base.theWave.mWaveNumberMenuItem);
		}		
		
		base.theWave.mWaveNumberMenuItem.setPosition((base.getCameraWidth() / 2) - (base.theWave.mWaveNumberMenuItem.getWidth() / 2), (base.getCameraHeight() / 2) - (base.theWave.mWaveNumberMenuItem.getHeight() / 2));

		if (base.gethud() != null)
			if (base.gethud().isVisible())
				base.gethud().setVisible(false);
		base.getEngine().setScene(NewWaveScene);
	}
	
	public void EndWaveScreen()
	{
		if (EndWaveScene == null)
		{
			EndWaveScene = new Scene(1);
			
			EndWaveScene.setBackground(new ColorBackground(0.0f, 0.0f, 1.0f));
			
			Sprite buyButton = new Sprite(base.getCameraWidth() - base.getCameraWidth(), base.getCameraHeight() - base.getBuyButton().getHeight() , base.getBuyButtonRegion()) {
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.ButtonPress(5);
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			Sprite NextWaveButton = new Sprite(base.getCameraWidth() - base.getNextWaveButton().getWidth(), base.getCameraHeight() - base.getNextWaveButton().getHeight(), base.getNextWaveButtonRegion()) {
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.ButtonPress(3);
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			Text increaseMaxHealth = new Text(100, 300, base.mFont, "Increase Max Health")
			{
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.ButtonPress(7);
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			Text saveGame = new Text(100, 250, base.mFont, "Save Game")
			{
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.savegame();					
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			EndWaveScene.attachChild(base.theWave.mCashAmountItem);
			EndWaveScene.attachChild(base.theWave.mBuyMenuItem);
			EndWaveScene.attachChild(buyButton);
			EndWaveScene.registerTouchArea(buyButton);
			EndWaveScene.attachChild(increaseMaxHealth);
			EndWaveScene.registerTouchArea(increaseMaxHealth);
			EndWaveScene.attachChild(NextWaveButton);
			EndWaveScene.registerTouchArea(NextWaveButton);
			EndWaveScene.attachChild(saveGame);
			EndWaveScene.registerTouchArea(saveGame);
		}
		
		if (base.gethud() != null)
			if (base.gethud().isVisible())
				base.gethud().setVisible(false);
		base.theWave.mCashAmountItem.setPosition(100, 100);
		base.theWave.mBuyMenuItem.setPosition(100, 160);
		base.complete.play();
		base.getEngine().setScene(EndWaveScene);
		base.savegame();
	}
	
	public void GameOverScreen()
	{
		if (GameOverScene == null)
		{
			GameOverScene = new Scene(1);
			
			Text gameOverText = new Text(base.getCameraWidth() / 2, base.getCameraHeight() / 2, base.mFont, "GAME OVER!", HorizontalAlign.CENTER) {
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.getEngine().setScene(MainMenu);
					base.theWave.setWaveNumber(1);
					base.sKillCount = 0;
					base.sPreviousKillCount = 0;
					base.sPreviousWaveNum = 0;
					base.sMoney = 0;
					base.mMoneyEarned = 0;
					base.sEnemyCount = 0;
					base.updateCashValue();
					base.sCastle.setCurrentHealth(3000);
					base.sCastle.setMaxHealth(3000);
					base.updateCastleHealth();
					base.mana = 0;
					base.updateManaValue();
					
					/*remove all sprite's still in the game (enemies etc)
					 * This needs optimising, like making it only remove enemies!
					 */
					base.sm.GameScreen.detachChildren();
					
					//Reload the castle, since it has now been removed
					base.loadCastle(base.getCameraWidth() - (base.getCastleTexture().getWidth()), base.getCameraHeight() - 60 - base.getCastleTexture().getHeight());
					
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			scorebits = new ChangeableText(gameOverText.getX() - gameOverText.getWidth(), gameOverText.getY() + gameOverText.getHeight(), base.mFont, "Kills = " + base.sKillCount + ", cash = " + base.mMoneyEarned);
			GameOverScene.attachChild(gameOverText);
			GameOverScene.attachChild(scorebits);
			GameOverScene.registerTouchArea(gameOverText);
		}		
		
		scorebits.setText("Kills = " + base.sKillCount + ", cash = " + base.mMoneyEarned);
		if (base.gethud() != null)
			if (base.gethud().isVisible())
				base.gethud().setVisible(false);
		base.game_over.play();
		base.getEngine().setScene(GameOverScene);
		
		long scoreValue = base.theWave.getWaveNumber();
		Score s = new Score(scoreValue, null); // Second parameter is null to indicate that custom display text is not used.
		Leaderboard l = new Leaderboard("794006");
		s.submitTo(l, new Score.SubmitToCB() {
			@Override public void onSuccess(boolean newHighScore) {
				// sweet, score was posted
				base.setResult(Activity.RESULT_OK);
				//base.finish(); //Dur dum feature!
			}

			@Override public void onFailure(String exceptionMessage) {
				Toast.makeText(base,
				"Error (" + exceptionMessage + ") posting score.",
				Toast.LENGTH_SHORT).show();
				base.setResult(Activity.RESULT_CANCELED);
				//base.finish();
			}
		});
	}
	
	public void loadPauseScreen()
	{
		if (PauseScreen == null)
		{
			PauseScreen = new Scene(1);
			
			Text someText = new Text( (base.getCameraWidth() / 2) - 10, (base.getCameraHeight() / 2) - 10, base.mFont, "PAUSED")
			{
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.sm.GameScreen();
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			Text restartText = new Text( someText.getX(), someText.getY() - someText.getHeight(), base.mFont, "Restart")
			{
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.ButtonPress(9);
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			Text openFeintText = new Text( someText.getX(), someText.getY() + someText.getHeight(), base.mFont, "OpenFeint Menu")
			{
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					Dashboard.open();
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			PauseScreen.registerTouchArea(someText);
			PauseScreen.registerTouchArea(restartText);
			PauseScreen.registerTouchArea(openFeintText);
			PauseScreen.attachChild(someText);
			PauseScreen.attachChild(restartText);
			PauseScreen.attachChild(openFeintText);
		}		
		
		if (base.gethud() != null)
			if (base.gethud().isVisible())
				base.gethud().setVisible(false);
		base.getEngine().setScene(PauseScreen);
	}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}