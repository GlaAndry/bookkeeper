/*
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
 */

package org.apache.bookkeeper.bookie;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.apache.bookkeeper.bookie.BufferedChannel;

@RunWith(Parameterized.class)
public class TestBufferedDataRead {

	private ByteBuf src;
	private int writeCapacity = 100;
	private int unpersistedBytesBound;
	private Object result;

	@Parameterized.Parameters
	public static Collection BufferedChannelParameters() throws Exception {
		return Arrays.asList(new Object[][] {
			{ null, 0, Exception.class},
			{generateEntryWithWrite(0), 1, 0},
			{generateEntryWithWrite(1), 1, 0}
		});
	}

	public TestBufferedDataRead(ByteBuf src, 
			int unpersistedBytesBound, Object result){
		this.src = src;
		this.unpersistedBytesBound = unpersistedBytesBound;
		this.result = result;
	}

	@Rule
	public ExpectedException exceptions = ExpectedException.none();

	@Test
	public void testWrite() throws Exception {

		if (!(result instanceof Integer)) {
			exceptions.expect(Exception.class);
		}

		BufferedChannel bufferedChannel = createObject(writeCapacity, unpersistedBytesBound);

		src.markReaderIndex();
		src.markWriterIndex();
		src.writeBytes("T".getBytes());
		bufferedChannel.write(src);

	}

	public static BufferedChannel createObject(int writeCapacity,  int unpersistedBytesBound) throws Exception {

		ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
		File newLogFile = File.createTempFile("test", "log");
		newLogFile.deleteOnExit();
		FileChannel fileChannel = new RandomAccessFile(newLogFile, "rw").getChannel();

		BufferedChannel logChannel = new BufferedChannel(allocator, fileChannel,
				writeCapacity, 10, unpersistedBytesBound);

		return logChannel;
	}

	private static ByteBuf generateEntryWithWrite(int length) {
		Random random = new Random();
		byte[] data = new byte[length];
		random.nextBytes(data);
		ByteBuf bb = Unpooled.buffer(length);
		bb.writeBytes(data);
		return bb;
	}

}  


