/*
 * Copyright (c) 2014 tabletoptool.com team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     rptools.com team - initial implementation
 *     tabletoptool.com team - further development
 */
package com.t3.macro.api.views;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.syntax.ParserException;




import com.t3.MD5Key;
import com.t3.client.AppUtil;
import com.t3.client.T3Util;
import com.t3.client.TabletopTool;
import com.t3.client.ui.commandpanel.ChatExecutor;
import com.t3.client.ui.token.BooleanTokenOverlay;
import com.t3.client.ui.zone.ZoneRenderer;
import com.t3.client.walker.WalkerMetric;
import com.t3.client.walker.ZoneWalker;
import com.t3.client.walker.astar.AStarSquareEuclideanWalker;
import com.t3.language.I18N;
import com.t3.macro.MacroException;
import com.t3.macro.api.functions.token.TokenLocation;
import com.t3.macro.api.functions.token.TokenPart;
import com.t3.macro.api.views.InitiativeListView.InitiativeEntry;
import com.t3.model.AbstractPoint;
import com.t3.model.CellPoint;
import com.t3.model.Direction;
import com.t3.GUID;
import com.t3.model.InitiativeList;
import com.t3.model.InitiativeList.TokenInitiative;
import com.t3.model.LightSource;
import com.t3.model.MacroButtonProperties;
import com.t3.model.Path;
import com.t3.model.Token;
import com.t3.model.Token.Type;
import com.t3.model.TokenFootprint;
import com.t3.model.Zone;
import com.t3.model.Zone.Layer;
import com.t3.model.ZonePoint;
import com.t3.model.campaign.TokenProperty;
import com.t3.model.chat.TokenSpeaker;
import com.t3.model.grid.Grid;
import com.t3.model.grid.SquareGrid;
import com.t3.util.ImageManager;
import com.t3.util.TokenUtil;
import com.t3.util.math.IntPoint;

public class TokenView extends TokenPropertyView {
	public TokenView(Token token) {
		super(token);
	}
	
	@Override
	public int hashCode() {
		return token.hashCode();
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TokenView other = (TokenView) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return token.toString();
	}
	
	
	
	
	
	///////////////////////////////////////////
	// Implementation of available functions //
	///////////////////////////////////////////
	
	/**
	 * Simply writes to the chat as this token
	 * @param message a string or some other kind of objects that is written to the chat
	 */
	public void say(Object message) {
		ChatExecutor.say(message.toString(),new TokenSpeaker(token.getId().toString()));
	}
	
	/**
	 * Whispers to a certain player as this token so that only you two can see it
	 * @param message a string or some other kind of objects that is written to the chat
	 */
	public void whisper(Object message, String targetPlayer) {
		ChatExecutor.whisper(message.toString(), new TokenSpeaker(token.getId().toString()), targetPlayer);		
	}
	
	/**
	 * Whispers to to the GM as this token so that only you two can see it
	 * @param message a string or some other kind of objects that is written to the chat
	 */
	public void whisperToGM(Object message) {
		ChatExecutor.gm(message.toString(), new TokenSpeaker(token.getId().toString()));
	}

	/**
	 * This writes a message about the token to the chat
	 * @param message a string or some other kind of objects that is written to the chat
	 */
	public void emote(Object message) {
		ChatExecutor.emote(message.toString(), new TokenSpeaker(token.getId().toString()));
	}
	
	/**
	 * This whispers an answer as this token back to last person that wrote to you
	 * @param message a string or some other kind of objects that is written to the chat
	 */
	public void reply(Object message) {
		ChatExecutor.reply(message.toString(), new TokenSpeaker(token.getId().toString()));
	}
	
	/**
	 * The method returns the value of a bar or null if the bar is not visible at the moment.
	 * @param barName the name of the bar you want the value of
	 * @return the value of the bar or null if the bar is invisible
	 */
	public Float getBar(String barName) {
		return token.getBar(barName);
	}
	
	/**
	 * This method is used to set the value of a bar. It also makes the bar visible.
	 * @param barName the name of the bar you want to set the value of
	 * @param value the value the bar should have between 0 and 100
	 */
	public void setBar(String barName, float value) {
		token.setBar(barName, value);
		this.sendUpdateToServer();
	}
	
	/**
	 * Returns if a bar is currently visible
	 * @param barName the name of the bar
	 * @return if this bar is currently visible on this token
	 */
	public boolean isBarVisible(String barName) {
		return token.getBar(barName) != null;
	}

	/**
	 * This allows you to show or hide a bar. This will reset the value of the bar!
	 * @param barName the name of the bar
	 * @param show if the bar should be visible or not
	 */
	public void setBarVisible(String barName, boolean show) {
		token.setBar(barName, show ? 1f : null);
		this.sendUpdateToServer();
	}
	
	/**
	 * This adds this token to the initiative list if it is not already present there
	 * @return true if the token was added
	 */
	public boolean addToInitiative() {
		return addToInitiative(false, null);
	}
	
	/**
	 * This method adds this token to the initiative list
	 * @param allowDuplicates if this token should be added even if it is already there
	 * @return true if the token was added
	 */
	public boolean addToInitiative(boolean allowDuplicates) {
		return addToInitiative(allowDuplicates, null);
	}
	
	/**
	 * This method adds this token to the initiative list. If it is already present and you don't allow duplicates the given state will override the old one.
	 * Otherwise the given state will be the state of the new initative list entry
	 * @param allowDuplicates if this token should be added if it is already in the intiative list
	 * @param state an optional state that can be displayed with the token name (like a initiative value)
	 * @return if this token was actually added
	 */
	public boolean addToInitiative(boolean allowDuplicates, String state) {
		InitiativeList list = token.getZone().getInitiativeList();
	    // insert the token if needed
	    TokenInitiative ti = null;
	    if (allowDuplicates || !list.contains(token)) {
	        ti = list.insertToken(-1, token);
	        if (state != null) ti.setState(state);
	    } else {
	    	setTokenInitiative(state);
	        return false;
	    }
	    return ti != null;
	}
	
