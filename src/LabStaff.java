/*  ( 1 ) Import Java Classes                                         */


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;



/* Class definition                                                   */


public class LabStaff {

    /* Register the class with the db2 Driver                             */


    static {
        try {

            /* ( 2 ) Load the DB2 Driver                             */

            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (Exception e) {
            System.out.println("\n  Error loading DB2 Driver...\n");
            e.printStackTrace();
            System.exit(1);
        }
    }


    /* Main routine                                                       */


    public static void main(String[] args) throws Exception {

        /* Define variable declarations for the variable which will be used   */
        /* to pass data to and from the stored procedure:                     */
        /* A character string for passing the department in.                  */
        /* A double for returning the median salary.                          */
        /* An integer for returning the number of employees.                  */

        String name;
        String deptno;
        short id = 0;
        double salary = 0;
        String job;
        String sqlstmt = "select name, job, salary from staff where STAFF.DEPT = 10";
        String s;
        int mydeptno;
        int SQLCode;
        String blanks = "                                                        ";
        String SQLState;

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));


        /*  Establish connection and set default context  */
        System.out.println("Connect statement follows:");


        /* ( 3 ) Code a statement that will connect to the database SAMPLE. */
        /*       Define the connection object named sample.                 */
        /*       Use the userid udba and the password udba                  */

        Connection sample = DriverManager.getConnection("jdbc:db2://192.168.245.128:50000/sample", "student", "student");
        System.out.println("Connect completed");
        sample.setAutoCommit(false);


        try {
            Statement stmt = sample.createStatement();
            System.out.println("\n Batch Statements begin ");
            stmt.addBatch("INSERT INTO DEPARTMENT VALUES ('BT6','BATCH7 NEWYORK','200340','E21','NEW YORK CITY6')");
            stmt.addBatch("INSERT INTO DEPARTMENT VALUES ('BT7','BATCH7 NEWYORK','200340','E21','NEW YORK CITY7')");
            stmt.addBatch("INSERT INTO DEPARTMENT VALUES ('BT8','BATCH7 NEWYORK','200340','E21','NEW YORK CITY8')");
            stmt.addBatch("INSERT INTO DEPARTMENT VALUES ('BT9','BATCH7 NEWYORK','200340','E21','NEW YORK CITY9')");
            stmt.addBatch("INSERT INTO DEPARTMENT VALUES ('BTA','BATCH7 NEWYORK','200340','E21','NEW YORK CITY10')");
            int[] updateCounts = stmt.executeBatch();
            System.out.println("updateCounts = " + Arrays.toString(updateCounts));
            sample.commit();
        }  // end try
        catch (SQLException x) {

            /* (8) An error has occurred.  Retrieve the SQLCode                   */

            SQLCode = x.getErrorCode();
            SQLState = x.getSQLState();
            String Message = x.getMessage();
            System.out.println("\nSQLCODE:  " + SQLCode);
            System.out.println("\nSQLSTATE: " + SQLState);
            System.out.println("\nSQLERRM:  " + Message);
        }

        System.exit(0);
    } // end main


}  // end of kegstaff class
