/*
 * This file is released under terms of BSD license
 * See LICENSE file for more information
 */
package cx2x.translator.transformation.primitive;

import cx2x.configuration.Configuration;
import cx2x.configuration.CompilerDirective;
import cx2x.configuration.Target;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Test methods of the Module class.
 *
 * @author clementval
 */
public class ModuleTest {

  @Test
  public void getSuffixTest() {
    Configuration config = Configuration.get();
    config.init(CompilerDirective.OPENACC, Target.GPU);
    // .[directive].[target].claw
    assertEquals(".internal.gpu.claw.xmod", Module.getSuffix());
    config.init(CompilerDirective.OPENMP, Target.CPU);
    assertEquals(".openmp.cpu.claw.xmod", Module.getSuffix());
    config.init(CompilerDirective.NONE, Target.CPU);
    assertEquals(".none.cpu.claw.xmod", Module.getSuffix());
    config.init(CompilerDirective.OPENMP, Target.MIC);
    assertEquals(".openmp.mic.claw.xmod", Module.getSuffix());
    config.init(CompilerDirective.NONE, Target.FPGA);
    assertEquals(".none.fpga.claw.xmod", Module.getSuffix());
    config.init(CompilerDirective.OPENACC, null);
    assertEquals(".internal.none.claw.xmod", Module.getSuffix());
    config.init(null, null);
    assertEquals(".none.none.claw.xmod", Module.getSuffix());
    config.init(CompilerDirective.NONE, Target.GPU);
    assertEquals(".none.gpu.claw.xmod", Module.getSuffix());
  }
}
