package com.handwin.game.empty;

import com.fasterxml.jackson.databind.JsonNode;
import com.handwin.game.GameHandler;
import com.handwin.game.GameSession;
import com.handwin.game.ResourceLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: roger
 * Date: 13-12-28 下午2:05
 */
public class EmptyGameHandler implements GameHandler {
    private static Logger LOG = LoggerFactory.getLogger(EmptyGameHandler.class);

    private GameSession session;
    @Override
    public void onPlayerConnected(String playerName) {
        LOG.info("new player {} connected to session {}", playerName, session);
    }

    @Override
    public void onPlayerDisconnected(String playerName, int exitCode) {
        LOG.info("player {} disconnected from session {}", playerName, session);
    }

    @Override
    public void onLockComplete(ResourceLock resourceLock) {
        LOG.info("lock complete occurs in session {}", session);
    }

    @Override
    public void onReceiveMessage(String playerName, JsonNode message) {
        LOG.info("receive message {} from player {}", message.toString(), playerName);
    }

    @Override
    public void setGameSession(GameSession session) {
        this.session = session;
    }

    @Override
    public GameSession getGameSession() {
        return session;
    }
    @Override
    public void clear() {
        LOG.info("clear the game session {}",session.getGameSessionID());
    }
}
