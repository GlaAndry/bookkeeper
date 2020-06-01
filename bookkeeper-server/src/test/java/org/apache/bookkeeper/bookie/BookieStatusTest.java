package org.apache.bookkeeper.bookie;


import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;


public class BookieStatusTest {


    @Rule
    public ExpectedException ex = ExpectedException.none();

    @Rule
    public ExpectedException exIO = ExpectedException.none();

    public BookieStatusTest() throws FileNotFoundException {
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
        dir.add(new File("path"));

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
        dir.add(new File("path"));

        BookieStatus bookieStatus = createBookieStatus();
        bookieStatus.readFromDirectories(dir);
    }


    @Test
    public void parseTest() throws IOException {

        //BufferedReader rd = Mockito.mock(BufferedReader.class);
        BookieStatus bookieStatus = createBookieStatus();

        File file = File.createTempFile("test", "log");
        file.deleteOnExit();
        List<File> dir = new ArrayList<>();
        dir.add(file);

        bookieStatus.writeToDirectories(dir);

        BufferedReader rd = new BufferedReader(new FileReader(file));

        //when(rd.readLine()).thenReturn(file.getPath());

        bookieStatus.parse(rd);
        //ex.expect(FileNotFoundException.class);

    }

}
