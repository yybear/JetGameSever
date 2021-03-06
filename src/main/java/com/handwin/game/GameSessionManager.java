package com.handwin.game;


import com.google.common.collect.Lists;
import com.handwin.server.ClientApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: roger
 * Date: 13-12-13 下午3:32
 */
public class GameSessionManager {
    private static final Logger log = LoggerFactory.getLogger(GameSessionManager.class);

    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<String, GameSession>(1024);

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private GameHandler gameHandler;

    public GameSession lookupSession(String gameSessionId) {
        return sessions.get(gameSessionId);
    }

    public GameSession createSession(String gameSessionId, int gameID,  List<String> players) {
        String key = gameSessionId;

        GameSession result = sessions.get(key);
        if(result == null) {
            GameSession session = new GameSession(gameID, gameSessionId, gameHandler, players, clientApi, this);
            result = sessions.putIfAbsent(key, session);
            if(result == null) result = session;
        }

        return result;
    }

    public Collection<GameSession> getAllSessions() {
        return Collections.unmodifiableCollection(sessions.values());

    }

    public void removeSession(GameSession session) {
        if(session != null) {
            sessions.remove(session.getGameSessionID());
        }
    }

    public GameSession createSession(final Player player1, final Player player2) {
        final String gameSessionId = UUID.randomUUID().toString();
        log.debug("{} and {} create game session {}", player1.getUser().getId(), player2.getUser().getId(), gameSessionId);

        List<String> members = Lists.asList(player1.getUser().getId(), player2.getUser().getId(), new String[]{});

        GameSession gameSession = createSession(gameSessionId, player1.getAppId(), members);
        return gameSession;
    }
}
