/**
 * Copyright (C) 2011 Stefan Lohr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.htwg_konstanz.in.uce.connection_reversal.mediator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.messages.CommonUceMethod;
import de.htwg_konstanz.in.uce.messages.MessageFormatException;
import de.htwg_konstanz.in.uce.messages.SemanticLevel;
import de.htwg_konstanz.in.uce.messages.SocketEndpoint;
import de.htwg_konstanz.in.uce.messages.UceMessage;
import de.htwg_konstanz.in.uce.messages.UceMessageReader;
import de.htwg_konstanz.in.uce.messages.UceMessageStaticFactory;
import de.htwg_konstanz.in.uce.messages.UniqueUserName;
import de.htwg_konstanz.in.uce.messages.SocketEndpoint.EndpointClass;

/**
 * Klasse zum Interpretieren einer eingehenden Nachricht.
 * Diese Klasse wird vom ListenerThread bei Nachrichteneingang verwendet.
 * 
 * Entsprechende Funktionen zu der Nachricht werden angestoßen.
 * Ebenso werden die Antwortnachrichten an den Sender zurück geschickt.
 * 
 * @author Stefan Lohr
 */
public class HandleMessage implements Runnable {
	
	private DatagramSocket datagramSocket;
	private UceMessage uceRequestMessage;
	private InetAddress sourceAddress;
	private int sourcePort;
	private byte[] data;
	private static final Logger logger = LoggerFactory.getLogger(HandleMessage.class);
	
	/**
	 * Konstruktor, initialisiert alle Objekte (logger, socket, port, ...)
	 * 
	 * @param datagramPacket Muss die Nachricht die empfangen wurde enthalten
	 * @param datagramSocket Socket über den die Antwort versendet werden kann
	 */
	public HandleMessage(DatagramPacket datagramPacket, DatagramSocket datagramSocket) {
		
		sourceAddress = datagramPacket.getAddress();
		sourcePort = datagramPacket.getPort();
		data = datagramPacket.getData();
		
		this.datagramSocket = datagramSocket;
	}
	
