package org.apache.bookkeeper.bookie;


import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class BookieStatusTest {

    String path;

    long lastUpdateTime;
    String bookieMode;
    String layoutVersion;
    Object res;


    @BeforeClass
    public static void createDir() {
        File file = new File("path");
        try {
            file.mkdir();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void deleteDir() {
        File file = new File("path");
        try {
            String[] entries = file.list();
            for (String s : entries) {
                File currentFile = new File(file.getPath(), s);
                currentFile.delete();
            }
            file.delete();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }


    @Parameterized.Parameters
    public static Collection BookieStatusParameters() throws Exception {
        return Arrays.asList(new Object[][]{
                //Path, LastUpdateTime, BookieMode, LayoutVersion, resException
                {"path", System.currentTimeMillis(), "READ_WRITE", "1", null},
                //{"path", 0L, "", "", NumberFormatException.class},

                {null, System.currentTimeMillis(), "READ_ONLY", "1", NullPointerException.class},
                {"", System.currentTimeMillis(), "READ_ONLY", "1", IOException.class}
        });
    }



    public BookieStatusTest(String path, long lastUpdateTime, String bookieMode, String layoutVersion, Object res) {
        this.path = path;
        this.lastUpdateTime = lastUpdateTime;
        this.bookieMode = bookieMode;
        this.layoutVersion = layoutVersion;
        this.res = res;
    }

    private BookieStatus createBookieStatus() {
        return new BookieStatus();
    }

    //Mutation on line 231
    @Test
    public void testToString(){
        BookieStatus bookieStatus = createBookieStatus();
        Assert.assertNotEquals("", bookieStatus.toString());

    }


    //Coverage
    @Test
    public void testWritable() {

        BookieStatus bookieStatus = createBookieStatus();
        Assert.assertEquals(true, bookieStatus.isInWritable());

        //For Mutation on line 64
        bookieStatus.setToReadOnlyMode();
        Assert.assertEquals(false, bookieStatus.isInWritable());

    }

    //Coverage
    @Test
    public void testWritableAfterReadOnly() {

        BookieStatus bookieStatus = createBookieStatus();
        boolean var = bookieStatus.isInReadOnlyMode();
        Assert.assertEquals(false, var);

        //for mutation on line 81
        bookieStatus.setToReadOnlyMode();
        Assert.assertEquals(true, bookieStatus.isInReadOnlyMode());


    }

    //Coverage
    @Test
    public void testSetToWritable() {
        BookieStatus bookieStatus = createBookieStatus();
        Assert.assertEquals(false, bookieStatus.setToWritableMode());

        //For mutation on line 75
        bookieStatus.setToReadOnlyMode();
        Assert.assertEquals(true, bookieStatus.setToWritableMode());


//
//        if (bookieStatus.isInWritable()) {
//            Assert.assertEquals(false, bookieStatus.setToWritableMode());
//        } else {
//            Assert.assertEquals(true, bookieStatus.setToWritableMode());
//        }
    }

    //Coverage
    @Test
    public void testSetReadOnly() {

        BookieStatus bookieStatus = createBookieStatus();
        Assert.assertEquals(true, bookieStatus.setToReadOnlyMode());

        //Mutation on line 90
        bookieStatus.setToReadOnlyMode();
        Assert.assertEquals(false, bookieStatus.setToReadOnlyMode());



//        if (bookieStatus.isInReadOnlyMode()) {
//            Assert.assertEquals(false, bookieStatus.setToReadOnlyMode());
//        } else {
//            Assert.assertEquals(true, bookieStatus.setToReadOnlyMode());
//        }
    }


    @Test
    public void writeToDirTest() {

        try {
            List<File> directories = new ArrayList<>();
            File file = new File(path);
            directories.add(file);
            BookieStatus bookieStatus = createBookieStatus();
            bookieStatus.writeToDirectories(directories);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), res);
        }
    }

    @Test
    public void readFromDirTest() {

        try {
            List<File> directories = new ArrayList<>();
            File file = new File(path);
            directories.add(file);
            BookieStatus bookieStatus = createBookieStatus();
            bookieStatus.readFromDirectories(directories);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), res);
        }
    }

    @Test
    public void parseTest() throws IOException {

        BookieStatus bookieStatus = createBookieStatus();
        String line = layoutVersion + "," + bookieMode + "," + lastUpdateTime;
        BufferedReader rd = new BufferedReader(new StringReader(line));
        Assert.assertEquals(BookieStatus.class, bookieStatus.parse(rd).getClass());

    }

    @Test
    public void parseTestNoLenght() throws IOException {
//        BookieStatus bookieStatus = createBookieStatus();
//        String line = " . . .";
//        if(line.trim().isEmpty()){
//            System.out.println("ciao");
//        }
//        String[] parts = line.split(",");
//        System.out.println(parts.length);
//        bufferedReader = new BufferedReader(new StringReader(line));
//        Assert.assertEquals(null, bookieStatus.parse(rd));
//
//        BookieStatus bookieStatus = createBookieStatus();
//
//
//        Mockito.when(bufferedReader.readLine().split(",").length).thenReturn(0);
//        Mockito.when(bufferedReader.readLine().split(",").length).thenReturn(0);
//        //doReturn("").when(bufferedReader).readLine().split(",");
//        Assert.assertEquals(null, bookieStatus.parse(bufferedReader));
        /**
         * Non avendo un controllo sul ritorno di line.split(",") non riesco ad arrivare
         * a coprire la seguente parte del codice:
         *
         * String[] parts = line.split(",");
         *         if (parts.length == 0) {
         *             LOG.debug("Error in parsing bookie status: {}", line);
         *             return null;
         *         }
         *
         * In quanto parts.lenght ritorna sempre almeno 1 per diverse stringhe testate.
         */

    }

    @Test
    public void parseTestNull() throws IOException {

        BookieStatus bookieStatus = createBookieStatus();
        BufferedReader rd = Mockito.mock(BufferedReader.class);
        when(rd.readLine()).thenReturn(null);
        Assert.assertSame(null, bookieStatus.parse(rd));

    }

    @Test
    public void parseTestEmpty() throws IOException {

        BookieStatus bookieStatus = createBookieStatus();
        BufferedReader rd = Mockito.mock(BufferedReader.class);
        when(rd.readLine()).thenReturn("");
        Assert.assertSame(null, bookieStatus.parse(rd));

    }

    @Test
    public void parseTestIntegerException() throws IOException {

        try {
            BookieStatus bookieStatus = createBookieStatus();
            BufferedReader rd = Mockito.mock(BufferedReader.class);
            when(rd.readLine()).thenReturn("testIncorrect"); //Creazione di uno stato non corretto.
            bookieStatus.parse(rd);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), NumberFormatException.class);

        }
    }


}
