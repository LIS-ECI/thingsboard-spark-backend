/**
 * Copyright © 2016-2018 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.common.data.id;

import org.thingsboard.server.common.data.EntityType;

import java.util.UUID;

/**
 * Created by ashvayka on 25.04.17.
 */
public class EntityIdFactory {

    public static EntityId getByTypeAndId(String type, String uuid) {
        return getByTypeAndUuid(EntityType.valueOf(type), UUID.fromString(uuid));
    }

    public static EntityId getByTypeAndUuid(String type, UUID uuid) {
        return getByTypeAndUuid(EntityType.valueOf(type), uuid);
    }

    public static EntityId getByTypeAndUuid(EntityType type, UUID uuid) {
        switch (type) {
            
            case FARM:
                return new FarmId(uuid);
            case PARCEL:
                return new ParcelId(uuid);
        }
        throw new IllegalArgumentException("EntityType " + type + " is not supported!");
    }
}