	/**
	 * Wird als Thread vom ListenerThread für die Nachrichtenbearbeitung ausgeführt.
	 * Damit der ListenerThread weiterhin auf eingegende Nachrichten Lauschen kann.
	 * 
	 * Hier wird die eingehende Nachricht aus dem DatagrammPacket interpretiert 
	 * und zu der Nachricht die jeweilige entsprechende Methode aufgerufen
	 */
	public void run() {
		
		UceMessageReader uceMessageReader = new UceMessageReader();
		
		try {
			
			uceRequestMessage = uceMessageReader.readUceMessage(data);
			
			logger.info("message received ({})", uceRequestMessage.getTransactionId());
			
			if (uceRequestMessage.isMethod(CommonUceMethod.CONNECTION_REQUEST)) connectionRequest();
			else if (uceRequestMessage.isMethod(CommonUceMethod.REGISTER)) register();
			else if (uceRequestMessage.isMethod(CommonUceMethod.KEEP_ALIVE)) keepAlive();
			else if (uceRequestMessage.isMethod(CommonUceMethod.DEREGISTER)) deregister();
			else if (uceRequestMessage.isMethod(CommonUceMethod.LIST)) list();
			else {
				
				logger.info("unknown message ({})", uceRequestMessage.getMethod().toString());
				
				sendErrorResponse(uceRequestMessage, 0, "unknown message");
			}
		}
		catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	/**
	 * Deregistrieren eines Benutzers und Bestätigung senden.
	 * 
	 * Der Benutzer wird aus dem Singleton UserList entfernt und eine Bestätigungsnachricht, mit der
	 * gleichen Nachrichten-ID wie bei der Anfrage-Nachricht, wird an den Benutzer zurück gesendet.
	 * 
	 * @throws IOException 
	 */
	private void deregister() throws IOException {
		
		UniqueUserName uniqueUserName;
		String userName;
		
		if (!uceRequestMessage.hasAttribute(UniqueUserName.class)) {
			
			sendErrorResponse(uceRequestMessage, 0, "UniqueUserName attribute Expected");
			
			throw new MessageFormatException("UniqueUserName attribute Expected");
		}
		
		uniqueUserName = uceRequestMessage.getAttribute(UniqueUserName.class);
		userName = uniqueUserName.getUniqueUserName();
		
		logger.info("handle deregister message ({})", userName);
		UserList.getInstance().removeUser(userName);
		
		logger.info("send deregister success responde ({})", userName);
		sendSuccessResponse(uceRequestMessage);
	}
	
	/**
	 * Sendet Liste der Registrierten Benutzer.
	 * 
	 * Die Benutzerliste wird aus dem Singleton UserList ausgelesen, in eine entsprechende
	 * Nachricht mit der gleichen Nachrichten-ID verpackt und an den Anfrager zurück gesendet.
	 * 
	 * @throws IOException
	 */
	private void list() throws IOException {
		
		/*
		 * TODO:
		 * 
		 * Was passiert bei einem aufgeteilten UDP Packet,
		 * also wenn das Packet größer als 65536 Bytes ist?!?
		 * 
		 * 65536 ist ein theoretisch möglicher Wert.
		 * Tatsächlich sollte ein Packet nicht größer als 512 Byte sein
		 * Das liegt an der MTU, die zu große packete in mehrere kleine teilt
		 * wenn das passiert, können die nachrichten nicht mehr interpretiert werden
		 * 
		 * LÖSUNGSANSÄTZE:
		 * - mehrere nachrichten mit 1-9 nutzern und einem flag damit man weiß ob weitere nachrichten kommen
		 * - benutzerliste mit neu aufgebautem tcp-socket versenden, dann ist die größe der nachricht egal
		 * 
		 * bei 512 byte sind maximal 9 nutzer pro nachricht möglich:
		 * #########################################################
		 *  20 Bytes Grundnachricht Header mit uuid und so
		 *  52 Bytes UniqueUserName (UserName[48]+Header[4])
		 * --------------------------------------...
		 *  72 Bytes Gesamt bei einem Nutzer
		 * ---------------------------------
		 * 488 Bytes Gesamt bei 9 Nutzern
		 * ==============================
		 * 512 - 488 = 24 Bytes stehen noch für ein Flag-Attribut zur Verfügung
		 * Dieses Flag-Attribut kann dann mitteilen das es Nachricht x von y ist
		 * oder einfach ob noch weitere folgen, das kann aber wegen einer evtl.
		 * falschen reihenfolge der eintreffenden packete schief gehen...
		 * uuid sollte bei jeder nachricht gleich sein, damit man sie zuordnen kann
		 * alternativ könnte die zuordnung auch über das flag-attribut funktionieren
		 * 
		 */
		
		UUID uuid = uceRequestMessage.getTransactionId();
		
		UceMessage uceResponseMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.LIST, SemanticLevel.SUCCESS_RESPONSE, uuid);
		
		logger.info("create list of users");
		for (String userName : UserList.getInstance().getUserNames()) {
			
			UniqueUserName uniqueUserName = new UniqueUserName(userName);
			
			uceResponseMessage.addAttribute(uniqueUserName);
		}
		
		byte[] buf = uceResponseMessage.toByteArray();
		
		DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, sourceAddress, sourcePort);
		
