package org.apache.bookkeeper.bookie;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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
public class TestBufferedChannelRead {

    int writeCapacity;
    ByteBuf writeBuffer;
    long position;
    int length;

    static BufferedChannel bufferedChannel;

    public TestBufferedChannelRead(int writeCapacity, ByteBuf writeBuffer, long position, int length, Object result) {
        this.writeCapacity = writeCapacity;
        this.writeBuffer = writeBuffer;
        this.position = position;
        this.length = length;
        this.result = result;
    }

    Object result; //For Exceptions


    @Parameterized.Parameters
    public static Collection BufferedChannelParameters() {
        return Arrays.asList(new Object[][]{
                //WriteCapacity, ByteBuf writeBuffer, position, length, resultException//
                {0, null, -2, -1, NullPointerException.class},
                /**
                 * {0, ByteBufNoWrite(0), 0, 1, 0}, Genera un ciclo infinito
                 *                 Questo può essere evitato aggiungendo
                 *                 if (dest.writableBytes() == 0)
                 *                 length = 0;
                */
                {1, ByteBufNoWrite(1), 2, 2, 2}, //restituisce 2 in quanto ci sono 4 scritture
                //e salta le prime due.


                {50, null, 0, 0, NullPointerException.class},
                {50, ByteBufNoWrite(100), 0, 10, IOException.class},
                {50, ByteBufNoWrite(100), 0, 4, 4},
                {50, ByteBufNoWrite(100), -1, 0, 0}, //test per non entrare nel while.

        });
    }

    @AfterClass
    public static void close() throws IOException {
        try{
            bufferedChannel.close();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }



    @Test
    public void testRead(){
        try{
            /**
             * Il metodo read ritorna il numero di Byte scritti, non il loro contenuto.
             */
            bufferedChannel = createBufferedChannel(writeCapacity, 100);

            //Scriviamo esattamente 4 volte, quindi ci aspettiamo che il metodo read restituisca 4
            writeBuffer.writeByte(1);
            writeBuffer.writeByte(1);
            writeBuffer.writeByte(1);
            writeBuffer.writeByte(1);

            bufferedChannel.write(writeBuffer);
            Assert.assertEquals(result, bufferedChannel.read(writeBuffer, position, length));

        }catch (Exception e){
            Assert.assertEquals(e.getClass(), result);
        }
    }

    @Test
    public void testReadNullWrite() throws Exception {

        /**
         * Non si riesce ad entrare nell'if (writeBuffer == null && writeBufferStartPosition.get() <= pos)
         * in quanto viene restituita direttamente NullPointerException
         */
//        BufferedChannel newBufferedChannel = createBufferedChannel(writeCapacity, 100);
//        ByteBuf writeBuffer2 = null;
//        non scriviamo nulla nel buffer
//        Assert.assertEquals(NullPointerException.class, newBufferedChannel.read(writeBuffer2, 0, 1));

    }


    public static BufferedChannel createBufferedChannel(int writeCapacity, int unpersistedBytesBound) throws Exception {

        ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
        File newLogFile = File.createTempFile("testFile", "");
        newLogFile.deleteOnExit();
        FileChannel fileChannel = new RandomAccessFile(newLogFile, "rw").getChannel();

        BufferedChannel logChannel = new BufferedChannel(allocator, fileChannel,
                writeCapacity, 100, unpersistedBytesBound);

        return logChannel;
    }


    public static ByteBuf generateValidByteBufAlreadyWrited(int length) {
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

    public static ByteBuf ByteBufNoWrite(int length) {
        /**
         * Questo metodo genera randomicamente un ByteBuf con una lunghezza impostata
         * senza scrivere nulla al suo interno.
         */
        ByteBuf bb = Unpooled.buffer(length);
        return bb;
    }
}
