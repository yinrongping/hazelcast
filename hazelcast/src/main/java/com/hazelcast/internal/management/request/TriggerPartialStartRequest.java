/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.internal.management.request;

import com.eclipsesource.json.JsonObject;
import com.hazelcast.instance.Node;
import com.hazelcast.instance.NodeExtension;
import com.hazelcast.internal.management.ManagementCenterService;

import java.io.IOException;

/**
 * Request coming from Management Center to trigger partial start during Hot Restart process
 */
public class TriggerPartialStartRequest implements ConsoleRequest {

    /**
     * Result sent within response when partial start is triggered successfully
     */
    public static final String SUCCESS_RESULT = "SUCCESS";

    /**
     * Result sent within response when partial start triggering failed
     */
    public static final String FAILED_RESULT = "FAILED";

    @Override
    public int getType() {
        return ConsoleRequestConstants.REQUEST_TYPE_TRIGGER_PARTIAL_START;
    }

    @Override
    public void writeResponse(ManagementCenterService mcs, JsonObject out) throws Exception {
        Node node = mcs.getHazelcastInstance().node;
        NodeExtension nodeExtension = node.getNodeExtension();
        boolean done = nodeExtension.triggerPartialStart();
        String result = done ? SUCCESS_RESULT : FAILED_RESULT;
        out.add("result", result);
    }

    @Override
    public Object readResponse(JsonObject in) throws IOException {
        return in.getString("result", FAILED_RESULT);
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject();
    }

    @Override
    public void fromJson(JsonObject json) {
    }
}
