/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.projectodd.stilts.stomp.protocol.websocket;

import java.net.URI;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * Abstraction of web-socket handshake versions.
 * 
 * <p>
 * Since each version uses different headers and behaves differently, these
 * differences are encapsulated in subclasses of <code>Handshake</code>.
 * </p>
 * 
 * @see HandshakeHandler
 * 
 * @author Bob McWhirter
 */
public abstract class Handshake {

    public Handshake(String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }
    
    public Handshake setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    protected String getWebSocketLocation(HttpRequest request) {
        return ( this.secure ? "wss" : "ws" ) + "://" + request.getHeader( HttpHeaders.Names.HOST ) + request.getUri();
    }

    public abstract boolean matches(HttpRequest request);

    public abstract HttpRequest generateRequest(URI uri) throws Exception;
    public abstract HttpResponse generateResponse(HttpRequest request) throws Exception;
    public abstract boolean isComplete(HttpResponse response) throws Exception;
    
    public abstract ChannelHandler newEncoder();
    public abstract ChannelHandler newDecoder();
    public abstract ChannelHandler[] newAdditionalHandlers();
    
    public abstract int readResponseBody();

    private String version;
    private boolean secure;

}
