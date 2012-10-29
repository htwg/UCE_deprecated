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

package de.htwg_konstanz.in.uce.dht.dht_access;

import static org.jmule.core.jkad.JKadConstants.TAG_DESCRIPTION;
import static org.jmule.core.jkad.JKadConstants.TAG_FILENAME;
import static org.jmule.core.jkad.JKadConstants.TAG_FILERATING;
import static org.jmule.core.jkad.JKadConstants.TAG_FILESIZE;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.bouncycastle.jce.provider.JDKMessageDigest.MD4;
import org.bouncycastle.util.encoders.Base64;
import org.jmule.core.JMRawData;
import org.jmule.core.JMuleCore;
import org.jmule.core.JMuleCoreException;
import org.jmule.core.JMuleCoreFactory;
import org.jmule.core.configmanager.ConfigurationManagerException;
import org.jmule.core.edonkey.ED2KConstants;
import org.jmule.core.edonkey.FileHash;
import org.jmule.core.edonkey.packet.tag.IntTag;
import org.jmule.core.edonkey.packet.tag.StringTag;
import org.jmule.core.edonkey.packet.tag.TagList;
import org.jmule.core.jkad.Int128;
import org.jmule.core.jkad.JKadException;
import org.jmule.core.jkad.JKadListener;
import org.jmule.core.jkad.JKadManager;
import org.jmule.core.jkad.JKadManagerSingleton;
import org.jmule.core.jkad.indexer.Source;
import org.jmule.core.jkad.publisher.PublishItem;
import org.jmule.core.jkad.publisher.PublishTask;
import org.jmule.core.jkad.publisher.Publisher;
import org.jmule.core.jkad.publisher.PublisherListener;
import org.jmule.core.jkad.routingtable.KadContact;
import org.jmule.core.jkad.routingtable.RoutingTableListener;
import org.jmule.core.jkad.search.Search;
import org.jmule.core.jkad.search.SearchListener;
import org.jmule.core.jkad.search.SearchTask;

public class UceDhtKadAdapter implements UceDht {

    private final JKadManager jKadManager;
    private final JMuleCore jMuleCore;

    public UceDhtKadAdapter(int port) throws JMuleCoreException, ConfigurationManagerException {
        JMRawData props = new JMRawData();
        props.setValue(JMRawData.UDP_PORT, port);
        props.setValue(JMRawData.TCP_PORT, port);
        jMuleCore = JMuleCoreFactory.create(props);
        jMuleCore.start();
        // ConfigurationManager configurationManager = ConfigurationManagerSingleton.getInstance();
        // configurationManager.initialize();
        // jMuleCore.start();
        jKadManager = JKadManagerSingleton.getInstance();
        // jKadManager.initialize();
        jKadManager.addJKadListener(new JKadListener() {
            public void JKadIsConnected() {
                System.out.println("connected");
            }

            public void JKadIsConnecting() {
                System.out.println("connecting");
            }

            public void JKadIsDisconnected() {
                System.out.println("disconnected");
            }
        });

        jKadManager.getRoutingTable().addListener(new RoutingTableListener() {
            public void contactAdded(final KadContact contact) {
                // System.out.println("contact added: " + contact);
            }

            public void contactRemoved(final KadContact contact) {
                // System.out.println("contact removed: " + contact);
            }

            public void contactUpdated(final KadContact contact) {
                // System.out.println("contact updated: " + contact);
            }

            public void allContactsRemoved() {
                System.out.println("all contacts removed");
            }

        });

        jKadManager.getPublisher().addListener(new PublisherListener() {
            public void publishTaskStopped(final PublishTask task) {
                System.out.println("publish task stopped: " + task);
            }

            public void publishTaskStarted(final PublishTask task) {
                System.out.println("publish task started: " + task);
            }

            public void publishTaskRemoved(final PublishTask task) {
                System.out.println("publish task removed: " + task);
            }

            public void publishTaskAdded(PublishTask task) {
                System.out.println("publish task added: " + task);
            }
        });

        jKadManager.getSearch().addListener(new SearchListener() {

            public void searchStopped(final SearchTask search) {
                System.out.println("search stopped: " + search);
            }

            public void searchStarted(final SearchTask search) {
                System.out.println("search started: " + search);
            }

            public void searchRemoved(SearchTask search) {
                System.out.println("search removed: " + search);
            }

            public void searchAdded(SearchTask search) {
                System.out.println("search added: " + search);
            }
        });
    }

