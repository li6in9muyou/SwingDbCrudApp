/*  ( 1 ) Import Java Classes                                         */


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;



/* Class definition                                                   */


public class LabStaff {

    /* Register the class with the db2 Driver                             */


    static {
        try {

            /* ( 2 ) Load the DB2 Driver                             */

            Class.forName("COM.ibm.db2.jdbc.app.DB2Driver");
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
        String job ;
        String sqlstmt = "select id, name,salary from staff where Dept = ?";
        String s ;
        int mydeptno;
        int SQLCode ;
        String blanks = "                                                        ";
        String SQLState;

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));


        /*  Establish connection and set default context  */
        System.out.println("Connect statement follows:");


        /* ( 3 ) Code a statement that will connect to the database SAMPLE. */
        /*       Define the connection object named sample.                 */
        /*       Use the userid udba and the password udba                  */

        Connection sample = DriverManager.getConnection("jdbc:db2:sample", "student", "student");
        System.out.println("Connect completed");


        /* ( 4 ) Turn autocommit to off                                     */


        sample.setAutoCommit(false);


        /*   Print instruction lines                       */
        System.out.println("This program will update the salaries for a department");
        System.out.println("\n");
        System.out.println("Please enter a department number: \n ");

        /*  Get the department number from the input data */


        s = in.readLine();
        deptno = s.substring(0, 2);
        mydeptno = Integer.parseInt(deptno);


        /*  Issue Select statement  */
        System.out.println("Statement stmt follows");
        try {


            /* ( 5 ) Create the PreparedStatment object name pstmt using the       */
            /* prepareStatement method                                             */


            PreparedStatement pstmt = sample.prepareStatement(sqlstmt);



            /* (6) Set the parameter marker to be value of the department.         */
            /*     This value is placed in the field deptno                        */


            pstmt.setInt(1, mydeptno);


            /* (7) Execute the SQL statement                                       */
            /*     The number of rows modified by the update statment should be    */
            /*     saved in the variable named updateCount                         */

            ResultSet rs = pstmt.executeQuery();
            boolean more = rs.next();
            String salary_text;
            while (more) {
                name = rs.getString(1);
                job = rs.getString(2);
                salary_text = rs.getString(3);
                String outline = (name + blanks.substring(0, 10 - name.length())) + (job + blanks.substring(0, 10 - job.length())) + (salary + blanks.substring(0, 12 - salary_text.length()));
                System.out.println("\n" + outline);

                /* ( 5 ) Move to the next row of the resultset                  */

                more = rs.next();
            }

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