		logger.info("send list of users");
		datagramSocket.send(datagramPacket);
	}
	
	/**
	 * Verarbeiten einer Verbindungsanfrage, weiterleitung an den Zielrechner
	 * 
	 * Als Adresse zum Verbindungsaufbau wird der Übertragene Port und
	 * die Absender IP-Adresse verwendet. Der Port ist in der Nachricht,
	 * weil dieser beim Erstellen des ListenerSockets schon ausgelesen werden kann.
	 * Die IP-Adresse wird vom Absendenden Packet gelesen, da diese im vorraus
	 * nicht bestimmt werden kann, da ein PC mehrere Netzwerkkarten haben kann.
	 * 
	 * Diese zusammengesetzte Adresse wird an den, in den Nachricht stehenden, Nutzer
	 * übertragen, damit dieser eine Verbindung zum Anfragerechner aufbauen kann.
	 * 
	 * Für denn ungewöhnlichen Fall, dass beim Anfragerechner für TCP- und UDP-
	 * Verbindungen unterschiedliche Netzwerkkarten benutzt werden, funktioniert
	 * diese Methode nicht. Das liegt darin begründet, dass hier der Port von der
	 * TCP Verbindung und die IP-Adresse von der UDP Verbindung verwendet wird.
	 * Da im Normalfall aber für beides eine Netzwerkkarte verwendet wird,
	 * sollte diese Methode ohne Probleme Funktionieren.
	 * 
	 * Eine Bestätigungsnachricht wird hier nicht versendet, da die Bestätigung in
	 * Form des TCP-Verbindungsaufbaus mit der gleichen Nachrichten-ID ausreichend ist.
	 * 
	 * @throws IOException 
	 */
	private void connectionRequest() throws IOException {
		
		if (!uceRequestMessage.hasAttribute(SocketEndpoint.class)) {
			
			sendErrorResponse(uceRequestMessage, 0, "SocketEndpoint attribute Expected");
			
			throw new MessageFormatException("SocketEndpoint attribute Expected");
		}
		
		if (!uceRequestMessage.hasAttribute(UniqueUserName.class)) {
			
			sendErrorResponse(uceRequestMessage, 0, "UniqueUserName attribute Expected");
			
			throw new MessageFormatException("UniqueUserName attribute Expected");
		}
		
		SocketEndpoint socketEndpoint = uceRequestMessage.getAttribute(SocketEndpoint.class);
		UniqueUserName uniqueUserName = uceRequestMessage.getAttribute(UniqueUserName.class);
		String userName = uniqueUserName.getUniqueUserName();
		
		logger.info("handle connectionRequest ({})", userName);
		
		UserData userData = UserList.getInstance().getUser(userName);
		
		if (userData == null) {
			
			sendErrorResponse(uceRequestMessage, 0, "unknown user");
			
			throw new MessageFormatException("unknown user");
		}
		
		InetSocketAddress targetAddress = userData.getInetSocketAddress();
		UUID uuid = uceRequestMessage.getTransactionId();
		
		UceMessage uceResponseMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.CONNECTION_REQUEST, SemanticLevel.REQUEST, uuid);
		
		int tcpSourcePort = socketEndpoint.getEndpoint().getPort();
		EndpointClass endpointClass = SocketEndpoint.EndpointClass.CONNECTION_REVERSAL;
		InetSocketAddress newEndpoint = new InetSocketAddress(sourceAddress, tcpSourcePort);
		SocketEndpoint newSocketEndpoint = new SocketEndpoint(newEndpoint, endpointClass);
		
		uceResponseMessage.addAttribute(newSocketEndpoint);
		
		byte[] buf = uceResponseMessage.toByteArray();
		
		DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
		datagramPacket.setAddress(targetAddress.getAddress());
		datagramPacket.setPort(targetAddress.getPort());
		
		logger.info("send successResponse and tcp address {}:{} to target", sourceAddress, tcpSourcePort);
		datagramSocket.send(datagramPacket);
	}
	
	/**
	 * Aktualisieren eines registrierten Benutzers damit er nicht gelöscht wird
	 * weil er zu lange inaktiv in der Liste war. Sendet bestätigung an Absender.
	 * 
	 * Wenn sich die Source-Adresse geändert hat, wird diese in der DB aktuallisiert.
	 * Der Zeitstampel des Benutzers wird dazu in dem Singleton UserList aktuallisiert.
	 * Die Bestätiguns-Nachricht bekommt die gleiche ID wie die Nachricht der Anfrage.
	 * 
	 * Die Bestätigungs-Nachricht ist hierbei nicht nur notwendig, damit der Benutzer weiß,
	 * dass die Nachricht angekommen ist. Das könnte gegebenenfalls bei dieser Nachricht
	 * vernachlässigt werden. Sie ist vielmehr dafür notwendig, das bei manchen Routern
	 * das UDP Mapping der 'Verbindung' nicht gelöscht wird und somit keine Verbindungs-
	 * Anfrage-Nachricht mehr vom Mediator an den Benutzer (Target) gesendet werden kann.
	 * 
	 * @throws IOException 
	 */
	private void keepAlive() throws IOException {
		
		UniqueUserName uniqueUserName;
		String userName;
		
		if (!uceRequestMessage.hasAttribute(UniqueUserName.class)) {
			
			sendErrorResponse(uceRequestMessage, 0, "UniqueUserName attribute Expected");
			
			throw new MessageFormatException("UniqueUserName attribute Expected");
		}
		
		uniqueUserName = uceRequestMessage.getAttribute(UniqueUserName.class);
		userName = uniqueUserName.getUniqueUserName();
		
		UserData userData = UserList.getInstance().getUser(userName);
		InetSocketAddress inetSocketAddress = new InetSocketAddress(sourceAddress, sourcePort);
		
		if (userData == null) {
			
			logger.info("unknown UserData of keepAlive message for {}", userName);
			
			sendErrorResponse(uceRequestMessage, 0, "unknown user");
			
			throw new MessageFormatException("unknown user");
		}
		else if (!userData.getInetSocketAddress().equals(inetSocketAddress)) {
			
			userData = new UserData(userName, inetSocketAddress);
			UserList.getInstance().updateUser(userData);
			
			logger.info("handle keepAlive message ({}) [update]", userName);
		}
		else {
			
			UserList.getInstance().refreshUserTimeStamp(userName);
			logger.info("handle keepAlive message ({}) [refresh]", userName);
		}
		
		logger.info("send keepAlive success responde ({})", userName);
		sendSuccessResponse(uceRequestMessage);
	}
	
	/**
	 * Registriert einen Benutzer beim Mediator und sendet eine Bestätigung.
	 * 
	 * Der Benutzer wird mit dem aktuellen Timestamp in dem Singleton UserList gespeichert 
	 * und eine Bestätigungs-Nachricht mit der gleichen ID wie bei der Anfrage-Nachricht
	 * wird an den Absender zurück gesendet, damit dieser weiß das er registriert ist
	 * und somit auf eingehende Nachrichten mit Verbindungsanfrage lauschen muss.
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws MessageFormatException
	 */
	private void register() throws IOException {
		
		InetSocketAddress inetSocketAddress;
		UniqueUserName uniqueUserName;
		UserData userData;
		String userName;
		
		/*
		 * TODO: prio2: evtl. prüfen ob der nutzer schon existiert
		 * und bei existenz evtl. ne fehlermeldung zurück schicken
		 */
		
		if (!uceRequestMessage.hasAttribute(UniqueUserName.class)) {
			
			sendErrorResponse(uceRequestMessage, 0, "UniqueUserName attribute Expected");
			
			throw new MessageFormatException("UniqueUserName attribute Expected");
		}
		
		uniqueUserName = uceRequestMessage.getAttribute(UniqueUserName.class);
		userName = uniqueUserName.getUniqueUserName();
		
		inetSocketAddress = new InetSocketAddress(sourceAddress, sourcePort);
		userData = new UserData(userName, inetSocketAddress);
		
		logger.info("handle register message ({})", userName);
		UserList.getInstance().addUser(userData);
		
		logger.info("send register success responde ({})", userName);
		sendSuccessResponse(uceRequestMessage);
	}
	
	/**
	 * Sende den SuccsessResponse zu der übergebenen Nachricht an den Absender.
	 * 
	 * Erstellt aus der übergebenen Anfrage-Nachricht eine Antwort-Nachricht an den
	 * Anfrage-Rechner. Die Nachricht enthält die gleiche ID wie die Anfrage-Nachricht.
	 * 
	 * @param uceMessage Muss die Anfrage-Nachricht enthalten
	 * @throws IOException
	 */
	private void sendSuccessResponse(UceMessage uceMessage) throws IOException {
		
		byte[] buffer;
		UceMessage uceResponseMessage;
		DatagramPacket datagramPacket;
		
		uceResponseMessage = uceRequestMessage.buildSuccessResponse();
		
		buffer = uceResponseMessage.toByteArray();
		
		datagramPacket = new DatagramPacket(buffer, buffer.length);
		datagramPacket.setAddress(sourceAddress);
		datagramPacket.setPort(sourcePort);
		
		datagramSocket.send(datagramPacket);
	}
	
	/**
	 * Send ErrorResponse Message with ErrorCode and ErrorMessage to sender
	 * 
	 * @param uceMessage UceRequestMessage
	 * @param errorCode Number of Error-Code
	 * @param errorMessage Message of the error
	 * @throws IOException
	 */
	private void sendErrorResponse(UceMessage uceMessage, int errorCode, String errorMessage) throws IOException {
		
		byte[] buffer;
		UceMessage uceResponseMessage;
		DatagramPacket datagramPacket;
		
		uceResponseMessage = uceRequestMessage.buildErrorResponse(errorCode, errorMessage);
		
		buffer = uceResponseMessage.toByteArray();
		
		datagramPacket = new DatagramPacket(buffer, buffer.length);
		datagramPacket.setAddress(sourceAddress);
		datagramPacket.setPort(sourcePort);
		
		datagramSocket.send(datagramPacket);
	}
}