	/**
	 * Removes all entries of this token from the initiative list.
	 * @return the number of entries removed
	 */
	public int removeFromInitiative() {
		InitiativeList list = token.getZone().getInitiativeList();
		List<Integer> tokens = list.indexOf(token);
		list.startUnitOfWork();
	    for (int i = tokens.size() - 1; i >= 0; i--) 
	    	list.removeToken(tokens.get(i).intValue());
	    list.finishUnitOfWork();
	    return tokens.size();
	}
	
	/**
	 * Gets the name of the token.
	 * @return the name of the token.
	 */
	public String getName() {
		return token.getName();
	}
	
	/**
	 * Sets the name of the token.
	 * @param name the name of the token.
	 */
	public void setName(String name) {
		token.setName(name);
		ZoneRenderer renderer = TabletopTool.getFrame().getZoneRenderer(token.getZone());
		TabletopTool.serverCommand().putToken(renderer.getZone().getId(), token);
	}
	
	/**
	 * Calculates if a certain point on the map is visible for this Token.
	 * @param x the x coordinate of the point you want to test
	 * @param y the y coordinate of the point you want to test
	 * @return if the point is visible
	 */
	public boolean isVisible(int x, int y) {
		Area visArea = TabletopTool.getFrame().getZoneRenderer(token.getZone()).getZoneView().getVisibleArea(token);
		if (visArea == null)
			return false;
		else
			return visArea.contains(x, y);
	}
	
	/**
     * Get the first token initiative
     * @return The first token initiative value for this token
     */
    public InitiativeEntry getInitiative() {
        Zone zone = token.getZone();
        List<Integer> list = zone.getInitiativeList().indexOf(token);
        if (list.isEmpty()) return null; 
        return new InitiativeEntry(zone.getInitiativeList().getTokenInitiative(list.get(0).intValue())); 
    }
    
    /**
     * Get the all initiatives of this token
     * @return a list of the initiatives of this token
     */
    public List<InitiativeEntry> getInitiatives() {
        Zone zone = token.getZone();
        List<Integer> list = zone.getInitiativeList().indexOf(token);
        if (list.isEmpty()) return Collections.emptyList();
        List<InitiativeEntry> ret = new ArrayList<InitiativeEntry>(list.size());
        for (Integer index : list)
            ret.add(new InitiativeEntry(zone.getInitiativeList().getTokenInitiative(index.intValue())));
        return ret;
    }
    
    /**
     * This lets you set the initiative of a token.
     * @param state the new initiative of the token
     */
    public void setTokenInitiative(String state) {
    	InitiativeList list=token.getZone().getInitiativeList();
    	for(Integer index:list.indexOf(token)) {
	        TokenInitiative ti = list.getTokenInitiative(index);
	        if(ti!=null)
	        	ti.setState(state);
        }
    }
    
    /**
     * @return if this token is visible for the current player
     */
    public boolean isVisible() {
    	return !(token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) && token.isVisible();
    }
    
    /**
     * @param visible if this token should be made visible or not
     */
    public void setVisible(boolean visible) {
		token.setVisible(visible);
		this.sendUpdateToServer();
    }
 
    /**
     * @return if this token is visible to its owner only
     */
    public boolean isVisibleToOwnerOnly() {
    	return token.isVisibleOnlyToOwner();
    }
    
    /**
     * @param ownerOnlyVisible if this token should be made visible to its owner only
     */
    public void setVisibleToOwnerOnly(boolean ownerOnlyVisible) {
		token.setVisibleOnlyToOwner(ownerOnlyVisible);
		this.sendUpdateToServer();
	}
    
    /**
     * This method is used to access the speeches defined in the token properties.
     * @param speechKey the key of the speech
     * @return the text associated with the speech key
     */
    public String getSpeech(String speechKey) {
    	return token.getSpeech(speechKey);
    }
    
    /**
     * This method is used to change the speeches defined in the token properties.
     * @param speechKey the key to access the speech in the future
     * @param speech the text to store as a speech
     */
    public void setSpeech(String speechKey, String speech) {
    	token.setSpeech(speechKey, speech);
    	this.sendUpdateToServer();
    }
    
    /**
     * @return a set of all the speech keys that are defined for this token
     */
    public Set<String> getSpeechKeys() {
    	return Collections.unmodifiableSet(token.getSpeechNames());
    }
    
    /**
     * @return the notes ostored on this token
     */
    public String getNotes() {
    	return token.getNotes();
    }
    
    /**
     * This sets the notes of the token
     * @param notes the new notes
     */
    public void setNotes(String notes) {
    	token.setNotes(notes);
    	this.sendUpdateToServer();
    }
    
    /**
     * @return the GM notes of the token
     */
    public String getGMNotes() {
    	return token.getGMNotes();
    }
    
    /**
     * This sets the GM notes of the token
     * @param notes the new GM notes
     */
    public void setGMNotes(String notes) {
    	token.setGMNotes(notes);
    	this.sendUpdateToServer();
    }
    
    /**
     * This method allows you to check the token for a certain state.
     * @param state the state to check for
     * @return if the token has this state
     */
    public boolean hasState(String state) {
    	return token.hasState(state);
    }
    
    /**
     * This method allows you to check the token for a certain 
     * @param state the state you want to enable or disable 
     * @param value if this value should be enabled
     */
    public void setState(String state, boolean value) {
    	token.setState(state, value);
    	this.sendUpdateToServer();
    }
    
    /**
     * This method allows you to change all the states at once. This is usefull
     * to clear a token of all its states.
     * @param value if all values should be enabled
     */
    public void setAllStates(boolean value) {
    	for (String stateName : TabletopTool.getCampaign().getTokenStatesMap().keySet())
			token.setState(stateName, value);
		this.sendUpdateToServer();
    }

