package jmri.jmrix.dccpp.swing.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of PacketGenFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class PacketGenFrameTest {

    jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PacketGenFrame action = new PacketGenFrame();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.jmrix.dccpp.DCCppInterfaceScaffold t = new jmri.jmrix.dccpp.DCCppInterfaceScaffold(new jmri.jmrix.dccpp.DCCppCommandStation());
        memo = new jmri.jmrix.dccpp.DCCppSystemConnectionMemo(t);

        jmri.InstanceManager.store(memo, jmri.jmrix.dccpp.DCCppSystemConnectionMemo.class);
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
