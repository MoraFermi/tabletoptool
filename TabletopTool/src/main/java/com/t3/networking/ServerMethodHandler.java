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
package com.t3.networking;

import java.awt.geom.Area;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.t3.MD5Key;
import com.t3.client.TabletopTool;
import com.t3.client.ui.zone.FogUtil;
import com.t3.client.ui.zone.ZoneRenderer;
import com.t3.clientserver.handler.AbstractMethodHandler;
import com.t3.common.T3Constants;
import com.t3.model.Asset;
import com.t3.model.AssetManager;
import com.t3.model.ExposedAreaMetaData;
import com.t3.GUID;
import com.t3.model.InitiativeList;
import com.t3.model.InitiativeList.TokenInitiative;
import com.t3.model.Label;
import com.t3.model.MacroButtonProperties;
import com.t3.model.Pointer;
import com.t3.model.Token;
import com.t3.model.Zone;
import com.t3.model.Zone.VisionType;
import com.t3.model.ZonePoint;
import com.t3.model.campaign.Campaign;
import com.t3.model.campaign.CampaignProperties;
import com.t3.model.chat.TextMessage;
import com.t3.model.drawing.Drawable;
import com.t3.model.drawing.DrawnElement;
import com.t3.model.drawing.Pen;
import com.t3.model.grid.Grid;
import com.t3.transfer.AssetProducer;

/**
 * @author drice
 */
public class ServerMethodHandler extends AbstractMethodHandler<NetworkCommand> implements ServerCommand {
	private final T3Server server;
	private final Object MUTEX = new Object();

	public ServerMethodHandler(T3Server server) {
		this.server = server;
	}

