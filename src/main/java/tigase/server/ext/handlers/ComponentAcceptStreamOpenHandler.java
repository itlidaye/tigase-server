/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2008 "Artur Hefczyc" <artur.hefczyc@tigase.org>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 * 
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */

package tigase.server.ext.handlers;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.server.ext.CompRepoItem;
import tigase.server.ext.ComponentConnection;
import tigase.server.ext.ComponentProtocolHandler;
import tigase.server.ext.StreamOpenHandler;
import tigase.util.Algorithms;
import tigase.xmpp.XMPPIOService;

import static tigase.server.ext.ComponentProtocolHandler.*;

/**
 * Created: Oct 7, 2009 5:51:47 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class ComponentAcceptStreamOpenHandler implements StreamOpenHandler {

  /**
   * Variable <code>log</code> is a class logger.
   */
  private static final Logger log =
    Logger.getLogger(ComponentAcceptStreamOpenHandler.class.getName());

	private String[] xmlnss = new String[]{"jabber:component:accept"};

	@Override
	public String streamOpened(XMPPIOService<ComponentConnection> serv,
			Map<String, String> attribs, ComponentProtocolHandler handler) {

		switch (serv.connectionType()) {
			case connect: {
				String id = attribs.get("id");
				serv.getSessionData().put(XMPPIOService.SESSION_ID_KEY, id);
				String secret =
						((CompRepoItem)serv.getSessionData().get(REPO_ITEM_KEY)).
						getAuthPasswd();
				try {
					String digest = Algorithms.hexDigest(id, secret, "SHA");
					return "<handshake>" + digest + "</handshake>";
				} catch (NoSuchAlgorithmException e) {
					log.log(Level.SEVERE, "Can not generate digest for pass phrase.", e);
					serv.stop();
					return null;
				}
			}
			case accept: {
				String hostname = attribs.get("to");
				CompRepoItem repoItem = handler.getCompRepoItem(hostname);
				serv.getSessionData().put(REPO_ITEM_KEY, repoItem);
				serv.getSessionData().put(XMPPIOService.HOSTNAME_KEY, hostname);
				String id = UUID.randomUUID().toString();
				serv.getSessionData().put(XMPPIOService.SESSION_ID_KEY, id);
				// This should be done only, after authentication is completed
				// addComponentConnection(hostname, serv);
				return "<stream:stream" +
						" xmlns='jabber:component:accept'" +
						" xmlns:stream='http://etherx.jabber.org/streams'" +
						" from='" + hostname + "'" +
						" id='" + id + "'" +
						">";
			}
			default:
				// Do nothing, more data should come soon...
				break;
		} // end of switch (service.connectionType())
		return null;
	}

	@Override
	public String[] getXMLNSs() {
		return xmlnss;
	}

	@Override
	public String serviceStarted(XMPPIOService<ComponentConnection> serv) {
		switch (serv.connectionType()) {
			case connect:
				CompRepoItem repoItem = (CompRepoItem)serv.getSessionData().get(REPO_ITEM_KEY);
				// Send init xmpp stream here
				serv.getSessionData().put(XMPPIOService.HOSTNAME_KEY, repoItem.getRemoteHost());
				// This should be done only, after authentication is completed
				//addComponentConnection(hostname, serv);
				String data = "<stream:stream" +
						" xmlns='jabber:component:accept'" +
						" xmlns:stream='http://etherx.jabber.org/streams'" +
						" to='" + repoItem.getDomain() + "'" +
						">";
				return data;
			default:
				// Do nothing, more/some data should come soon...
				break;
		} // end of switch (service.connectionType())
		return null;
	}

}