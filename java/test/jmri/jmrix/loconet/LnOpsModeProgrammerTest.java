package jmri.jmrix.loconet;

import jmri.ProgrammingMode;
import jmri.managers.DefaultProgrammerManager;
import jmri.ProgListenerScaffold;
import jmri.ProgrammerException;

import junit.framework.Assert;
import junit.framework.TestCase;

public class LnOpsModeProgrammerTest extends TestCase {

    LocoNetInterfaceScaffold lnis;
    SlotManager sm;
    LocoNetSystemConnectionMemo memo;
    ProgListenerScaffold pl;
    
    public void testSetMode() {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        try {
            lnopsmodeprogrammer.setMode(DefaultProgrammerManager.PAGEMODE);
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail("No IllegalArgumentException thrown");

    }

    public void testGetMode() {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        ProgrammingMode intRet = lnopsmodeprogrammer.getMode();
        Assert.assertEquals("OpsByteMode", DefaultProgrammerManager.OPSBYTEMODE, intRet);
    }

    public void testGetCanRead() {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        Assert.assertEquals("ops mode always can read", true,
                lnopsmodeprogrammer.getCanRead());
    }

    public void testSV2DataBytes() {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        LocoNetMessage m = new LocoNetMessage(15);

        // check data bytes
        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x12345678);
        Assert.assertEquals(0x10, m.getElement(10));
        Assert.assertEquals(0x78, m.getElement(11));
        Assert.assertEquals(0x56, m.getElement(12));
        Assert.assertEquals(0x34, m.getElement(13));
        Assert.assertEquals(0x12, m.getElement(14));
    }
    
    public void testSV2highBits() {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        LocoNetMessage m = new LocoNetMessage(15);

        // check high bits
        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x01020384);
        Assert.assertEquals(0x11, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x01028304);
        Assert.assertEquals(0x12, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x01820304);
        Assert.assertEquals(0x14, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x81020304);
        Assert.assertEquals(0x18, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x81828384);
        Assert.assertEquals(0x1F, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));
    }
    
 
     public void testSvWrite() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);
        
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETSV2MODE);
        lnopsmodeprogrammer.writeCV("22",33,pl);
        
        // should have written and not returned
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());
        
        // turn the message around as a reply
        LocoNetMessage m = lnis.outbound.get(0);
        m.setElement(3, m.getElement(3) | 0x40);
        lnopsmodeprogrammer.message(m);

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        
     }

     public void testSvRead() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);
        
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETSV2MODE);
        lnopsmodeprogrammer.readCV("22",pl);
        
        // should have written and not returned
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());
        
        int testVal = 130;
        
        // turn the message around as a reply
        LocoNetMessage m = lnis.outbound.get(0);
        m.setElement(3, m.getElement(3) | 0x40);
        m.setElement(10, (m.getElement(10)&0x7E) | ((testVal & 0x80) != 0 ? 1 : 0));
        m.setElement(11, testVal & 0x7F);
        lnopsmodeprogrammer.message(m);

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 130, pl.getRcvdValue());
        
     }
   
    // from here down is testing infrastructure
    public LnOpsModeProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LnOpsModeProgrammerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        
        lnis = new LocoNetInterfaceScaffold();
        sm = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis, sm);
        pl = new ProgListenerScaffold();

    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