	@Override
	public void handleMethod(String id, NetworkCommand method, Object... parameters) {

		try {
			RPCContext context = new RPCContext(id, method, parameters);
			RPCContext.setCurrent(context);
			switch (method) {
			case bootPlayer:
				bootPlayer(context.getString(0));
				break;
			case bringTokensToFront:
				bringTokensToFront(context.getGUID(0), (Set<GUID>) context.get(1));
				break;
			case draw:
				draw(context.getGUID(0), (Pen) context.get(1), (Drawable) context.get(2));
				break;
			case enforceZoneView:
				enforceZoneView(context.getGUID(0), context.getInt(1), context.getInt(2), context.getDouble(3), context.getInt(4), context.getInt(5));
				break;
			case exposeFoW:
				exposeFoW(context.getGUID(0), (Area) context.get(1), (Set<GUID>) context.get(2));
				break;
			case getAsset:
				getAsset((MD5Key) context.get(0));
				break;
			case getZone:
				getZone(context.getGUID(0));
				break;
			case hideFoW:
				hideFoW(context.getGUID(0), (Area) context.get(1), (Set<GUID>) context.get(2));
				break;
			case setFoW:
				setFoW(context.getGUID(0), (Area) context.get(1), (Set<GUID>) context.get(2));
				break;
			case hidePointer:
				hidePointer(context.getString(0));
				break;
			case setLiveTypingLabel:
				setLiveTypingLabel(context.getString(0), context.getBool(1));
				break;
			case enforceNotification:
				enforceNotification(context.getBool(0));
				break;
			case message:
				message((TextMessage) context.get(0));
				break;
			case putAsset:
				putAsset((Asset) context.get(0));
				break;
			case putLabel:
				putLabel(context.getGUID(0), (Label) context.get(1));
				break;
			case putToken:
				putToken(context.getGUID(0), (Token) context.get(1));
				break;
			case putZone:
				putZone((Zone) context.get(0));
				break;
			case removeZone:
				removeZone(context.getGUID(0));
				break;
			case removeAsset:
				removeAsset((MD5Key) context.get(0));
				break;
			case removeToken:
				removeToken(context.getGUID(0), context.getGUID(1));
				break;
			case removeLabel:
				removeLabel(context.getGUID(0), context.getGUID(1));
				break;
			case sendTokensToBack:
				sendTokensToBack(context.getGUID(0), (Set<GUID>) context.get(1));
				break;
			case setCampaign:
				setCampaign((Campaign) context.get(0));
				break;
			case setZoneGridSize:
				setZoneGridSize(context.getGUID(0), context.getInt(1), context.getInt(2), context.getInt(3), context.getInt(4));
				break;
			case setZoneVisibility:
				setZoneVisibility(context.getGUID(0), (Boolean) context.get(1));
				break;
			case setZoneHasFoW:
				setZoneHasFoW(context.getGUID(0), context.getBool(1));
				break;
			case showPointer:
				showPointer(context.getString(0), (Pointer) context.get(1));
				break;
			case startTokenMove:
				startTokenMove(context.getString(0), context.getGUID(1), context.getGUID(2), (Set<GUID>) context.get(3));
				break;
			case stopTokenMove:
				stopTokenMove(context.getGUID(0), context.getGUID(1));
				break;
			case toggleTokenMoveWaypoint:
				toggleTokenMoveWaypoint(context.getGUID(0), context.getGUID(1), (ZonePoint) context.get(2));
				break;
			case undoDraw:
				undoDraw(context.getGUID(0), context.getGUID(1));
				break;
			case updateTokenMove:
				updateTokenMove(context.getGUID(0), context.getGUID(1), context.getInt(2), context.getInt(3));
				break;
			case clearAllDrawings:
				clearAllDrawings(context.getGUID(0), (Zone.Layer) context.get(1));
				break;
			case enforceZone:
				enforceZone(context.getGUID(0));
				break;
			case setServerPolicy:
				setServerPolicy((ServerPolicy) context.get(0));
				break;
			case addTopology:
				addTopology(context.getGUID(0), (Area) context.get(1));
				break;
			case removeTopology:
				removeTopology(context.getGUID(0), (Area) context.get(1));
				break;
			case renameZone:
				renameZone(context.getGUID(0), context.getString(1));
				break;
			case heartbeat:
				heartbeat(context.getString(0));
				break;
			case updateCampaign:
				updateCampaign((CampaignProperties) context.get(0));
				break;
			case movePointer:
				movePointer(context.getString(0), context.getInt(1), context.getInt(2));
				break;
			case updateInitiative:
				updateInitiative((InitiativeList) context.get(0), (Boolean) context.get(1));
				break;
			case updateTokenInitiative:
				updateTokenInitiative(context.getGUID(0), context.getGUID(1), context.getBool(2), context.getString(3), context.getInt(4));
				break;
			case setVisionType:
				setVisionType(context.getGUID(0), (VisionType) context.get(1));
				break;
			case setBoard:
				setBoard(context.getGUID(0), (MD5Key) context.get(1), context.getInt(2), context.getInt(3));
				break;
			case updateCampaignMacros:
				updateCampaignMacros((List<MacroButtonProperties>) context.get(0));
				break;
			case setTokenLocation:
				setTokenLocation(context.getGUID(0), context.getGUID(1), context.getInt(2), context.getInt(3));
				break;
			case exposePCArea:
				exposePCArea(context.getGUID(0));
				break;
			case updateExposedAreaMeta:
				updateExposedAreaMeta(context.getGUID(0), context.getGUID(1), (ExposedAreaMetaData) context.get(2));
				break;
			}
		} finally {
			RPCContext.setCurrent(null);
		}
	}

	/**
	 * Send the current call to all other clients except for the sender
	 */
	private void forwardToClients() {
		server.getConnection().broadcastCallMethod(new String[] { RPCContext.getCurrent().id }, RPCContext.getCurrent().method, RPCContext.getCurrent().parameters);
	}

	/**
	 * Send the current call to all clients including the sender
	 */
	private void forwardToAllClients() {
		server.getConnection().broadcastCallMethod(new String[] {}, RPCContext.getCurrent().method, RPCContext.getCurrent().parameters);
	}

	private void broadcastToClients(String exclude, NetworkCommand method, Object... parameters) {
		server.getConnection().broadcastCallMethod(new String[] { exclude }, method, parameters);
	}

