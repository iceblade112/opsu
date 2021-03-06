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

package itdelatrisu.opsu;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

/**
 * OsuGroupNode sorts.
 */
public enum SongSort {
	TITLE   (0, "Title",   new TitleOrder()),
	ARTIST  (1, "Artist",  new ArtistOrder()),
	CREATOR (2, "Creator", new CreatorOrder()),
	BPM     (3, "BPM",     new BPMOrder()),
	LENGTH  (4, "Length",  new LengthOrder());

	/**
	 * The ID of the sort (used for tab positioning).
	 */
	private int id;

	/**
	 * The name of the sort.
	 */
	private String name;

	/**
	 * The comparator for the sort.
	 */
	private Comparator<OsuGroupNode> comparator;

	/**
	 * The tab associated with the sort (displayed in Song Menu screen).
	 */
	private GUIMenuButton tab;

	/**
	 * Total number of sorts.
	 */
	private static final int SIZE = SongSort.values().length;

	/**
	 * Array of SongSort objects in reverse order.
	 */
	private static final SongSort[] VALUES_REVERSED;
	static {
		VALUES_REVERSED = SongSort.values();
		Collections.reverse(Arrays.asList(VALUES_REVERSED));
	}

	/**
	 * Current sort.
	 */
	private static SongSort currentSort = TITLE;

	/**
	 * Returns the current sort.
	 * @return the current sort
	 */
	public static SongSort getSort() { return currentSort; }

	/**
	 * Sets a new sort.
	 * @param sort the new sort
	 */
	public static void setSort(SongSort sort) { SongSort.currentSort = sort; }

	/**
	 * Draws all sort tabs.
	 */
	public static void drawAll() {
		Image tabImage = currentSort.tab.getImage();
		float tabTextY = currentSort.tab.getY() - (tabImage.getHeight() / 2f);
		for (SongSort sort : VALUES_REVERSED) {
			float tabTextX = sort.tab.getX() - (Utils.FONT_MEDIUM.getWidth(sort.name) / 2);
			tabImage.setAlpha((sort == currentSort) ? 1.0f : 0.7f);
			sort.tab.draw();
			Utils.FONT_MEDIUM.drawString(tabTextX, tabTextY, sort.name, Color.white);
		}
	}

	/**
	 * Compares two OsuGroupNode objects by title.
	 */
	private static class TitleOrder implements Comparator<OsuGroupNode> {
		@Override
		public int compare(OsuGroupNode v, OsuGroupNode w) {
			return v.osuFiles.get(0).title.compareToIgnoreCase(w.osuFiles.get(0).title);
		}
	}

	/**
	 * Compares two OsuGroupNode objects by artist.
	 */
	private static class ArtistOrder implements Comparator<OsuGroupNode> {
		@Override
		public int compare(OsuGroupNode v, OsuGroupNode w) {
			return v.osuFiles.get(0).artist.compareToIgnoreCase(w.osuFiles.get(0).artist);
		}
	}

	/**
	 * Compares two OsuGroupNode objects by creator.
	 */
	private static class CreatorOrder implements Comparator<OsuGroupNode> {
		@Override
		public int compare(OsuGroupNode v, OsuGroupNode w) {
			return v.osuFiles.get(0).creator.compareToIgnoreCase(w.osuFiles.get(0).creator);
		}
	}

	/**
	 * Compares two OsuGroupNode objects by BPM.
	 */
	private static class BPMOrder implements Comparator<OsuGroupNode> {
		@Override
		public int compare(OsuGroupNode v, OsuGroupNode w) {
			return Integer.compare(v.osuFiles.get(0).bpmMax, w.osuFiles.get(0).bpmMax);
		}
	}

	/**
	 * Compares two OsuGroupNode objects by length.
	 * Uses the longest beatmap in each set for comparison.
	 */
	private static class LengthOrder implements Comparator<OsuGroupNode> {
		@Override
		public int compare(OsuGroupNode v, OsuGroupNode w) {
			int vMax = 0, wMax = 0;
			for (OsuFile osu : v.osuFiles) {
				if (osu.endTime > vMax)
					vMax = osu.endTime;
			}
			for (OsuFile osu : w.osuFiles) {
				if (osu.endTime > wMax)
					wMax = osu.endTime;
			}
			return Integer.compare(vMax, wMax);
		}
	}

	/**
	 * Constructor.
	 * @param id the ID of the sort (for tab positioning)
	 * @param name the sort name
	 * @param comparator the comparator for the sort
	 */
	SongSort(int id, String name, Comparator<OsuGroupNode> comparator) {
		this.id = id;
		this.name = name;
		this.comparator = comparator;
	}

	/**
	 * Initializes the sort tab.
	 * @param img the tab image
	 * @param width the container width
	 * @param height the container height
	 */
	public void init(Image img, int width, int height) {
		float buttonX = width * 0.6f;
		float tabOffset = (width - buttonX - img.getWidth()) / (SIZE - 1);
		this.tab = new GUIMenuButton(img,
				(buttonX + (img.getWidth() / 2f)) + (id * tabOffset),
				(height * 0.15f) - (img.getHeight() / 2f) - 2f
		);
	}

	/**
	 * Returns the comparator for the sort.
	 * @return the comparator
	 */
	public Comparator<OsuGroupNode> getComparator() { return comparator; }

	/**
	 * Checks if the coordinates are within the image bounds.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return true if within bounds
	 */
	public boolean contains(float x, float y) { return tab.contains(x, y); }
}