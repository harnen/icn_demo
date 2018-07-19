package uk.ac.ucl.kbapp;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import uk.ac.ucl.kbapp.utils.G;

/**
 * Created by srene on 02/11/17.
 *
 * A class representing the bloomfilter advertised by users containing the local videos

BloomFilter bloomFilterIn = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 50,0.03);
bloomFilterIn.put("item1");
bloomFilterIn.put("item2");
ByteArrayOutputStream out = new ByteArrayOutputStream(); try { bloomFilterIn.writeTo(out); */
//String msg = out.toString();
// /*out.flush(); out.close();*/
// byte[] msg = out.toByteArray();
// ByteArrayInputStream in = new ByteArrayInputStream(msg);
// ByteArrayInputStream in = new ByteArrayInputStream(msg.getBytes());
// BloomFilter bloomFilterOut = BloomFilter.readFrom(in, Funnels.stringFunnel(Charset.defaultCharset()));
// Log.d(TAG, "bloomFilterIn hash code: " + bloomFilterIn.hashCode());
// Log.d(TAG, "bloomFilterOut hash code: " + bloomFilterOut.hashCode());
// Log.d(TAG, "Are both compatible: " + bloomFilterIn.isCompatible(bloomFilterOut));
// Log.d(TAG, "bloomFilterIn contains \"item1\"?: " + bloomFilterIn.mightContain("item1"));
// Log.d(TAG, "bloomFilterIn contains \"item2\"?: " + bloomFilterIn.mightContain("item2"));
// Log.d(TAG, "bloomFilterOut contains \"item1\"?: " + bloomFilterOut.mightContain("item1"));
// Log.d(TAG, "bloomFilterOut contains \"item2\"?: " + bloomFilterOut.mightContain("item2"));
public class ContentAdvertisement {

    private BloomFilter<CharSequence> bloomFilter;

    private static final String TAG = "ContentAdvertisement";
    // Empty constructor
    public ContentAdvertisement(){
         bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 50,0.03);
    }

    public ContentAdvertisement(String filter){
        ByteArrayInputStream in = new ByteArrayInputStream(filter.getBytes());
        try {
            G.Log(TAG,"Filter "+filter);
            bloomFilter = BloomFilter.readFrom(in, Funnels.stringFunnel(Charset.defaultCharset()));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bloomFilter.writeTo(out);
            G.Log(TAG,"New filter "+out.size()+" "+out.toString());
        } catch (Exception e){G.Log("Exception "+e);}

    }

    public void addElement(String str)
    {
        G.Log(TAG,"Add element "+str);
        bloomFilter.put(str);

       // G.Log(TAG,"Contains elemment "+bloomFilter.mightContain("/source".getBytes()));
    }

    public boolean checkElement(String str)
    {
        return bloomFilter.mightContain(str);

    }

    public String getFilter()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try{
            bloomFilter.writeTo(out);

            G.Log(TAG,"BF size "+out.size());
        } catch (IOException e)
        {
            G.Log(TAG,"Exception "+e);
        }

        return out.toString();
    }

    public BloomFilter getBloomFilter()
    {
        return bloomFilter;
    }


    public boolean compareFilters(String filter){


        return connectFilter(filter);
    }

    public boolean connectFilter(String filter){
        /*ContentAdvertisement ca = new ContentAdvertisement(filter);

         G.Log(TAG,"BF received size "+ca.getFilter());
        G.Log(TAG,"Hash "+ ca.getBloomFilter().hashCode() +" "+ca.checkElement("/source"));
        G.Log(TAG,"Hash "+bloomFilter.hashCode());

        if(filter.equals(getFilter()))return false;
       //if(source.equals("source")||id<)
        if(ca.checkElement("/source")) {
            try {
                ContentAdvertisement ca2 = (ContentAdvertisement) this.clone();
                ca2.addElement("/source");
                return !ca2.getFilter().equals(filter);
            } catch (Exception e) {
            }

           // return true;
        }else
            return ca.getBloomFilter().hashCode() < bloomFilter.hashCode();
*/
        return !getFilter().equals(filter);
        //return false;
    }

    //Putting elements into the filter
    //A BigInteger representing a key of some sort
    //Testing for element in set
}