	private void broadcastToAllClients(NetworkCommand method, Object... parameters) {
		server.getConnection().broadcastCallMethod(new String[] {}, method, parameters);
	}

	////
	// SERVER COMMAND
	@Override
	public void setVisionType(GUID zoneGUID, VisionType visionType) {
		Zone zone = server.getCampaign().getZone(zoneGUID);
		zone.setVisionType(visionType);
		server.getConnection().broadcastCallMethod(NetworkCommand.setUseVision, RPCContext.getCurrent().parameters);
	}

	@Override
	public void heartbeat(String data) {
		// Nothing to do yet
	}

	@Override
	public void enforceZone(GUID zoneGUID) {
		forwardToClients();
	}

	@Override
	public void updateCampaign(CampaignProperties properties) {
		server.getCampaign().replaceCampaignProperties(properties);
		forwardToClients();
	}

	@Override
	public void bringTokensToFront(GUID zoneGUID, Set<GUID> tokenSet) {
		synchronized (MUTEX) {
			Zone zone = server.getCampaign().getZone(zoneGUID);

			// Get the tokens to update
			List<Token> tokenList = new ArrayList<Token>();
			for (GUID tokenGUID : tokenSet) {
				Token token = zone.getToken(tokenGUID);
				if (token != null) {
					tokenList.add(token);
				}
			}
			// Arrange
			Collections.sort(tokenList, Zone.TOKEN_Z_ORDER_COMPARATOR);

			// Update
			int z = zone.getLargestZOrder() + 1;
			for (Token token : tokenList) {
				token.setZOrder(z++);
			}
			// Broadcast
			for (Token token : tokenList) {
				broadcastToAllClients(NetworkCommand.putToken, zoneGUID, token);
			}
		}
	}

	@Override
	public void clearAllDrawings(GUID zoneGUID, Zone.Layer layer) {
		Zone zone = server.getCampaign().getZone(zoneGUID);
		List<DrawnElement> list = zone.getDrawnElements(layer);
		zone.clearDrawables(list); // FJE Empties the DrawableUndoManager and empties the list
		forwardToAllClients();
	}

	@Override
	public void draw(GUID zoneGUID, Pen pen, Drawable drawable) {
		server.getConnection().broadcastCallMethod(NetworkCommand.draw, RPCContext.getCurrent().parameters);
		Zone zone = server.getCampaign().getZone(zoneGUID);
		zone.addDrawable(new DrawnElement(drawable, pen));
	}

	@Override
	public void enforceZoneView(GUID zoneGUID, int x, int y, double scale, int width, int height) {
		forwardToClients();
	}

