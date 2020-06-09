package org.apache.bookkeeper.bookie;


import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.*;
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


    @Rule
    public ExpectedException ex = ExpectedException.none();

    @Parameterized.Parameters
    public static Collection BookieStatusParameters() throws Exception {
        return Arrays.asList(new Object[][] {
                {"path", System.currentTimeMillis(), "READ_WRITE", "1"},
                {"path", System.currentTimeMillis(), "READ_ONLY", "1"}
        });
    }


    public BookieStatusTest(String path, long lastUpdateTime, String bookieMode, String layoutVersion) throws FileNotFoundException {
        this.path = path;
        this.lastUpdateTime = lastUpdateTime;
        this.bookieMode = bookieMode;
        this.layoutVersion = layoutVersion;
    }


    private BookieStatus createBookieStatus(){
        return new BookieStatus();
    }

    @Test
    public void testWritable(){
        Assert.assertEquals(true, createBookieStatus().isInWritable());
    }

    @Test
    public void testWritableAfterReadOnly(){

        BookieStatus bookieStatus = createBookieStatus();
        boolean var = bookieStatus.isInReadOnlyMode();
        Assert.assertEquals(false, var);

    }

    @Test
    public void writeToDirNotEmptyTest() throws Exception{

        List<File> dir = new ArrayList<>();

        File file = File.createTempFile("test", "log");
        file.deleteOnExit();
        File file2 = File.createTempFile("test2", "log");
        file2.deleteOnExit();
        File file3 = File.createTempFile("test3", "log");
        file3.deleteOnExit();

        dir.add(file);
        dir.add(file2);
        dir.add(file3);



        BookieStatus bookieStatus = createBookieStatus();
        bookieStatus.writeToDirectories(dir);

    }

    @Test
    public void writeToDirEmptyTest() throws Exception{
        List<File> dir = new ArrayList<>();
        BookieStatus bookieStatus = createBookieStatus();
        bookieStatus.writeToDirectories(dir);

    }

    @Test
    public void readFromDirTest(){

        List<File> dir = new ArrayList<>();
        dir.add(new File(path));

        BookieStatus bookieStatus = createBookieStatus();
        bookieStatus.readFromDirectories(dir);
    }


    @Test
    public void parseTest() throws IOException {

        BookieStatus bookieStatus = createBookieStatus();
        File file = File.createTempFile("test", "log");
        file.deleteOnExit();
        List<File> dir = new ArrayList<>();
        dir.add(file);
        bookieStatus.writeToDirectories(dir);
        BufferedReader rd = new BufferedReader(new FileReader(file));
        bookieStatus.parse(rd);

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
    public void parseTestNotEmpty() throws IOException {

        BookieStatus bookieStatus = createBookieStatus();

        BufferedReader rd = Mockito.mock(BufferedReader.class);
        when(rd.readLine()).thenReturn(layoutVersion+","+bookieMode+","+ lastUpdateTime); //Creazione di uno stato corretto. " 1,READ_WRITE,"+ System.currentTimeMillis()
        Assert.assertNotEquals(null, bookieStatus.parse(rd));

    }



}
