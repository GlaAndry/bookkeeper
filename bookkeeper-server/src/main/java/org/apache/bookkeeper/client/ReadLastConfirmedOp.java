package org.apache.bookkeeper.client;
/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.EnumSet;
import org.apache.bookkeeper.client.AsyncCallback.ReadLastConfirmedCallback;
import org.apache.bookkeeper.client.BKException.BKDigestMatchException;
import org.apache.bookkeeper.client.DigestManager.RecoveryData;
import org.apache.bookkeeper.proto.BookkeeperInternalCallbacks.ReadEntryCallback;
import org.apache.bookkeeper.proto.BookieProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * This class encapsulated the read last confirmed operation.
 *
 */
class ReadLastConfirmedOp implements ReadEntryCallback {
    static final Logger LOG = LoggerFactory.getLogger(LedgerRecoveryOp.class);
    LedgerHandle lh;
    Object ctx;
    int numResponsesPending;
    int validResponses;
    long maxAddConfirmed;
    long maxLength = 0;
    volatile boolean completed = false;

    ReadLastConfirmedCallback cb;

    public ReadLastConfirmedOp(LedgerHandle lh, ReadLastConfirmedCallback cb, Object ctx) {
        this.cb = cb;
        this.ctx = ctx;
        this.lh = lh;
        this.maxAddConfirmed = -1L;
        this.validResponses = 0;
        this.numResponsesPending = lh.metadata.ensembleSize;
    }

    public void initiate() {
        LOG.info("### Initiate ###");
        for (int i = 0; i < lh.metadata.currentEnsemble.size(); i++) {
            lh.bk.bookieClient.readEntry(lh.metadata.currentEnsemble.get(i), lh.ledgerId, BookieProtocol.LAST_ADD_CONFIRMED, 
                                         this, i, BookieProtocol.FLAG_NONE);
        }
    }

    public synchronized void readEntryComplete(final int rc, final long ledgerId, final long entryId,
            final ChannelBuffer buffer, final Object ctx) {
        int bookieIndex = (Integer) ctx;

        numResponsesPending--;

        if (rc == BKException.Code.OK) {
            try {
                RecoveryData recoveryData = lh.macManager.verifyDigestAndReturnLastConfirmed(buffer);
                maxAddConfirmed = Math.max(maxAddConfirmed, recoveryData.lastAddConfirmed);
                validResponses++;
            } catch (BKDigestMatchException e) {
                // Too bad, this bookie didn't give us a valid answer, we
                // still might be able to recover though so continue
                LOG.error("Mac mismatch while reading last entry from bookie: "
                          + lh.metadata.currentEnsemble.get(bookieIndex));
            }
        }

        if (rc == BKException.Code.NoSuchLedgerExistsException || rc == BKException.Code.NoSuchEntryException) {
            // this still counts as a valid response, e.g., if the client crashed without writing any entry
            validResponses++;
        }

        // other return codes don't count as valid responses
        if ((validResponses >= lh.metadata.quorumSize) &&
                !completed) {
            completed = true;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Read Complete with enough validResponses");
            }
            if(maxAddConfirmed > lh.lastAddConfirmed) lh.lastAddConfirmed = maxAddConfirmed;
            cb.readLastConfirmedComplete(BKException.Code.OK, maxAddConfirmed, this.ctx);
            return;
        }

        if (numResponsesPending == 0 && !completed) {
            completed = true;
            // Have got all responses back but was still not enough, just fail the operation
            LOG.error("While readLastConfirmed ledger: " + ledgerId + " did not hear success responses from all quorums");
            cb.readLastConfirmedComplete(BKException.Code.LedgerRecoveryException, maxAddConfirmed, this.ctx);
        }

    }
}
