package org.opentox.toxotis.core;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentox.toxotis.ToxOtisException;
import org.opentox.toxotis.client.VRI;
import org.opentox.toxotis.client.collection.Media;
import org.opentox.toxotis.client.collection.Services;
import static org.junit.Assert.*;

/**
 *
 * @author chung
 */
public class CompoundTest {

    public CompoundTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

  //  @Test
    public void testSomeMethod() throws ToxOtisException {
        Compound c =new Compound(Services.IDEACONSULT.augment("compound","100"));
        c.downloadAsFile(new File("/home/chung/Desktop/b.sdf"), Media.CHEMICAL_MDLSDF.getMime(), null);
    }

    @Test
    public void testPublishFromFile() throws ToxOtisException {
       File f;
       Compound comp = new Compound(new VRI(Services.AMBIT_UNI_PLOVDIV.augment("compound","4")));
       comp.downloadAsFile(f = new File("/Users/hampos/Desktop/b.sdf"), Media.CHEMICAL_MDLSDF.getMime(), null);

       Compound c = Compound.publishFromFile(f, Media.CHEMICAL_SMILES.getMime(), null);
       System.out.println(c.getUri().toString());
    }

}