	@Override
	public void exposeFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
		Zone zone = server.getCampaign().getZone(zoneGUID); // this can return a zone that's not in T3Frame.zoneRenderList???
		zone.exposeArea(area, selectedToks);
		server.getConnection().broadcastCallMethod(NetworkCommand.exposeFoW, RPCContext.getCurrent().parameters);
	}

	@Override
	public void exposePCArea(GUID zoneGUID) {
		ZoneRenderer renderer = TabletopTool.getFrame().getZoneRenderer(zoneGUID);
		FogUtil.exposePCArea(renderer);
		server.getConnection().broadcastCallMethod(NetworkCommand.exposePCArea, RPCContext.getCurrent().parameters);
	}

	@Override
	public void getAsset(MD5Key assetID) {
		if (assetID == null || assetID.toString().length() == 0) {
			return;
		}
		try {
			AssetProducer producer = new AssetProducer(assetID, AssetManager.getAssetInfo(assetID).getProperty(AssetManager.NAME), AssetManager.getAssetCacheFile(assetID));
			server.getConnection().callMethod(RPCContext.getCurrent().id, T3Constants.Channel.IMAGE, NetworkCommand.startAssetTransfer, producer.getHeader());
			server.addAssetProducer(RPCContext.getCurrent().id, producer);

		} catch (IOException ioe) {
			ioe.printStackTrace();

			// Old fashioned way
			server.getConnection().callMethod(RPCContext.getCurrent().id, NetworkCommand.putAsset, AssetManager.getAsset(assetID));
		} catch (IllegalArgumentException iae) {
			// Sending an empty asset will cause a failure of the image to load on the client side, showing a broken
			// image instead of blowing up
			Asset asset = new Asset("broken", new byte[] {});
			asset.setId(assetID);
			server.getConnection().callMethod(RPCContext.getCurrent().id, NetworkCommand.putAsset, asset);
		}
	}

	@Override
	public void getZone(GUID zoneGUID) {
		server.getConnection().callMethod(RPCContext.getCurrent().id, NetworkCommand.putZone, server.getCampaign().getZone(zoneGUID));
	}

	@Override
	public void hideFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
		Zone zone = server.getCampaign().getZone(zoneGUID);
		zone.hideArea(area, selectedToks);
		server.getConnection().broadcastCallMethod(NetworkCommand.hideFoW, RPCContext.getCurrent().parameters);
	}

	@Override
	public void setFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
		Zone zone = server.getCampaign().getZone(zoneGUID);
		zone.setFogArea(area, selectedToks);
		server.getConnection().broadcastCallMethod(NetworkCommand.setFoW, RPCContext.getCurrent().parameters);
	}

	@Override
	public void hidePointer(String player) {
		forwardToAllClients();
	}

	@Override
	public void movePointer(String player, int x, int y) {
		forwardToAllClients();
	}

	@Override
	public void updateInitiative(InitiativeList list, Boolean ownerPermission) {
		if (list != null) {
			if (list.getZone() == null)
				return;
			Zone zone = server.getCampaign().getZone(list.getZone().getId());
			zone.setInitiativeList(list);
		} else if (ownerPermission != null) {
			TabletopTool.getFrame().getInitiativePanel().setOwnerPermissions(ownerPermission.booleanValue());
		}
		forwardToAllClients();
	}

	@Override
	public void updateTokenInitiative(GUID zoneId, GUID tokenId, Boolean hold, String state, Integer index) {
		Zone zone = server.getCampaign().getZone(zoneId);
		InitiativeList list = zone.getInitiativeList();
		TokenInitiative ti = list.getTokenInitiative(index);
		if (!ti.getId().equals(tokenId)) {
			// Index doesn't point to same token, try to find it
			Token token = zone.getToken(tokenId);
			List<Integer> tokenIndex = list.indexOf(token);

			// If token in list more than one time, punt
			if (tokenIndex.size() != 1)
				return;
			ti = list.getTokenInitiative(tokenIndex.get(0));
		} // endif
		ti.update(hold, state);
		forwardToAllClients();
	}

	@Override
	public void renameZone(GUID zoneGUID, String name) {
		Zone zone = server.getCampaign().getZone(zoneGUID);
		if (zone != null) {
			zone.setName(name);
			forwardToAllClients();
		}
	}

	@Override
	public void message(TextMessage message) {
		forwardToClients();
	}

	@Override
	public void putAsset(Asset asset) {
		AssetManager.putAsset(asset);
	}

	@Override
	public void putLabel(GUID zoneGUID, Label label) {
		Zone zone = server.getCampaign().getZone(zoneGUID);
		zone.putLabel(label);
		forwardToClients();
	}

	@Override
	public void putToken(GUID zoneGUID, Token token) {
		Zone zone = server.getCampaign().getZone(zoneGUID);

		boolean newToken = zone.getToken(token.getId()) == null;
		synchronized (MUTEX) {
			// Set z-order for new tokens
			if (newToken) {
				token.setZOrder(zone.getLargestZOrder() + 1);
			}
			zone.putToken(token);
		}
		if (newToken) {
			forwardToAllClients();
		} else {
			forwardToClients();
		}
	}

	@Override
	public void putZone(Zone zone) {
		server.getCampaign().putZone(zone);
		forwardToClients();
	}

	@Override
	public void removeAsset(MD5Key assetID) {
		AssetManager.removeAsset(assetID);
	}

	@Override
	public void removeLabel(GUID zoneGUID, GUID labelGUID) {
		Zone zone = server.getCampaign().getZone(zoneGUID);
		zone.removeLabel(labelGUID);
		server.getConnection().broadcastCallMethod(NetworkCommand.removeLabel, RPCContext.getCurrent().parameters);
	}

	@Override
	public void removeToken(GUID zoneGUID, GUID tokenGUID) {
		Zone zone = server.getCampaign().getZone(zoneGUID);
		zone.removeToken(tokenGUID);
		server.getConnection().broadcastCallMethod(NetworkCommand.removeToken, RPCContext.getCurrent().parameters);
	}

	@Override
	public void removeZone(GUID zoneGUID) {
		server.getCampaign().removeZone(zoneGUID);
		forwardToClients();
	}

	@Override
	public void sendTokensToBack(GUID zoneGUID, Set<GUID> tokenSet) {
		synchronized (MUTEX) {
			Zone zone = server.getCampaign().getZone(zoneGUID);

			// Get the tokens to update
			List<Token> tokenList = new ArrayList<Token>();
			for (GUID tokenGUID : tokenSet) {
				Token token = zone.getToken(tokenGUID);
				if (token != null) {
					tokenList.add(token);
				}
			}
			// Arrange
			Collections.sort(tokenList, Zone.TOKEN_Z_ORDER_COMPARATOR);

			// Update
			int z = zone.getSmallestZOrder() - 1;
			for (Token token : tokenList) {
				token.setZOrder(z--);
			}
			// Broadcast
			for (Token token : tokenList) {
				broadcastToAllClients(NetworkCommand.putToken, zoneGUID, token);
			}
		}
	}

	@Override
	public void setCampaign(Campaign campaign) {
		server.setCampaign(campaign);
		forwardToClients();
	}

	@Override
	public void setZoneGridSize(GUID zoneGUID, int offsetX, int offsetY, int size, int color) {
		Zone zone = server.getCampaign().getZone(zoneGUID);
		Grid grid = zone.getGrid();
		grid.setSize(size);
		grid.setOffset(offsetX, offsetY);
		zone.setGridColor(color);
		server.getConnection().broadcastCallMethod(NetworkCommand.setZoneGridSize, RPCContext.getCurrent().parameters);
	}

	@Override
	public void setZoneHasFoW(GUID zoneGUID, boolean hasFog) {
		Zone zone = server.getCampaign().getZone(zoneGUID);
		zone.setHasFog(hasFog);
		server.getConnection().broadcastCallMethod(NetworkCommand.setZoneHasFoW, RPCContext.getCurrent().parameters);
	}

	@Override
	public void setZoneVisibility(GUID zoneGUID, boolean visible) {
		server.getCampaign().getZone(zoneGUID).setVisible(visible);
		server.getConnection().broadcastCallMethod(NetworkCommand.setZoneVisibility, RPCContext.getCurrent().parameters);
	}

	@Override
	public void showPointer(String player, Pointer pointer) {
		server.getConnection().broadcastCallMethod(NetworkCommand.showPointer, RPCContext.getCurrent().parameters);
	}

	@Override
	public void setLiveTypingLabel(String label, boolean show) {
		forwardToClients();
	}

	@Override
	public void enforceNotification(Boolean enforce) {
		forwardToClients();
	}

	@Override
	public void bootPlayer(String player) {
		forwardToClients();

		// And just to be sure, remove them from the server
		server.releaseClientConnection(server.getConnectionId(player));
	}

	@Override
	public void startTokenMove(String playerId, GUID zoneGUID, GUID tokenGUID, Set<GUID> tokenList) {
		forwardToClients();
	}

	@Override
	public void stopTokenMove(GUID zoneGUID, GUID tokenGUID) {
		forwardToClients();
	}

	@Override
	public void toggleTokenMoveWaypoint(GUID zoneGUID, GUID tokenGUID, ZonePoint cp) {
		forwardToClients();
	}

	@Override
	public void undoDraw(GUID zoneGUID, GUID drawableGUID) {
		// This is a problem.  The contents of the UndoManager are not synchronized across machines
		// so if one machine uses Meta-Z to undo a drawing, that drawable will be removed on all
		// machines, but there is no attempt to keep the UndoManager in sync.  So that same drawable
		// will still be in the UndoManager queue on other machines.  Ideally we should be filtering
		// the local Undomanager queue based on the drawable (removing it when we find it), but
		// the Swing UndoManager doesn't provide that capability so we would need to subclass it.
		// And if we're going to do that, we may as well fix the other problems:  the UndoManager should
		// be per-map and per-layer (?) and not a singleton instance for the entire application!  But
		// now we're talking a pretty intrusive set of changes:  when a zone is deleted, the UndoManagers
		// would need to be cleared and duplicating a zone means doing a deep copy on the UndoManager
		// or flushing it entirely in the new zone.  We'll save all of this for a separate patch against 1.3 or
		// for 1.4.
		server.getConnection().broadcastCallMethod(NetworkCommand.undoDraw, zoneGUID, drawableGUID);
		Zone zone = server.getCampaign().getZone(zoneGUID);
		zone.removeDrawable(drawableGUID);
	}

	@Override
	public void updateTokenMove(GUID zoneGUID, GUID tokenGUID, int x, int y) {
		forwardToClients();
	}

	public void setTokenLocation(GUID zoneGUID, GUID tokenGUID, int x, int y) {
		forwardToClients();
	}

	@Override
	public void setServerPolicy(ServerPolicy policy) {
		forwardToClients();
	}

	@Override
	public void addTopology(GUID zoneGUID, Area area) {
		Zone zone = server.getCampaign().getZone(zoneGUID);
		zone.addTopology(area);
		forwardToClients();
	}

	@Override
	public void removeTopology(GUID zoneGUID, Area area) {
		Zone zone = server.getCampaign().getZone(zoneGUID);
		zone.removeTopology(area);
		forwardToClients();
	}

	@Override
	public void updateCampaignMacros(List<MacroButtonProperties> properties) {
		TabletopTool.getCampaign().setMacroButtonPropertiesArray(new ArrayList<MacroButtonProperties>(properties));
		forwardToClients();
	}

	@Override
	public void setBoard(GUID zoneGUID, MD5Key mapId, int x, int y) {
		forwardToClients();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.t3.networking.ServerCommand#updateExposedAreaMeta(com.t3.GUID,
	 * com.t3.GUID, com.t3.model.ExposedAreaMetaData)
	 */
	@Override
	public void updateExposedAreaMeta(GUID zoneGUID, GUID tokenExposedAreaGUID, ExposedAreaMetaData meta) {
		forwardToClients();
	}

	////
	// CONTEXT
	private static class RPCContext {
		private static ThreadLocal<RPCContext> threadLocal = new ThreadLocal<RPCContext>();

		public String id;
		public NetworkCommand method;
		public Object[] parameters;

		public RPCContext(String id, NetworkCommand method, Object[] parameters) {
			this.id = id;
			this.method = method;
			this.parameters = parameters;
		}

		public static boolean hasCurrent() {
			return threadLocal.get() != null;
		}

		public static RPCContext getCurrent() {
			return threadLocal.get();
		}

		public static void setCurrent(RPCContext context) {
			threadLocal.set(context);
		}

		////
		// Convenience methods
		public GUID getGUID(int index) {
			return (GUID) parameters[index];
		}

		public Integer getInt(int index) {
			return (Integer) parameters[index];
		}

		public Double getDouble(int index) {
			return (Double) parameters[index];
		}

		public Object get(int index) {
			return parameters[index];
		}

		public String getString(int index) {
			return (String) parameters[index];
		}

		public Boolean getBool(int index) {
			return (Boolean) parameters[index];
		}
	}
}
