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

package org.projectodd.stilts.stomp.client;

import org.jboss.netty.buffer.ChannelBuffer;
import org.projectodd.stilts.stomp.Acknowledger;
import org.projectodd.stilts.stomp.DefaultStompMessage;
import org.projectodd.stilts.stomp.Headers;
import org.projectodd.stilts.stomp.StompException;
import org.projectodd.stilts.stomp.TransactionalAcknowledger;

class ClientStompMessage extends DefaultStompMessage {

    ClientStompMessage(Headers headers, ChannelBuffer content, boolean isError) {
        super( headers, content, isError );
    }

    void setAcknowledger(Acknowledger acknowledger) {
        this.acknowledger = acknowledger;
    }

    @Override
    public void ack(String transactionId) throws StompException {
        if (this.acknowledger instanceof TransactionalAcknowledger ) {
            try {
                ((TransactionalAcknowledger)this.acknowledger).ack( transactionId );
            } catch (Exception e) {
                throw new StompException( e );
            }
        } else {
            ack();
        }
    }

    @Override
    public void ack() throws StompException {
        if (this.acknowledger != null) {
            try {
                this.acknowledger.ack();
            } catch (Exception e) {
                throw new StompException( e );
            }
        } else {
            super.ack();
        }
    }

    
    @Override
    public void nack(String transactionId) throws StompException {
        if (this.acknowledger instanceof TransactionalAcknowledger) {
            try {
                ((TransactionalAcknowledger) this.acknowledger).nack( transactionId );
            } catch (Exception e) {
                throw new StompException( e );
            }
        } else {
            nack();
        }
    }
    
    @Override
    public void nack() throws StompException {
        if (this.acknowledger != null) {
            try {
                this.acknowledger.nack();
            } catch (Exception e) {
                throw new StompException( e );
            }
        } else {
            super.ack();
        }
    }

    private Acknowledger acknowledger;

}
