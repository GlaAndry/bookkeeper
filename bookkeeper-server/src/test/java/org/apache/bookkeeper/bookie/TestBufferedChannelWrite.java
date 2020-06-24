package org.apache.bookkeeper.bookie;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

@RunWith(Parameterized.class)
public class TestBufferedChannelWrite {


    static int writeCapacity;
    long position;
    ByteBuf writeBuffer;
    static int unpersistedBytesBound;

    static BufferedChannel bufferedChannel;

    Object result; //for Exceptions

    @Parameterized.Parameters
    public static Collection BufferedChannelParameters() {
        return Arrays.asList(new Object[][]{
                //WriteCapacity, Position, ByteBuf writeBuffer, UnpersistedBytesBound, Exception//
                {50, 0, null, 0, NullPointerException.class},
                {50, 0, byteBufNoWrite(0), 0, (long) 0},
                {50, 0, byteBufNoWrite(1), 0, (long) 0}, //in questo caso non scrive nulla in
                //quanto unpersistedByteBound è pari a 0.

                //variazione su UnpersistedBytesBound impostato ad 1>0, quindi ci aspettiamo
                //che la scrittura venga correttamente eseguita.
                {50, 0, null, 1, NullPointerException.class},
                {50, 0, byteBufNoWrite(0), 1, (long) 1},
                {50, 0, byteBufNoWrite(1), 1, (long) 1},

                //ulteriore test
                {50, 0, byteBufNoWrite(1), 20, (long) 0} //cerco di eseguire una scrittura
                //ma questa non viene finalizzata in quanto unpersistedByteBound non è stato
                //raggiunto


        });
    }


    @Before
    public void openChannel() {

        File file;
        try {
            file = File.createTempFile("test", "");
            file.deleteOnExit();
            FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
            bufferedChannel = new BufferedChannel(UnpooledByteBufAllocator.DEFAULT, fileChannel,
                    writeCapacity, 10, unpersistedBytesBound);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @After
    public void close() throws IOException {
        bufferedChannel.close();
    }

    private static ByteBuf generateByteBuf(int length) {
        /**
         * Questo metodo genera randomicamente un ByteBuf con una lunghezza impostata
         * dalla variabile lenght andando a scrivere al suo interno.
         */

        Random random = new Random();
        byte[] data = new byte[length];
        random.nextBytes(data);
        ByteBuf bb = Unpooled.buffer(length);
        bb.writeBytes(data);
        return bb;
    }

    public static ByteBuf byteBufNoWrite(int length) {
        /**
         * Questo metodo genera randomicamente un ByteBuf con una lunghezza impostata
         * senza scrivere nulla al suo interno.
         */
        ByteBuf bb = Unpooled.buffer(length);
        return bb;
    }

    public TestBufferedChannelWrite(int writeCapacity, long position, ByteBuf writeBuffer, int unpersistedBytesBound, Object result) {
        this.writeCapacity = writeCapacity;
        this.position = position;
        this.writeBuffer = writeBuffer;
        this.unpersistedBytesBound = unpersistedBytesBound;
        this.result = result;
    }

    @Test
    public void testWrite() {
        try {
            BufferedChannel bufferedChannel = createBufferedChannel(writeCapacity, unpersistedBytesBound);
            writeBuffer.writeByte(10); //10 in binario equivale a 2 come intero.
            bufferedChannel.write(writeBuffer);
            Assert.assertEquals((long) result, bufferedChannel.fileChannel.size());
            //Assert.assertEquals(result, bufferedChannel.read(writeBuffer, position, 0));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertEquals(e.getClass(), result);
        }

    }

    //for Coverage
    @Test
    public void testClose() throws Exception {

        try {
            BufferedChannel logChannel = createBufferedChannel(writeCapacity, unpersistedBytesBound);
            Assert.assertEquals(true, logChannel.fileChannel.isOpen());
            logChannel.close();
            Assert.assertEquals(false, logChannel.fileChannel.isOpen());
        } catch (Exception e) {
            Assert.assertEquals(IOException.class, e.getClass());
        }

    }

    //for mutation on line 94 && 98
    @Test
    public void testCloseAlreadyClosed() throws Exception {
        BufferedChannel bf = createBufferedChannel(10, 1);
        Assert.assertEquals(true, bf.fileChannel.isOpen());

        bf.close();

        Assert.assertEquals(false, bf.fileChannel.isOpen());

        //close on closed channel
        bf.close();
    }

    //For Coverage
    @Test
    public void testFlush(){
        try {
            BufferedChannel logChannel = createBufferedChannel(writeCapacity, unpersistedBytesBound);
            logChannel.flush();
            Assert.assertEquals(0, writeBuffer.writerIndex());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //For Coverage
    @Test
    public void testPosition(){
        try {
            BufferedChannel logChannel = createBufferedChannel(writeCapacity, unpersistedBytesBound);
            Assert.assertEquals(position, logChannel.position());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static BufferedChannel createBufferedChannel(int writeCapacity, int unpersistedBytesBound) throws Exception {

        ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
        File newLogFile = File.createTempFile("testFile", "");
        newLogFile.deleteOnExit();
        FileChannel fileChannel = new RandomAccessFile(newLogFile, "rw").getChannel();

        BufferedChannel logChannel = new BufferedChannel(allocator, fileChannel,
                writeCapacity, 20, unpersistedBytesBound);

        return logChannel;
    }


}
