/**
 * Copyright (C) 2011 Daniel Maier
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

package de.htwg_konstanz.in.uce.hp.parallel.mediator;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.NoSuchElementException;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import de.htwg_konstanz.in.uce.hp.parallel.mediator.Repository;
import de.htwg_konstanz.in.uce.hp.parallel.mediator.Repository.RepositoryValue;

public class RepositoryTest {

    @Before
    public void setUp() {
        Repository.INSTANCE.reset();
    }

    @AfterClass
    public static void tearDown() {
        Repository.INSTANCE.reset();
    }

    @Test
    public void testInsertOrUpdateID() {
        String id = "test1";
        InetSocketAddress privateEndpoint = new InetSocketAddress(1234);
        Socket socketToTarget = new Socket();
        Repository.INSTANCE.insertOrUpdateID(id, privateEndpoint, socketToTarget);
        Assert.assertEquals(privateEndpoint,
                Repository.INSTANCE.getRepositoryEntry(id).privateEndpoint);
        Assert.assertEquals(socketToTarget,
                Repository.INSTANCE.getRepositoryEntry(id).registerSocket);
    }

    @Test
    public void testInsertOrUpdateID1() {
        String id = "test23";
        InetSocketAddress privateEndpoint = new InetSocketAddress(1235);
        Socket socketToTarget = new Socket();
        Repository.INSTANCE.insertOrUpdateID(id, privateEndpoint, socketToTarget);
        Assert.assertEquals(privateEndpoint,
                Repository.INSTANCE.getRepositoryEntry(id).privateEndpoint);
        Assert.assertEquals(socketToTarget,
                Repository.INSTANCE.getRepositoryEntry(id).registerSocket);
    }

    @Test(expected = NullPointerException.class)
    public void testInsertOrUpdateIDNull() {
        InetSocketAddress privateEndpoint = new InetSocketAddress(1234);
        Socket socketToTarget = new Socket();
        Repository.INSTANCE.insertOrUpdateID(null, privateEndpoint, socketToTarget);
    }

    @Test(expected = NullPointerException.class)
    public void testInsertOrUpdateIDNull1() {
        String id = "test1";
        Socket socketToTarget = new Socket();
        Repository.INSTANCE.insertOrUpdateID(id, null, socketToTarget);
    }

    @Test(expected = NullPointerException.class)
    public void testInsertOrUpdateIDNull2() {
        String id = "test1";
        InetSocketAddress privateEndpoint = new InetSocketAddress(1234);
        Repository.INSTANCE.insertOrUpdateID(id, privateEndpoint, null);
    }

    @Test(expected = NoSuchElementException.class)
    public void testRepositoryEntryOfNonRegisteredTarget() {
        Repository.INSTANCE.getRepositoryEntry("unknown");
    }

    @Test
    public void testGetRegisteredTargets() {
        Assert.assertTrue(Repository.INSTANCE.getRegisteredTargets().isEmpty());

        String id = "test1";
        InetSocketAddress privateEndpoint = new InetSocketAddress(1234);
        Socket socketToTarget = new Socket();
        Repository.INSTANCE.insertOrUpdateID(id, privateEndpoint, socketToTarget);
        Assert.assertTrue(Repository.INSTANCE.getRegisteredTargets().contains(id));

        String id1 = "test2";
        InetSocketAddress privateEndpoint1 = new InetSocketAddress(1234);
        Socket socketToTarget1 = new Socket();
        Repository.INSTANCE.insertOrUpdateID(id1, privateEndpoint1, socketToTarget1);
        Assert.assertTrue(Repository.INSTANCE.getRegisteredTargets().contains(id1));
        Assert.assertTrue(Repository.INSTANCE.getRegisteredTargets().contains(id));
    }

    @Test
    public void testUnregisterTarget() {
        String id = "test1";
        InetSocketAddress privateEndpoint = new InetSocketAddress(1234);
        Socket socketToTarget = new Socket();
        Repository.INSTANCE.insertOrUpdateID(id, privateEndpoint, socketToTarget);
        Assert.assertEquals(privateEndpoint, Repository.INSTANCE.getRepositoryEntry(id).privateEndpoint);
        Assert.assertEquals(socketToTarget, Repository.INSTANCE.getRepositoryEntry(id).registerSocket);
        RepositoryValue value = Repository.INSTANCE.getRepositoryEntry(id);
        Repository.INSTANCE.unregisterTarget(id);
        Assert.assertTrue(value.keepAliveFuture.isCancelled());
        try {
            Repository.INSTANCE.getRepositoryEntry(id);
            Assert.fail("should not get there");
        } catch(NoSuchElementException e) {
            
        }        
    }
}
