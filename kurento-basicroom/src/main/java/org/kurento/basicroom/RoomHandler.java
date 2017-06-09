/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.kurento.basicroom;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * @author Ivan Gracia (izanmail@gmail.com)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 1.0.0
 */
public class RoomHandler extends TextWebSocketHandler {

  private static final String USER = "user";

  private static final Logger log = LoggerFactory.getLogger(RoomHandler.class);

  private static final Gson gson = new GsonBuilder().create();

  private static final String HANDLER_THREAD_NAME = "handler";

  private static final ExecutorService executor = Executors.newFixedThreadPool(10);

  @Autowired
  private RoomManager roomManager;

  @PreDestroy
  public void close() {
    executor.shutdown();
  }

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

    final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);

    // FIXME: Hack to ignore real userName sent by browser
    // if (jsonMessage.get("id").getAsString().equals("joinRoom")) {
    // jsonMessage.add("name", new JsonPrimitive(UUID.randomUUID()
    // .toString()));
    // }

    final RoomParticipant user = (RoomParticipant) session.getAttributes().get(USER);

    if (user != null) {
      log.debug("Incoming message from user '{}': {}", user.getName(), jsonMessage);
    } else {
      log.debug("Incoming message from new user: {}", jsonMessage);
    }

    switch (jsonMessage.get("id").getAsString()) {
      case "receiveVideoFrom":
        executor.submit(new Runnable() {
          @Override
          public void run() {
            updateThreadName("rv:" + user.getName());
            receiveVideoFrom(user, jsonMessage);
            updateThreadName(HANDLER_THREAD_NAME);
          }
        });
        break;
      case "joinRoom":
        joinRoom(jsonMessage, session);
        break;
      case "leaveRoom":
        leaveRoom(user);
        break;
      default:
        break;
    }

    updateThreadName(HANDLER_THREAD_NAME);
  }

  private void receiveVideoFrom(final RoomParticipant user, final JsonObject jsonMessage) {

    final String senderName = jsonMessage.get("sender").getAsString();
    final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();

    Room room = user.getRoom();
    final RoomParticipant sender = room.getParticipant(senderName);

    if (sender != null) {
      user.receiveVideoFrom(sender, sdpOffer);
    } else {
      log.warn("PARTICIPANT {}: Requesting send video for user {} in room {} but it is not found",
          user.getName(), senderName, user.getRoom().getName());
    }
  }

  private void joinRoom(JsonObject jsonMessage, final WebSocketSession session)
      throws IOException, InterruptedException, ExecutionException {

    final String roomName = jsonMessage.get("room").getAsString();
    final String userName = jsonMessage.get("name").getAsString();

    updateThreadName(userName);

    log.debug("PARTICIPANT {}: trying to join room {}", userName, roomName);

    final Room room = roomManager.getRoom(roomName);

    if (!room.isClosed()) {

      room.execute(new Runnable() {
        @Override
        public void run() {
          updateThreadName("r>" + userName);
          final RoomParticipant user = room.join(userName, session);
          session.getAttributes().put(USER, user);
          updateThreadName("r>" + HANDLER_THREAD_NAME);
        }
      });

    } else {
      log.warn("Trying to join from room {} but it is closed", room.getName());
    }
  }

  private void leaveRoom(final RoomParticipant user)
      throws IOException, InterruptedException, ExecutionException {

    final Room room = user.getRoom();

    final String threadName = Thread.currentThread().getName();

    if (!room.isClosed()) {

      room.execute(new Runnable() {
        @Override
        public void run() {
          updateThreadName("room>" + threadName);
          room.leave(user);
          if (room.getParticipants().isEmpty()) {
            roomManager.removeRoom(room);
          }
          updateThreadName("room>" + HANDLER_THREAD_NAME);
        }
      });
    } else {
      log.warn("Trying to leave from room {} but it is closed", room.getName());
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

    RoomParticipant user = (RoomParticipant) session.getAttributes().get(USER);
    if (user != null) {
      updateThreadName(user.getName() + "|wsclosed");
      leaveRoom(user);
      updateThreadName(HANDLER_THREAD_NAME);
    }
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    RoomParticipant user = (RoomParticipant) session.getAttributes().get(USER);

    if (user != null && !user.isClosed()) {
      log.warn("Transport error", exception);
    }
  }

  private void updateThreadName(final String name) {
    Thread.currentThread().setName("user:" + name);
  }
}
