/*
 * Copyright 2011 Red Hat, Inc, and individual contributors.
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
 */

package org.projectodd.stilts.stomp;

public interface Subscription {

    public static enum AckMode {
        AUTO("auto"),
        CLIENT("client"),
        CLIENT_INDIVIDUAL("client-individual");

        private String str;

        AckMode(String str) {
            this.str = str;
        }

        public String toString() {
            return this.str;
        }

        public static AckMode getAckMode(String ackHeader) {
            AckMode ackMode = AckMode.AUTO;

            if (ackHeader == null || "auto".equalsIgnoreCase( ackHeader )) {
                ackMode = AckMode.AUTO;
            } else if ("client".equalsIgnoreCase( ackHeader )) {
                ackMode = AckMode.CLIENT;
            } else if ("client-individual".equalsIgnoreCase( ackHeader )) {
                ackMode = AckMode.CLIENT_INDIVIDUAL;
            }
            
            return ackMode;
        }
    }

    String getId();

    void cancel() throws StompException;
}
