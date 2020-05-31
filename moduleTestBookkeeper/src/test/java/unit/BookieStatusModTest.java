package unit;


import org.junit.Assert;
import org.junit.Test;



public class BookieStatusModTest {


    private final BookieStatusMod bookieStatus = new BookieStatusMod();


//    public BookieStatus createBookieStatus(){
//
//        return new BookieStatus();
//    }

    @Test
    public void testWritable(){
        Assert.assertEquals(true, bookieStatus.isInWritable());
    }

    @Test
    public void testWritableAfterReadOnly(){



    }

}
