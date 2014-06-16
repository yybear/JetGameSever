package com.handwin.game.rhythm;

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
public class RhythmGameHandler implements GameHandler {
    private static Logger LOG = LoggerFactory.getLogger(RhythmGameHandler.class);

    @Override
    public void onPlayerConnected(String playerName, GameSession session) {
        LOG.info("new player {} connected to session {}", playerName, session);
    }

    @Override
    public void onPlayerDisconnected(String playerName, int exitCode, GameSession session) {
        LOG.info("player {} disconnected from session {}", playerName, session);
    }

    @Override
    public void onLockComplete(ResourceLock resourceLock, GameSession session) {
        LOG.info("lock complete occurs in session {}", session);
    }

    @Override
    public void onReceiveMessage(String playerName, JsonNode message, GameSession session) {
        LOG.info("receive message {} from player {}", message.toString(), playerName);
    }

    @Override
    public void clear(GameSession session) {
        LOG.info("clear the game session {}",session.getGameSessionID());
    }
}
