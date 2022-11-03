/*  ( 1 ) Import Java Classes                                         */


import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;



/* Class definition                                                   */


public class LabUpdate {

    /* Register the class with the db2 Driver                             */


    static {
        try {

            /* ( 2 ) Load the DB2 Driver                             */

            Class.forName("COM.ibm.db2.jdbc.app.DB2Driver");
        } catch (Exception e) {
            System.out.println("\n  Error loading DB2 Driver...\n");
            System.out.println(e);
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

        String name = "";
        java.lang.String deptno = "";
        short id = 0;
        double salary = 0;
        String job = "";
        short NumEmp = 0;
        String sqlstmt = "UPDATE STAFF SET SALARY = SALARY * 1.05 WHERE DEPT = ?";
        String s = " ";
        int mydeptno = 0;
        int SQLCode = 0;
        String SQLState = "     ";
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


        s = JOptionPane.showInputDialog(null,
                "部门涨薪5%，请输入一个部门的编号",
                "55200628作品",
                JOptionPane.QUESTION_MESSAGE);
        if (s == null) {
            System.out.println("user canceled operation");
            System.exit(-1);
        }
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

            int updateCount = pstmt.executeUpdate();

            System.out.println("\nNumber of rows updated: " + updateCount);
            JOptionPane.showMessageDialog(null, "更新了的行数" + updateCount);
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