    /**
     * @return the names of all the states defined in the campaign settings
     */
    public Set<String> getStateNames() {
    	return Collections.unmodifiableSet(TabletopTool.getCampaign().getTokenStatesMap().keySet());
    }
    
    /**
     * @param group the group which state names you want
     * @return the names of all the states defined in the campaign settings for the given group
     */
    public Set<String> getStateNames(String group) {
    	Set<String> stateNames;
		Map<String, BooleanTokenOverlay> states = TabletopTool.getCampaign().getTokenStatesMap();
		stateNames = new HashSet<String>();
		for (BooleanTokenOverlay bto : states.values()) {
			if (group.equals(bto.getGroup())) {
				stateNames.add(bto.getName());
			}
		}
		return stateNames;
    }
    
	private void sendUpdateToServer() {
		TabletopTool.serverCommand().putToken(token.getZone().getId(), token);
	}

	/**
	 * @return the id of the token
	 */
	public GUID getId() {
		return token.getId();
	}
	
	/**
	 * Returns a set of the parts of a token that can be seen by this token.
	 * @param target the token of which we want to check what this token can see
	 * @return the set of visible token parts
	 */
	public EnumSet<TokenPart> getVisibleTokenParts(TokenView target) {
		if(!token.getHasSight())
			return EnumSet.noneOf(TokenPart.class);
		
		ZoneRenderer zr=TabletopTool.getFrame().getZoneRenderer(token.getZone());
		Zone zone=zr.getZone();
		Area tokensVisibleArea = zr.getZoneView().getVisibleArea(token);
		if (tokensVisibleArea == null)
			return EnumSet.noneOf(TokenPart.class);
		if (target == null)
			throw new NullPointerException();
		if (!target.isVisible() || (target.token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(target.token))) {
			return EnumSet.noneOf(TokenPart.class);
		}
		Grid grid = zone.getGrid();
		
		Rectangle bounds = target.token.getFootprint(grid).getBounds(grid, grid.convert(new ZonePoint(target.token.getX(), target.token.getY())));
		if(!target.token.isSnapToGrid())
			bounds = target.token.getBounds(zone);

		EnumSet<TokenPart> ret = EnumSet.noneOf(TokenPart.class);
		
		int x = (int) bounds.getX();
		int y = (int) bounds.getY();
		int w = (int) bounds.getWidth();
		int h = (int) bounds.getHeight();

		int halfX = x + (w) / 2;
		int halfY = y + (h) / 2;
		if (tokensVisibleArea.intersects(bounds)) {
			if (tokensVisibleArea.contains(new Point(x, y)))
				ret.add(TokenPart.TOP_LEFT);
			if (tokensVisibleArea.contains(new Point(x, y + h)))
			if (tokensVisibleArea.contains(new Point(x + w, y)))
				ret.add(TokenPart.TOP_RIGHT);
			if (tokensVisibleArea.contains(new Point(x + w, y + h)))
				ret.add(TokenPart.BOTTOM_LEFT);
			if (tokensVisibleArea.contains(new Point(halfX, halfY)))
				ret.add(TokenPart.BOTTOM_RIGHT);
		}
		return ret;
	}
	
	/**
	 * @return if this token has sight
	 */
	public boolean hasSight() {
		return token.getHasSight();
	}
	
	/**
	 * @param value if this token should have sight
	 */
	public void setHasSight(boolean value) {
		token.setHasSight(value);
		this.sendUpdateToServer();
		TabletopTool.getFrame().getZoneRenderer(token.getZone()).flushLight();
	}
	
	/**
	 * @return the type of vision this token is using
	 * @see #getHasSight
	 */
	public String getVisionType() {
		return token.getSightType();
	}
	
	/**
	 * This allows you to set the type of vision this token should use. Be aware that to token can
	 * only see things if {@link #setHasSight} is set.
	 * @param visionType the type of vision this token should use 
	 * @see #setHasSight
	 */
	public void setVisionType(String visionType) {
		token.setSightType(visionType);
		this.sendUpdateToServer();
		TabletopTool.getFrame().getZoneRenderer(token.getZone()).flushLight();
	}
	
	/**
	 * This sets the label of the token. Te label will be shown below the name of the token on the map.
	 * @param label the new label
	 */
	public void setLabel(String label) {
		token.setLabel(label);
		token.getZone().putToken(token);
		this.sendUpdateToServer();
	}
	
	/**
	 * @return the label of the token. 
	 * @see #setLabel
	 */
	public String getLabel() {
		return token.getLabel();
	}
	
	/**
	 * @return the GM name of the token
	 */
	public String getGMName() {
		return token.getGMName();
	}
	
	/**
	 * This method sets the GM name of the token
	 * @param name the new GM name
	 */
	public void setGMName(String name) {
		token.setGMName(name);
		Zone zone = token.getZone();
		TabletopTool.serverCommand().putToken(zone.getId(), token);
		zone.putToken(token);
	}
	
	/**
	 * Gets the halo for the token.
	 * @return the halo color or null if there is no halo
	 */
	public String getHalo() {
		if (token.getHaloColor() != null)
			return "#" + Integer.toHexString(token.getHaloColor().getRGB()).substring(2);
		else
			return null;
	}

	/**
	 * Sets the halo color of the token.
	 * @param hexColor the color to set in the form #AAAAAA or null to deactivate the halo
	 */
	public void setHalo(String hexColor) {
		if (hexColor == null) {
			token.setHaloColor(null);
		} else {
			Color color = T3Util.getColor(hexColor);
			token.setHaloColor(color);
		}
		
		Zone zone = token.getZone();
		zone.putToken(token);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
	}
	
	/**
	 * @return if this token has any light source
	 */
	public boolean hasLight() {
		return token.hasLightSources();
	}
	
