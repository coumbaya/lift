package mylift;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class Main for launchng program with user defined configuration options
 * and either load an execution trace or init LIFT BGP deduction
 *
 * @author Nassopoulos Georges
 * @version 1.0
 * @since 2016-12-15
 */
public class Main {

    /**
     * This is the main method for launching the programm.
     *
     * @param args parameters passed from user, in order to choose LIFT's comportement
     */
    public static final void main(final String[] args) {

        final Configuration myConf = new Configuration();
        final InitLift myDeduction = new InitLift();
        
        try {

            // set LIFT arguments, defined by user
            myConf.setParameteres(args);
            // initialize LIFT programm (load execution trace or init BGP deduction)
            myDeduction.initProcessing();

        } catch (Exception ex) {

            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}