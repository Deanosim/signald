/*
 * Copyright (C) 2020 Finn Herzfeld
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

package io.finn.signald.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.finn.signald.clientprotocol.v1.JsonAddress;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;

import java.util.UUID;

public class ContactInfo {
    @JsonProperty
    public String name;

    @JsonProperty
    public JsonAddress address;

    @JsonProperty
    public String color;

    @JsonProperty
    public String profileKey;

    public void setNumber(@JsonProperty String number) {
        if(address == null) {
            address = new JsonAddress(number);
        } else {
            address.number = number;
        }
    }

    public void setIdentifier(@JsonProperty String identifier) {
        if(address == null) {
            address = new JsonAddress(new SignalServiceAddress(UUID.fromString(identifier), null));
        } else {
            address.uuid = identifier;
        }
    }
}