	public byte[] remove(String key) throws InterruptedException {
		MD4 md4 = new MD4();
        byte[] keyHash = md4.digest(key.getBytes());
        String hashString = org.jmule.core.utils.Convert.byteToHexString(keyHash);
        Int128 fileID = new Int128(new FileHash(hashString));
        Publisher publisher = jKadManager.getPublisher();
        final CountDownLatch latch = new CountDownLatch(1);
        publisher.stopPublish(fileID);
        latch.await();
		return keyHash;
	}
    
    public boolean put(String key, byte[] value) throws InterruptedException {
        MD4 md4 = new MD4();
        byte[] keyHash = md4.digest(key.getBytes());
        String hashString = org.jmule.core.utils.Convert.byteToHexString(keyHash);
        // publish note
        Publisher publisher = jKadManager.getPublisher();
        TagList tagList = new TagList();
        tagList.addTag(new StringTag(TAG_FILENAME, "filename"));
        tagList.addTag(new IntTag(TAG_FILESIZE, 4043844));
        tagList.addTag(new IntTag(TAG_FILERATING, ED2KConstants.FILE_QUALITY_EXCELLENT));
        tagList.addTag(new StringTag(TAG_DESCRIPTION, new String(Base64.encode(value))));
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            publisher.publishNote(new Int128(new FileHash(hashString)), new PublishItem(
                    new FileHash(hashString), tagList));
            final PublishTask publishTask = publisher.getPublishTask(new Int128(new FileHash(
                    hashString)));
            publisher.addListener(new PublisherListener() {

                public void publishTaskStopped(PublishTask task) {
                    if (task.equals(publishTask)) {
                        latch.countDown();
                    }
                }

                public void publishTaskStarted(PublishTask task) {
                    // TODO Auto-generated method stub

                }

                public void publishTaskRemoved(PublishTask task) {
                    // TODO Auto-generated method stub

                }

                public void publishTaskAdded(PublishTask task) {
                    // TODO Auto-generated method stub

                }
            });
        } catch (JKadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        latch.await();
        // TODO wie erfolg prüfen?
        return true;
    }

    public byte[] get(String key) throws InterruptedException {
        // search
        MD4 md4 = new MD4();
        byte[] bhash = md4.digest(key.getBytes());
        // byte[] bhash = org.jmule.core.utils.Convert.hexStringToByte(key);
        Int128 fileHash = new Int128(bhash);
        Search search = jKadManager.getSearch();
        final BlockingQueue<String> queue = new ArrayBlockingQueue<String>(1);
        try {
            search.searchNotes(fileHash, new org.jmule.core.jkad.search.SearchResultListener() {

                public void searchStarted() {
                    System.out.println("note search started");
                }

                public void searchFinished() {
                    System.out.println("note search finished");
                }

                public void processNewResults(List<Source> result) {
                    for (Source s : result) {
                        queue.add((String) s.getTagList().getTag(TAG_DESCRIPTION).getValue());
                        return;
                    }
                }
            }, 4043844);
        } catch (JKadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String result = queue.take();
        return Base64.decode(result);
    }

    public void bootstrap() {
        final CountDownLatch latch = new CountDownLatch(1);
        jKadManager.addJKadListener(new JKadListener() {

            public void JKadIsDisconnected() {
                // TODO Auto-generated method stub

            }

            public void JKadIsConnecting() {
                // TODO Auto-generated method stub

            }

            public void JKadIsConnected() {
                latch.countDown();

            }
        });
        jKadManager.connect();
        try {
            System.out.println("wait");
            latch.await();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        System.out.println(jKadManager.getRoutingTable().getTotalContacts());
        // try {
        // // TODO wie fixes limit verhindern?
        // Thread.sleep(60000);
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        System.out.println(jKadManager.getRoutingTable().getTotalContacts());
    }
    
    public void destroy() {
    	jKadManager.disconnect();
    	try {
			jMuleCore.stop();
		} catch (JMuleCoreException e) {
			// do nothing
		}
    }

}