	/**
	 * 
	 * @param category the category
	 * @return if this token has any light source of the given category
	 */
	public boolean hasLight(String category) {

			for (LightSource ls : TabletopTool.getCampaign().getLightSourcesMap().get(category).values()) {
				if (token.hasLightSource(ls))
					return true;
			}
			return false;
	}
	
	/**
	 * 
	 * @param category the category of the light source you are looking for
	 * @param name the name of the light source you are looking for
	 * @return if this token has the given light source
	 */
	public boolean hasLight(String category, String name) {
		for (LightSource ls : TabletopTool.getCampaign().getLightSourcesMap().get(category).values()) {
			if (ls.getName().equals(name)) {
				if (token.hasLightSource(ls)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * This method will remove all light sources from the token.
	 */
	public void clearLightSources() {
		token.clearLightSources();
		this.sendUpdateToServer();
		TabletopTool.getFrame().updateTokenTree();
		TabletopTool.getFrame().getZoneRenderer(token.getZone()).flushLight();
	}
	
	/**
	 * Sets the light value for a token.
	 * @param category the category of the light source.
	 * @param name  The name of the light source.
	 * @param active The value to set for the light source, false for off or true for on.
	 * @return false if the light was not found, otherwise true;
	 */
	public boolean setLight(String category, String name, boolean active) {
		boolean found = false;

		for (LightSource ls : TabletopTool.getCampaign().getLightSourcesMap().get(category).values()) {
			if (ls.getName().equals(name)) {
				found = true;
				if (active) {
					token.removeLightSource(ls);
				} else {
					token.addLightSource(ls, Direction.CENTER);
				}
				break;
			}
		}
		ZoneRenderer renderer = TabletopTool.getFrame().getZoneRenderer(token.getZone());
		Zone zone = renderer.getZone();
		zone.putToken(token);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
		TabletopTool.getFrame().updateTokenTree();
		renderer.flushLight();

		return found;
	}
	
	/**
	 * Gets the names of the light sources that are on.
	 * @return a string list containing the lights that are on.
	 */
	public List<String> getLights(Token token) {
		ArrayList<String> lightList = new ArrayList<String>();
		for (Map<GUID, LightSource> category : TabletopTool.getCampaign().getLightSourcesMap().values()) {
			for (LightSource ls : category.values()) {
				if (token.hasLightSource(ls))
					lightList.add(ls.getName());
			}
		}
		return lightList;
	}
	
	/**
	 * Gets the names of the light sources that are on.
	 * @param category The category to get the light sources for
	 * @return a string list containing the lights that are on.
	 */
	public List<String> getLights(Token token, String category) {
		ArrayList<String> lightList = new ArrayList<String>();
		for (LightSource ls : TabletopTool.getCampaign().getLightSourcesMap().get(category).values()) {
			if (token.hasLightSource(ls)) {
				lightList.add(ls.getName());
			}
		}
		return lightList;
	}
	
	/**
	 * @return the location of the token
	 */
	public TokenLocation getLocation() {
		return getLocation(true);
	}

	/**
	 * @param useDistancePerCell if you want the location as distance per cell
	 * @return the location of the token
	 */
	public TokenLocation getLocation(boolean useDistancePerCell) {
		if (useDistancePerCell) {
			Rectangle tokenBounds = token.getBounds(token.getZone());
			return new TokenLocation(tokenBounds.x, tokenBounds.y, token.getZOrder());
		} else {
			Zone zone = token.getZone();
			CellPoint cellPoint = zone.getGrid().convert(new ZonePoint(token.getX(), token.getY()));
			return new TokenLocation(cellPoint.x, cellPoint.y, token.getZOrder());
		}
	}
	
	/**
	 * @return the z order of this token. The z value gives the order in which the tokens
	 * are drawn.
	 */
	public int getZOrder() {
		return token.getZOrder();
	}
	
	/**
	 * This methods sets the z order of this token.
	 * @param newZ the new z order
	 */
	public void setZOrder(int newZ) {
		token.setZOrder(newZ);
		ZoneRenderer renderer = TabletopTool.getFrame().getZoneRenderer(token.getZone());
		Zone zone = renderer.getZone();
		zone.putToken(token);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
		renderer.flushLight();
	}
	
	/**
	 * Gets the distance between this token and this one.
	 * @param target the token to calculate the distance to
	 * @return the distance between this token and target.
	 */
	public double getDistance(TokenView target) {
		return getDistance(target,true,null);
	}
	
	/**
	 * Gets the distance between this token and this one.
	 * @param target the token to calculate the distance to
	 * @param metric the metric that should be used to calculate the distance. Can be
	 * NO_GRID, NO_DIAGONALS, MANHATTAN, ONE_TWO_ONE or ONE_ONE_ONE
	 * @return the distance between this token and target.
	 */
	public double getDistance(TokenView target, String metric) {
		return getDistance(target,true,metric);
	}
	
	/**
	 * Gets the distance between this token and this one.
	 * @param target the token to calculate the distance to
	 * @param gridUnits if you want the result in grid units
	 * @return the distance between this token and target.
	 */
	public double getDistance(TokenView target, boolean gridUnits) {
		return getDistance(target,gridUnits,null);
	}
	
	/**
	 * Gets the distance between this token and the given one.
	 * @param target the token to calculate the distance to
	 * @param gridUnits if you want the result in grid units
	 * @param metric the metric that should be used to calculate the distance. Can be
	 * NO_GRID, NO_DIAGONALS, MANHATTAN, ONE_TWO_ONE or ONE_ONE_ONE
	 * @return the distance between this token and target.
	 */
	public double getDistance(TokenView target, boolean gridUnits, String metric) {
		ZoneRenderer renderer = TabletopTool.getFrame().getZoneRenderer(token.getZone());
		Grid grid = renderer.getZone().getGrid();

		if (grid.getCapabilities().isPathingSupported() && !"NO_GRID".equals(metric)) {

			// Get which cells our tokens occupy
			Set<CellPoint> sourceCells = token.getFootprint(grid).getOccupiedCells(grid.convert(new ZonePoint(token.getX(), token.getY())));
			Set<CellPoint> targetCells = target.token.getFootprint(grid).getOccupiedCells(grid.convert(new ZonePoint(target.token.getX(), target.token.getY())));

			ZoneWalker walker;
			if (metric != null && grid instanceof SquareGrid) {
				try {
					WalkerMetric wmetric = WalkerMetric.valueOf(metric);
					walker = new AStarSquareEuclideanWalker(renderer.getZone(), wmetric);

				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException(I18N.getText("macro.function.getDistance.invalidMetric", metric));
				}
			} else {
				walker = grid.createZoneWalker();
			}

			// Get the distances from each source to target cell and keep the minimum one
			int distance = Integer.MAX_VALUE;
			for (CellPoint scell : sourceCells) {
				for (CellPoint tcell : targetCells) {
					walker.setWaypoints(scell, tcell);
					distance = Math.min(distance, walker.getDistance());
				}
			}

			if (gridUnits) {
				return distance;
			} else {
				return (double)distance / renderer.getZone().getUnitsPerCell();
			}
		} else {

			double d = token.getFootprint(grid).getScale();
			double sourceCenterX = token.getX() + (d * grid.getSize()) / 2;
			double sourceCenterY = token.getY() + (d * grid.getSize()) / 2;
			d = target.token.getFootprint(grid).getScale();
			double targetCenterX = target.token.getX() + (d * grid.getSize()) / 2;
			double targetCenterY = target.token.getY() + (d * grid.getSize()) / 2;
			double a = sourceCenterX - targetCenterX;
			double b = sourceCenterY - targetCenterY;
			double h = Math.sqrt(a * a + b * b);
			h /= renderer.getZone().getGrid().getSize();
			if (gridUnits) {
				h *= renderer.getZone().getUnitsPerCell();
			}
			return h;
		}
	}
	
	/**
	 * Gets the distance between this token and a given Point.
	 * @param x the x value of the target point
	 * @param y the y value of the target point
	 * @return the distance between this token and target.
	 */
	public double getDistance(int x, int y) {
		return getDistance(x, y,true,null);
	}
	
	/**
	 * Gets the distance between this token and a given Point.
	 * @param x the x value of the target point
	 * @param y the y value of the target point
	 * @param metric the metric that should be used to calculate the distance. Can be
	 * NO_GRID, NO_DIAGONALS, MANHATTAN, ONE_TWO_ONE or ONE_ONE_ONE
	 * @return the distance between this token and target.
	 */
	public double getDistance(int x, int y, String metric) {
		return getDistance(x,y,true,metric);
	}
	
	/**
	 * Gets the distance between this token and a given Point.
	 * @param x the x value of the target point
	 * @param y the y value of the target point
	 * @param gridUnits if you want the result in grid units
	 * @return the distance between this token and target.
	 */
	public double getDistance(int x, int y, boolean gridUnits) {
		return getDistance(x,y,gridUnits,null);
	}
	
	/**
	 * Gets the distance between this token and a given Point.
	 * @param x the x value of the target point
	 * @param y the y value of the target point
	 * @param gridUnits if you want the result in grid units
	 * @param metric the metric that should be used to calculate the distance. Can be
	 * NO_GRID, NO_DIAGONALS, MANHATTAN, ONE_TWO_ONE or ONE_ONE_ONE
	 * @return the distance between this token and target.
	 */
	public double getDistance(int x, int y, boolean gridUnits, String metric) {
		ZoneRenderer renderer = TabletopTool.getFrame().getZoneRenderer(token.getZone());
		Grid grid = renderer.getZone().getGrid();

		if (grid.getCapabilities().isPathingSupported() && !"NO_GRID".equals(metric)) {

			// Get which cells our tokens occupy
			Set<CellPoint> sourceCells = token.getFootprint(grid).getOccupiedCells(grid.convert(new ZonePoint(token.getX(), token.getY())));
			CellPoint targetCell = grid.convert(new ZonePoint(x,y));

			ZoneWalker walker;
			if (metric != null && grid instanceof SquareGrid) {
				try {
					WalkerMetric wmetric = WalkerMetric.valueOf(metric);
					walker = new AStarSquareEuclideanWalker(renderer.getZone(), wmetric);

				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException(I18N.getText("macro.function.getDistance.invalidMetric", metric));
				}
			} else {
				walker = grid.createZoneWalker();
			}

			// Get the distances from each source to target cell and keep the minimum one
			int distance = Integer.MAX_VALUE;
			for (CellPoint scell : sourceCells) {
				walker.setWaypoints(scell, targetCell);
				distance = Math.min(distance, walker.getDistance());
			}

			if (gridUnits) {
				return distance;
			} else {
				return (double)distance / renderer.getZone().getUnitsPerCell();
			}
		} else {

			double d = token.getFootprint(grid).getScale();
			double sourceCenterX = token.getX() + (d * grid.getSize()) / 2;
			double sourceCenterY = token.getY() + (d * grid.getSize()) / 2;
			double a = sourceCenterX - x;
			double b = sourceCenterY - y;
			double h = Math.sqrt(a * a + b * b);
			h /= renderer.getZone().getGrid().getSize();
			if (gridUnits) {
				h *= renderer.getZone().getUnitsPerCell();
			}
			return h;
		}
	}
		
	/**
	 * Moves a token to the specified x,y location instantly.
	 * @param x the x coordinate of the destination.
	 * @param y  the y coordinate of the destination.
	 * @param gridUnits  whether the (x,y) coordinates are in zone coordinates or point to a grid cell
	 */
	public void moveToken(int x, int y, boolean gridUnits) {
		Grid grid = token.getZone().getGrid();

		if (gridUnits) {
			CellPoint cp = new CellPoint(x, y);
			ZonePoint zp = grid.convert(cp);
			token.setX(zp.x);
			token.setY(zp.y);
		} else {
			ZonePoint zp = new ZonePoint(x, y);
			token.setX(zp.x);
			token.setY(zp.y);
		}
		
		ZoneRenderer renderer = TabletopTool.getFrame().getZoneRenderer(token.getZone());
		Zone zone = renderer.getZone();
		zone.putToken(token);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
		renderer.flushLight();
	}
	
	/**
	 * @param gridUnits   whether the coordinates are in zone coordinates or point to a grid cell
	 * @return the last path this unit took.
	 */
	public List<IntPoint> getLastPath(boolean gridUnits) {
		Path<? extends AbstractPoint> path = token.getLastPath();
		List<IntPoint> points = new ArrayList<IntPoint>();
		if (path != null) {
			Zone zone = token.getZone();
			AbstractPoint zp = null;
			for (AbstractPoint pathCells : path.getCellPath()) {
				if (pathCells instanceof CellPoint) {
					CellPoint cp = (CellPoint) pathCells;
					if (gridUnits)
						zp = cp;
					else
						zp = zone.getGrid().convert((CellPoint) pathCells);
				} else {
					zp = pathCells;
					if(gridUnits)
						zp=zone.getGrid().convert((ZonePoint)zp);
				}
				if (zp != null) {
					points.add(new IntPoint(zp.x,zp.y));
				}
			}
		}
		return points;
	}

	/**
	 * @return if this token is snap to grid
	 */
	public boolean isSnapToGrid() {
		return token.isSnapToGrid();
	}
	
	/**
	 * Returns the Rectangle the token would fill if it stould at the given coordinates
	 * @param x the x coodinate (gridless)
	 * @param y the y coodinate (gridless)
	 * @return the bounding rectangle
	 */
	public Rectangle getBounds(int x, int y) {
		Zone zone=token.getZone();
		if (token.isSnapToGrid()) {
			return token.getFootprint(zone.getGrid()).getBounds(zone.getGrid(), zone.getGrid().convert(new ZonePoint(x,y)));
		} else {
			return token.getBounds(zone);
		}
	}

	/**
	 * Returns the Rectangle the token fills.
	 * @return the bounding rectangle
	 */
	public Rectangle getBounds() {
		return getBounds(token.getX(),token.getY());
	}
	
	/**
	 * @return the names of all properties this token has
	 */
	public Set<String> getPropertyNames() {
		return Collections.unmodifiableSet(token.getPropertyNames());
	}
	
	/**
	 * @param name the name of the property
	 * @return if this token has the given property and if it is not null
	 */
	public boolean hasProperty(String name) {
		Object o=token.getProperty(name);
		return o!=null && StringUtils.isEmpty(o.toString());
	}
	
	/**
	 * @return if this token is a PC
	 */
	public boolean isPC() {
		return token.getType()==Type.PC;
	}
	
	/**
	 * @return if this token is a NPC
	 */
	public boolean isNPC() {
		return token.getType()==Type.NPC;
	}
	
	/**
	 * This method makes this token a PC.
	 */
	public void setPC() {
		if(token.getType()!=Type.PC) {
			ZoneRenderer zr=TabletopTool.getFrame().getZoneRenderer(token.getZone());
			Zone zone=zr.getZone();
			token.setType(Token.Type.PC);
			TabletopTool.serverCommand().putToken(zone.getId(), token);
			zone.putToken(token);
			zr.flushLight();
			TabletopTool.getFrame().updateTokenTree();
		}
	}
	
	/**
	 * This method makes this token a NPC.
	 */
	public void setNPC() {
		if(token.getType()!=Type.NPC) {
			ZoneRenderer zr=TabletopTool.getFrame().getZoneRenderer(token.getZone());
			Zone zone=zr.getZone();
			token.setType(Token.Type.NPC);
			TabletopTool.serverCommand().putToken(zone.getId(), token);
			zone.putToken(token);
			zr.flushLight();
			TabletopTool.getFrame().updateTokenTree();
		}
	}
	
	/**
	 * @return the layer this token is one
	 */
	public String getLayer() {
		return token.getLayer().toString();
	}
	
	/**
	 * This method moves the token to the given layer
	 * @param layer the layer to move this token to
	 * @param forceShape if the shape type of this token should be reset
	 */
	public void setLayer(String layer, boolean forceShape) {
		Layer l=Zone.Layer.valueOf(layer);
		ZoneRenderer zr=TabletopTool.getFrame().getZoneRenderer(token.getZone());
		Zone zone=zr.getZone();
		token.setLayer(l);
		if (forceShape) {
			switch (l) {
			case BACKGROUND:
			case OBJECT:
				token.setShape(Token.TokenShape.TOP_DOWN);
				break;
			case GM:
			case TOKEN:
				Image image = ImageManager.getImage(token.getImageAssetId());
				if (image == null || image == ImageManager.TRANSFERING_IMAGE) {
					token.setShape(Token.TokenShape.TOP_DOWN);
				} else {
					token.setShape(TokenUtil.guessTokenType(image));
				}
				break;
			}
		}
		TabletopTool.serverCommand().putToken(zone.getId(), token);
		zone.putToken(token);
		zr.flushLight();
		TabletopTool.getFrame().updateTokenTree();
	}
	
	/**
	 * Gets the size of the token.
	 * The sizes returned are:
     * <ul><li>Fine</li>
     *<li>Diminutive</li>
     *<li>Tiny</li>
     *<li>Small</li>
     *<li>Medium</li>
     *<li>Large</li>
     *<li>Huge</li>
     *<li>Gargantuan</li>
     *<li>Colossal</li></ul>
     *
	 * @return the size of the token.
	 */
	public String getSize() {
		Grid grid = token.getZone().getGrid();
		if (token.isSnapToScale()) {
			return token.getFootprint(grid).getName();
		}
		return "";
	}
	
	/**
	 * Sets the size of the token.
	 * @param size The size to set the token to.
	 * @return t  he new size of the token.
	 * @throws ParserException
	 *             if the size specified is an invalid size.
	 */
	public String setSize(String size) throws MacroException {
		if (size.equalsIgnoreCase("native") || size.equalsIgnoreCase("free")) {
			token.setSnapToScale(false);
			return this.getSize();
		}
		token.setSnapToScale(true);
		ZoneRenderer renderer = TabletopTool.getFrame().getZoneRenderer(token.getZone());
		Zone zone = renderer.getZone();
		Grid grid = zone.getGrid();
		for (TokenFootprint footprint : grid.getFootprints()) {
			if (footprint.getName().equalsIgnoreCase(size)) {
				token.setFootprint(grid, footprint);
				renderer.flush(token);
				renderer.repaint();
				TabletopTool.serverCommand().putToken(zone.getId(), token);
				zone.putToken(token);
				TabletopTool.getFrame().updateTokenTree();
				return this.getSize();
			}
		}
		throw new MacroException(I18N.getText("macro.function.tokenProperty.invalidSize", "setSize", size));
	}
	
	/**
	 * @return a list of all the explicit owners
	 */
	public Set<String> getOwners() {
		return Collections.unmodifiableSet(token.getOwners());
	}
	
	/**
	 * @return if this token is owned by all
	 */
	public boolean isOwnedByAll() {
		return token.isOwnedByAll();
	}
	
	/**
	 * @param player the player you want to test for
	 * @return if this token is owned by the given player (explicitly or through owned by all)
	 */
	public boolean isOwner(String player) {
		return token.isOwner(player);
	}
	
	/**
	 * This method will reset the given property of this token to the default value
	 * @param property the property to reset
	 */
	public void resetProperty(String property) {
		Zone zone=token.getZone();
		token.resetProperty(property);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
		zone.putToken(token);
	}
	
	/**
	 * @param property the name of the property you want
	 * @return the value of the given property
	 */
	public Object getProperty(String property) {
		return token.getProperty(property);
	}
	
	/**
	 * This method sets a property of this token
	 * @param property the name of the property you want to set
	 * @param value the value the property should have
	 */
	public void setProperty(String property, Object value) {
		Zone zone=token.getZone();
		token.setProperty(property, value);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
		zone.putToken(token);
	}
	
	/**
	 * @param property the property you want the default value of
	 * @return the default value of the given property
	 */
	public Object getPropertyDefault(String property) {
		Object val = null;
		List<TokenProperty> propertyList = TabletopTool.getCampaign().getCampaignProperties().getTokenPropertyList(token.getPropertyType());
		if (propertyList != null) {
			for (TokenProperty tp : propertyList) {
				if (property.equalsIgnoreCase(tp.getName())) {
					val = tp.getDefaultValue();
					break;
				}
			}
		}
		if (val == null) {
			return null;
		}
		if (val instanceof String) {
			// try to convert to a number
			try {
				return new BigDecimal(val.toString()).intValue();
			} catch (Exception e) {
				return val;
			}
		} else {
			return val;
		}
	}
	
	/**
	 * This method will send this token back. This means it will be drawn behind all 
	 * the other tokens.
	 */
	public void sendToBack() {
		Zone zone=token.getZone();
		TabletopTool.serverCommand().sendTokensToBack(zone.getId(), Collections.singleton(token.getId()));
		zone.putToken(token);
	}
	
	/**
	 * This method will send this token to the fron. This means it will be drawn 
	 * in front of all the other tokens.
	 */
	public void bringToFront() {
		Zone zone=token.getZone();
		TabletopTool.serverCommand().bringTokensToFront(zone.getId(), Collections.singleton(token.getId()));
		zone.putToken(token);
	}
	
	/**
	 * This method will set the properties type of this token. This allows you to 
	 * change what properties the token has and which it does not.
	 * @param type the new properties type
	 */
	public void setPropertiesType(String type) {
		Zone zone=token.getZone();
		token.setPropertyType(type);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
		zone.putToken(token); // FJE Should this be here?  Added because other places have it...?!
	}
	
	/**
	 * @return the properties type of the token
	 */
	public String getPropertiesType() {
		 return token.getPropertyType();
	}
	
	/**
	 * @return an Integer indicating the direction the token is facing or null if
	 * it is not facing
	 */
	public Integer getFacing() {
		return token.getFacing();
	}
	
	/**
	 * This method allows you to set the facing of this token or remove it by
	 * setting it to null
	 * @param direction the direction you want the token to face or null
	 */
	public void setFacing(Integer direction) {
		ZoneRenderer renderer = TabletopTool.getFrame().getZoneRenderer(token.getZone());
		Zone zone = renderer.getZone();
		token.setFacing(direction);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
		if(this.hasLight())
			renderer.flushLight();
		zone.putToken(token);
	}
	
	/**
	 * This method removes the facing from this token.
	 */
	public void removeFacing() {
		this.setFacing(null);
	}
	
	/**
	 * This method adds an owner to this token.
	 * @param player the player you want to add
	 */
	public void addOwner(String player) {
		Zone zone=token.getZone();
		token.addOwner(player);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
		zone.putToken(token);
	}
	
	/**
	 * This method removes all owners from this token. It might still be owned by all.
	 */
	public void clearAllOwners() {
		token.clearAllOwners();
		Zone zone=token.getZone();
		TabletopTool.serverCommand().putToken(zone.getId(), token);
		zone.putToken(token);
	}
	
	/**
	 * This method allows you to remove one explicit owner of this token
	 * @param player the player you want to disown
	 */
	public void removeOwner(String player) {
		Zone zone=token.getZone();
		token.removeOwner(player);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
		zone.putToken(token);
	}
	
	/**
	 * @return the width of the token
	 */
	public int getWidth() {
		return token.getBounds(token.getZone()).width;
	}
	
	/**
	 * @return the height of the token
	 */
	public int getHeight() {
		return token.getBounds(token.getZone()).height;
	}
	
	/**
	 * @return the shape of the token. This is one of:
	 * <ul>
	 * <li>Top down</li>
	 * <li>Circle</li>
	 * <li>Square</li>
	 * </ul>
	 */
	public String getTokenShape() {
		return token.getShape().toString();
	}
	
	/**
	 * This method allows you to set the shape type of this token. It can be set to:
	 * <ul>
	 * <li>Top down</li>
	 * <li>Circle</li>
	 * <li>Square</li>
	 * </ul>
	 * @param shape the new shape 
	 */
	public void setTokenShape(String shape) {
		Token.TokenShape newShape = Token.TokenShape.valueOf(shape);
		token.setShape(newShape);
		this.sendUpdateToServer();
	}
	
	/**
	 * @return a copy of this token
	 */
	public TokenView copyToken() {
		Zone zone = token.getZone();
		List<Token> allTokens = zone.getTokens();
		Token t = new Token(token);

		if (allTokens != null) {
			for (Token tok : allTokens) {
				GUID tea = tok.getExposedAreaGUID();
				if (tea != null && tea.equals(t.getExposedAreaGUID())) {
					t.setExposedAreaGUID(new GUID());
				}
			}
		}
		zone.putToken(t);

		TabletopTool.serverCommand().putToken(zone.getId(), t);
			
		TabletopTool.getFrame().getZoneRenderer(token.getZone()).flushLight();
		return new TokenView(t);
	}
	
	/**
	 * This method allows you to copy this token in bulk
	 * @param numberOfCopies the number of copies you want
	 * @return a list of the copies created
	 */
	public List<TokenView> copyToken(int numberOfCopies) {
		Zone zone = token.getZone();
		List<TokenView> newTokens = new ArrayList<TokenView>(numberOfCopies);
		List<Token> allTokens = zone.getTokens();
		for (int i = 0; i < numberOfCopies; i++) {
			Token t = new Token(token);

			if (allTokens != null) {
				for (Token tok : allTokens) {
					GUID tea = tok.getExposedAreaGUID();
					if (tea != null && tea.equals(t.getExposedAreaGUID())) {
						t.setExposedAreaGUID(new GUID());
					}
				}
			}
			zone.putToken(t);

			TabletopTool.serverCommand().putToken(zone.getId(), t);
			newTokens.add(new TokenView(t));
		}
		TabletopTool.getFrame().getZoneRenderer(token.getZone()).flushLight();
		return newTokens;
	}
	
	/**
	 * This method will remove this token
	 */
	public void removeToken() {
		Zone zone = token.getZone();
		TabletopTool.serverCommand().removeToken(zone.getId(), token.getId());
		TabletopTool.getFrame().getZoneRenderer(token.getZone()).flushLight();
	}
	
	/**
	 * This method allows you to set the token image of this token
	 * @param assetId the asset id of the image
	 */
	public void setTokenImage(String assetId) {
		Zone zone = token.getZone();
		token.setImageAsset(null, new MD5Key(assetId));
		zone.putToken(token);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
	}
	
	/**
	 * This method allows you to set the token portrait of this token
	 * @param assetId the asset id of the image
	 */
	public void setTokenPortrait(String assetId) {
		Zone zone = token.getZone();
		token.setPortraitImage(new MD5Key(assetId));
		zone.putToken(token);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
	}
	
	/**
	 * This method allows you to set the token handout of this token
	 * @param assetId the asset id of the image
	 */
	public void setTokenHandout(String assetId) {
		Zone zone = token.getZone();
		token.setCharsheetImage(new MD5Key(assetId));
		zone.putToken(token);
		TabletopTool.serverCommand().putToken(zone.getId(), token);
	}
	
	/**
	 * @return the asset id of this tokens image
	 */
	public String getTokenImage() {
		return token.getImageAssetId().toString();
	}
	
	/**
	 * @return the asset id of this tokens portrait
	 */
	public String getTokenPortrait() {
		return token.getPortraitImage().toString();
	}
	
	/**
	 * @return the asset id of this tokens handout
	 */
	public String getTokenHandout() {
		return token.getCharsheetImage().toString();
	}
	
	/**
	 * @param macroName the macro button name
	 * @return if this token has a macro button of the given name
	 */
	public boolean hasMacro(String macroName) {
		return token.getMacroNames(false).contains(macroName);
	}
	
	/**
	 * This method will return a macro button of this token for the given name
	 * @param macroName the name of the macro button
	 * @return the macro button
	 */
	public MacroButtonView getMacro(String macroName) {
		return new MacroButtonView(token.getMacro(macroName,false));
	}
	
	/**
	 * This method will remove a macro button from this token
	 * @param macroName the name of the macro button
	 * @return if it could be removed
	 */
	public boolean removeMacro(String macroName) {
		MacroButtonProperties mbp=token.getMacro(macroName, false);
		if(mbp!=null) {
			token.deleteMacroButtonProperty(mbp);
			TabletopTool.serverCommand().putToken(token.getZone().getId(), token);
			return true;
		}
		return false;
	}
	
	/**
	 * This method will create a new macro button for this token.
	 * @param macroName the name of the new macro button
	 * @return an object representing the button
	 */
	public MacroButtonView createMacro(String macroName) {
		MacroButtonProperties mbp = new MacroButtonProperties(token.getMacroNextIndex());
		mbp.setToken(token);
		TabletopTool.serverCommand().putToken(token.getZone().getId(), token);
		return new MacroButtonView(mbp);
	}
	
	
	public static List<TokenView> makeTokenViewList(List<Token> list) {
		ArrayList<TokenView> l=new ArrayList<TokenView>(list.size());
		for(Token t:list)
			l.add(new TokenView(t));
		return l;
	}
}
