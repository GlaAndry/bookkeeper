/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.bookkeeper.bookie;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Random;

/**
 * Tests for BufferedChannel.
 */
public class BufferedChannelTest {

    private static Random rand = new Random();
    private static final int INTERNAL_BUFFER_WRITE_CAPACITY = -1;
    private static final int INTERNAL_BUFFER_READ_CAPACITY = -1;

    @Rule
    public ExpectedException ex = ExpectedException.none();

    @Test
    public void testObjectInstantiation() throws Exception {
        ex.expect(Exception.class);
        createBufferedChannel(5000, 30, 0, false, false);
    }

    @Test
    public void testRead() throws Exception {
        ex.expect(Exception.class);
        BufferedChannel channel = createBufferedChannel(5000, 30, 0, false, false);
        ByteBuf dataBuf = generateEntry(5000);

        dataBuf.resetReaderIndex();
        dataBuf.resetWriterIndex();
        channel.read(dataBuf, -1);
        channel.close();
    }

    @Test
    public void testWrite() throws Exception{
        ex.expect(Exception.class);
        BufferedChannel channel = createBufferedChannel(5000, 30, 0, false, false);

        ByteBuf dataBuf = generateEntry(5000);
        dataBuf.markReaderIndex();
        dataBuf.markWriterIndex();
        dataBuf.writeBytes("testtesttest".getBytes());
        channel.write(dataBuf);
    }

    @Test
    public void testFlush() throws Exception{
        ex.expect(Exception.class);
        BufferedChannel logChannel = createBufferedChannel(5000, 30, 0, false, false);
        logChannel.flush();
        Assert.assertEquals(0, logChannel.getFileChannelPosition());

    }


    public BufferedChannel createBufferedChannel(int byteBufLength, int numOfWrites, int unpersistedBytesBound, boolean flush,
                                                 boolean shouldForceWrite) throws Exception {
        File file = File.createTempFile("test", "log");
        file.deleteOnExit();
        FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();

        return new BufferedChannel(UnpooledByteBufAllocator.DEFAULT, fileChannel, INTERNAL_BUFFER_WRITE_CAPACITY, INTERNAL_BUFFER_READ_CAPACITY, unpersistedBytesBound);
    }

    private static ByteBuf generateEntry(int length) {
        byte[] data = new byte[length];
        ByteBuf bb = Unpooled.buffer(length);
        rand.nextBytes(data);
        bb.writeBytes(data);
        return bb;
    }
}

