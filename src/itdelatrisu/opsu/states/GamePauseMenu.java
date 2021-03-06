/*
 * opsu! - an open-source osu! client
 * Copyright (C) 2014 Jeffrey Han
 *
 * opsu! is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * opsu! is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with opsu!.  If not, see <http://www.gnu.org/licenses/>.
 */

package itdelatrisu.opsu.states;

import itdelatrisu.opsu.GUIMenuButton;
import itdelatrisu.opsu.GameImage;
import itdelatrisu.opsu.MusicController;
import itdelatrisu.opsu.Opsu;
import itdelatrisu.opsu.SoundController;
import itdelatrisu.opsu.Utils;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
import org.newdawn.slick.state.transition.FadeOutTransition;

/**
 * "Game Pause/Fail" state.
 * <ul>
 * <li>[Continue] - unpause game (return to game state)
 * <li>[Retry]    - restart game (return to game state)
 * <li>[Back]     - return to song menu state
 * </ul>
 */
public class GamePauseMenu extends BasicGameState {
	/**
	 * Music fade-out time, in milliseconds.
	 */
	private static final int FADEOUT_TIME = 1000;

	/**
	 * Track position when the pause menu was loaded (for FADEOUT_TIME).
	 */
	private long pauseStartTime;

	/**
	 * "Continue", "Retry", and "Back" buttons.
	 */
	private GUIMenuButton continueButton, retryButton, backButton;

	// game-related variables
	private GameContainer container;
	private StateBasedGame game;
	private Input input;
	private int state;

	public GamePauseMenu(int state) {
		this.state = state;
	}

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
		this.container = container;
		this.game = game;
		input = container.getInput();
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		// background
		if (Game.getRestart() != Game.RESTART_LOSE)
			GameImage.PAUSE_OVERLAY.getImage().draw();
		else
			GameImage.FAIL_BACKGROUND.getImage().draw();

		// draw buttons
		if (Game.getRestart() != Game.RESTART_LOSE)
			continueButton.draw();
		retryButton.draw();
		backButton.draw();

		Utils.drawFPS();
		Utils.drawCursor();
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		Utils.updateCursor(delta);
	}

	@Override
	public int getID() { return state; }

	@Override
	public void keyPressed(int key, char c) {
		// game keys
		if (!Keyboard.isRepeatEvent()) {
			if (key == Options.getGameKeyLeft())
				mousePressed(Input.MOUSE_LEFT_BUTTON, input.getMouseX(), input.getMouseY());
			else if (key == Options.getGameKeyRight())
				mousePressed(Input.MOUSE_RIGHT_BUTTON, input.getMouseX(), input.getMouseY());
		}

		switch (key) {
		case Input.KEY_ESCAPE:
			// 'esc' will normally unpause, but will return to song menu if health is zero
			if (Game.getRestart() == Game.RESTART_LOSE) {
				MusicController.stop();
				MusicController.playAt(MusicController.getOsuFile().previewTime, true);
				SoundController.playSound(SoundController.SOUND_MENUBACK);
				game.enterState(Opsu.STATE_SONGMENU, new FadeOutTransition(Color.black), new FadeInTransition(Color.black));
			} else
				unPause(Game.RESTART_FALSE);
			break;
		case Input.KEY_F12:
			Utils.takeScreenShot();
			break;
		}
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		if (button == Input.MOUSE_MIDDLE_BUTTON)
			return;

		boolean loseState = (Game.getRestart() == Game.RESTART_LOSE);

		// if music faded out (i.e. health is zero), don't process any actions before FADEOUT_TIME
		if (loseState && System.currentTimeMillis() - pauseStartTime < FADEOUT_TIME)
			return;

		if (continueButton.contains(x, y) && !loseState)
			unPause(Game.RESTART_FALSE);
		else if (retryButton.contains(x, y)) {
			unPause(Game.RESTART_MANUAL);
		} else if (backButton.contains(x, y)) {
			MusicController.pause();  // lose state
			MusicController.playAt(MusicController.getOsuFile().previewTime, true);
			SoundController.playSound(SoundController.SOUND_MENUBACK);
			game.enterState(Opsu.STATE_SONGMENU, new FadeOutTransition(Color.black), new FadeInTransition(Color.black));
		}
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game)
			throws SlickException {
		pauseStartTime = System.currentTimeMillis();
		if (Game.getRestart() == Game.RESTART_LOSE) {
			MusicController.fadeOut(FADEOUT_TIME);
			SoundController.playSound(SoundController.SOUND_FAIL);
		} else
			MusicController.pause();
	}

	/**
	 * Unpause and return to the Game state.
	 */
	private void unPause(byte restart) {
		if (restart == Game.RESTART_MANUAL)
			SoundController.playSound(SoundController.SOUND_MENUHIT);
		else
			SoundController.playSound(SoundController.SOUND_MENUBACK);
		Game.setRestart(restart);
		game.enterState(Opsu.STATE_GAME);
	}

	/**
	 * Loads all game pause/fail menu images.
	 */
	public void loadImages() {
		int width = container.getWidth();
		int height = container.getHeight();

		// initialize buttons
		continueButton = new GUIMenuButton(GameImage.PAUSE_CONTINUE.getImage(), width / 2f, height * 0.25f);
		retryButton = new GUIMenuButton(GameImage.PAUSE_RETRY.getImage(), width / 2f, height * 0.5f);
		backButton = new GUIMenuButton(GameImage.PAUSE_BACK.getImage(), width / 2f, height * 0.75f);

		// pause background image
		if (!GameImage.PAUSE_OVERLAY.isScaled()) {
			GameImage.PAUSE_OVERLAY.setImage(GameImage.PAUSE_OVERLAY.getImage().getScaledCopy(width, height));
			GameImage.PAUSE_OVERLAY.getImage().setAlpha(0.7f);
			GameImage.PAUSE_OVERLAY.setScaled();
		}

		// fail image
		if (!GameImage.FAIL_BACKGROUND.isScaled()) {
			GameImage.FAIL_BACKGROUND.setImage(GameImage.FAIL_BACKGROUND.getImage().getScaledCopy(width, height));
			GameImage.FAIL_BACKGROUND.getImage().setAlpha(0.7f);
			GameImage.FAIL_BACKGROUND.setScaled();
		}
	}
}
