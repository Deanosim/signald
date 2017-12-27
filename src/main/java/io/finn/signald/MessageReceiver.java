/**
 * Copyright (C) 2018 Finn Herzfeld
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.finn.signald;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

import org.whispersystems.signalservice.api.messages.SignalServiceEnvelope;
import org.whispersystems.signalservice.api.messages.SignalServiceContent;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;

import org.asamk.signal.AttachmentInvalidException;
import org.asamk.signal.GroupNotFoundException;
import org.asamk.signal.NotAGroupMemberException;
import org.asamk.signal.storage.contacts.ContactInfo;



class MessageReceiver implements Manager.ReceiveMessageHandler, Runnable {
    final String username;
    private Manager m;
    private ConcurrentHashMap<String,Manager> managers;
    private SocketManager sockets;

    public MessageReceiver(String username, SocketManager sockets, ConcurrentHashMap<String,Manager> managers) throws NotAGroupMemberException, GroupNotFoundException, AttachmentInvalidException, IOException {
      this.sockets = sockets;
      this.managers = managers;
      this.username = username;
    }

    public void run() {
      try {
        String settingsPath = System.getProperty("user.home") + "/.config/signal";
        this.m = new Manager(this.username, settingsPath);
        System.out.println("Creating new manager for " + username);
        if(m.userExists()) {
          this.m.init();
          this.managers.put(username, m);
        }
        Boolean exitNow = false;
        while(!exitNow) {
          double timeout = 3600;
          boolean returnOnTimeout = true;
          if (timeout < 0) {
            returnOnTimeout = false;
            timeout = 3600;
          }
          boolean ignoreAttachments = false;
          try {
            this.m.receiveMessages((long) (timeout * 1000), TimeUnit.MILLISECONDS, returnOnTimeout, ignoreAttachments, this);
          } catch (IOException e) {
            System.out.println("IO Exception while receiving messages: " + e.getMessage());
          } catch (AssertionError e) {
            System.out.println("AssertionError occured while receiving messages: " + e.getMessage());
          }
        }
      } catch (org.whispersystems.signalservice.api.push.exceptions.AuthorizationFailedException e) {
        System.err.println("Authorization Failed for " + username);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    @Override
    public void handleMessage(SignalServiceEnvelope envelope, SignalServiceContent content, Throwable exception) {
      try {
        SignalServiceAddress source = envelope.getSourceAddress();
        ContactInfo sourceContact = this.m.getContact(source.getNumber());
        if(envelope != null) {
          JsonMessageEnvelope message = new JsonMessageEnvelope(envelope, content, this.m);
          this.sockets.broadcast(new MessageWrapper("message", message));
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
}
