/*  Package Jabber Server
 *  Copyright (C) 2001, 2002, 2003, 2004, 2005
 *  "Artur Hefczyc" <kobit@users.sourceforge.net>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */
package tigase.xmpp.impl;

import java.util.Queue;
import java.util.logging.Logger;
import tigase.server.Command;
import tigase.server.Packet;
import tigase.xml.Element;
import tigase.xmpp.StanzaType;
import tigase.xmpp.XMPPProcessor;
import tigase.xmpp.XMPPResourceConnection;

/**
 * Describe class StartTLS here.
 *
 *
 * Created: Fri Mar 24 07:22:57 2006
 *
 * @author <a href="mailto:artur.hefczyc@gmail.com">Artur Hefczyc</a>
 * @version $Rev$
 */
public class StartTLS extends XMPPProcessor {

  private static Logger log = Logger.getLogger("tigase.xmpp.impl.StartTLS");

  private static final String XMLNS = "urn:ietf:params:xml:ns:xmpp-tls";
	private static final String TLS_STARTED_KEY = "TLS-Started";
	public static final String TLS_REQUIRED_KEY = "tls-required";

	protected static final String ID = "starttls";
  protected static final String[] ELEMENTS = {"starttls", "proceed", "failure"};
  protected static final String[] XMLNSS = {XMLNS, XMLNS, XMLNS};
  protected static final String[] F_REQUIRED = {
    "<starttls xmlns='" + XMLNS + "'><required/></starttls>"};
  protected static final String[] F_NOT_REQUIRED = {
    "<starttls xmlns='" + XMLNS + "'/>"};

	protected Element proceed = null;
	protected Element failure = null;

	public StartTLS() {
		proceed = new Element("proceed");
		proceed.setXMLNS(XMLNS);
		failure = new Element("failure");
		failure.setXMLNS(XMLNS);
	}

	public String id() { return ID; }

	public String[] supElements() { return ELEMENTS; }

  public String[] supNamespaces() { return XMLNSS; }

  public String[] supStreamFeatures(final XMPPResourceConnection session)	{
    if (session.getSessionData(TLS_STARTED_KEY) == null) {
      if (session.getSessionData(TLS_REQUIRED_KEY) != null
				&& session.getSessionData(TLS_REQUIRED_KEY).equals("true")) {
        return F_REQUIRED;
      } else {
        return F_NOT_REQUIRED;
      }
    } // end of if (session.isAuthorized())
    else {
      return null;
    } // end of if (session.isAuthorized()) else
	}

  public void process(final Packet packet, final XMPPResourceConnection session,
		final Queue<Packet> results) {
		if (packet.getElement().getName().equals("starttls")) {
			session.putSessionData(TLS_STARTED_KEY, "true");
			//results.offer(packet.swapFromTo(proceed));
			results.offer(Command.STARTTLS.getPacket(packet.getTo(),
					packet.getFrom(), StanzaType.set, "1", proceed));
		} // end of if (packet.getElement().getName().equals("starttls"))
		else {
      log.warning("Unknown TLS element: " + packet.getStringData());
			results.offer(packet.swapFromTo(failure));
			results.offer(Command.CLOSE.getPacket(packet.getTo(),
					packet.getFrom(), StanzaType.set, "1"));
		} // end of if (packet.getElement().getName().equals("starttls")) else
	}

} // StartTLS
