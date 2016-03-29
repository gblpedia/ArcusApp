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

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.projectodd.stilts.stomp.protocol.StompFrame.Header;

public class DefaultStompMessage implements StompMessage {

    private static final Charset UTF_8 = Charset.forName( "UTF-8" );
    
    public DefaultStompMessage() {
        this( new DefaultHeaders(), ChannelBuffers.EMPTY_BUFFER );
    }
    
    public DefaultStompMessage(Headers headers, ChannelBuffer content) {
        this( headers, content, false );
    }
    
    public DefaultStompMessage(Headers headers, String content) {
        this( headers, ChannelBuffers.copiedBuffer( content.getBytes() ), false );
        //this( headers, ChannelBuffers.copiedBuffer( content.subSequence(0, content.length()) , Charset.forName("UTF-8") ), false );
    }
    
    public DefaultStompMessage(Headers headers, String content, boolean isError) {
        this( headers, ChannelBuffers.copiedBuffer( content.getBytes() ), isError );
    }
    
    public DefaultStompMessage(Headers headers, ChannelBuffer content, boolean isError) {
        this.headers = headers;
        this.content = content;
        this.isError = isError;
    }
    
    public String getId() {
        return this.headers.get( Header.MESSAGE_ID  );
    }
    
    public void setError(boolean isError) {
        this.isError = isError;
    }
    
    @Override
    public boolean isError() {
        return this.isError;
    }
    
    @Override
    public String getDestination() {
        return this.headers.get(  Header.DESTINATION );
    }
    
    @Override
    public void setDestination(String destination) {
        this.headers.put( Header.DESTINATION, destination );
    }
    
    @Override
    public String getContentType() {
        return this.headers.get(  Header.CONTENT_TYPE );
    }
    
    @Override
    public void setContentType(String contentType) {
        this.headers.put( Header.CONTENT_TYPE, contentType);
    }

    @Override
    public Headers getHeaders() {
        return this.headers;
    }
    
    public void setContent(ChannelBuffer content) {
        this.content = content;
    }

    @Override
    public ChannelBuffer getContent() {
        return ChannelBuffers.wrappedBuffer( this.content );
    }
    
    @Override
    public String getContentAsString() {
        return this.content.toString( UTF_8 );
    }
    
    public void setContentAsString(String content) {
        this.content = ChannelBuffers.copiedBuffer( content.getBytes() );
    }
    
    public String toString() {
        return "[StompMessage: headers=" + this.headers + "\n  content=" + getContentAsString() + "]";
    }
    
    @Override
    public void ack() throws StompException {
        throw new UnsupportedOperationException("ACK");
    }
    
    @Override
    public void ack(String transactionId) throws StompException {
        throw new UnsupportedOperationException("ACK");
    }
    
    @Override
    public void nack() throws StompException {
        throw new UnsupportedOperationException("ACK");
    }
    
    @Override
    public void nack(String transactionId) throws StompException {
        throw new UnsupportedOperationException("NACK");
    }
    
    @Override
    public StompMessage duplicate() {
        return new DefaultStompMessage( headers.duplicate(), ChannelBuffers.wrappedBuffer( content ), isError );
    }

    
    private Headers headers;
    private ChannelBuffer content;
    private boolean isError = false;


}
