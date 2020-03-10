/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wsbank.kasatukelima;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/** 
 * Class WebServiceBank.
 * @author K01-05
 */
@WebService(serviceName = "WebServiceBank")
public class WebServiceBank {

  /**
   * Web service validateAccountNumber.
   * @param accountNumber representasi nomor rekening yang akan divalidasi
   * @return 
   */
  @WebMethod(operationName = "validateAccountNumber")
  public int validateAccountNumber(@WebParam(name = "accountNumber") long accountNumber) {
    String dbUrl = "jdbc:mariadb://localhost/bankdb?useUnicode=true&useJDBCCompliant" 
        + "TimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    String dbClass = "org.mariadb.jdbc.Driver";
    String query = "SELECT * FROM akun WHERE nomor_rekening = " + accountNumber + ";";
    String userName = "root"; 
    String password = "";
    try {
      Class.forName(dbClass);
      Connection con = DriverManager.getConnection(dbUrl, userName, password); 
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      if (rs.next()) { 
        stmt.close();
        con.close();
        return 200;
      } else { 
        return 404; 
      }
    } catch (ClassNotFoundException e) { 
      return 500; 
    } catch (SQLException e) { 
      return 501; 
    }
  }

  /**
   * Web service getAccountDetail.
   * @param accountNumber merepresentasikan nomor rekening yang akan dicari detailnya
   * @return 
   */
  @WebMethod(operationName = "getAccountDetail")
  public AccountTransaction[] getAccountDetail(@WebParam(name = "accountNumber") 
      long accountNumber) {
    String dbUrl = "jdbc:mariadb://localhost/bankdb?useUnicode=true&useJDBCCompliant" 
        + "TimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    String dbClass = "org.mariadb.jdbc.Driver";
    String userName = "root";
    String password = "";
    String query = "SELECT * FROM akun WHERE nomor_rekening="  + accountNumber + ";";
    ArrayList<AccountTransaction> listTransaction = new ArrayList<AccountTransaction>();
    try {
      /* connecting to database */
      Class.forName(dbClass);
      Connection con = DriverManager.getConnection(dbUrl, userName, password); 
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      if (rs.next()) {
        AccountTransaction transData = new AccountTransaction();
        long accNumber = rs.getLong("nomor_rekening");
        transData.setAccountNumber(accNumber);
        String user = rs.getString("nama_pemilik");
        transData.setUserName(user);
        double balance = rs.getDouble("saldo");
        transData.setBalance(balance);
        long idAccount = rs.getLong("id_nasabah");
        // Search all transaction done by user
        query = "SELECT * FROM transaksibank WHERE id_nasabah = " + idAccount + ";";
        rs = stmt.executeQuery(query);
        boolean isTrxExist = false;
        while (rs.next()) {
          transData.setType(rs.getString("jenis_transaksi"));
          transData.setAmount(rs.getDouble("jumlah_transaksi"));
          transData.setAccNumTrx(rs.getLong("nomor_akun"));
          transData.setTransactionTime(rs.getString("waktu"));
          listTransaction.add(transData);
          isTrxExist = true;
        } 
        if (!isTrxExist) {
          listTransaction.add(transData);
        }
        stmt.close();
        con.close();
        return listTransaction.toArray(new AccountTransaction[0]);
      } else {
        return null;
      }
    } catch (ClassNotFoundException | SQLException e) { 
      return null; 
    }
  }

  /**
   * Web service transfer.
   * @param senderAccountNumber account number of sender
   * @param recipientsAccountNumber account number / virtual account recipients
   * @param amount vaue of transaction
   * @return 
   */
  @WebMethod(operationName = "transfer")
  public String transfer(@WebParam(name = "senderAccountNumber") long senderAccountNumber, 
      @WebParam(name = "recipientsAccountNumber") long recipientsAccountNumber, 
      @WebParam(name = "amount") double amount) throws SQLException {
    String dbUrl = "jdbc:mariadb://localhost/bankdb?useUnicode=true&useJDBCCompliant" 
        + "TimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    String dbClass = "org.mariadb.jdbc.Driver";
    String userName = "root"; 
    String password = "";
    double balance = 0;
    int idTransaction = 0;
    long idAccSender = 0L;
    long idAccReceiver = 0L;
    long accNumReceiver;
    
    // validate recipientsAccountNumber/virtualAcc
    boolean isExistRekening = false;
    try {
      /* connecting to database */
      Class.forName(dbClass);
      Connection con = DriverManager.getConnection(dbUrl, userName, password); 
      Statement stmt = con.createStatement();
      String query = "SELECT * from akun WHERE nomor_rekening = " + recipientsAccountNumber + ";";
      ResultSet rs = stmt.executeQuery(query);
      if (rs.next()) {
        idAccReceiver = rs.getLong("id_nasabah");
        isExistRekening = true;
      }
      stmt.close();
      con.close();
    } catch (ClassNotFoundException | SQLException e) {
      return "Exception";
    }

    // Check maybe receiver account number is a virtual account, not rekening
    if (!isExistRekening) {
      try {
        /* connecting to database */
        Class.forName(dbClass);
        Connection con = DriverManager.getConnection(dbUrl, userName, password); 
        Statement stmt = con.createStatement();
        String query = "SELECT * from akun_virtual WHERE nomor_akun_virtual = " 
            + recipientsAccountNumber + ";";
        ResultSet rs = stmt.executeQuery(query);
        if (rs.next()) {
          accNumReceiver = rs.getLong("nomor_rekening");
          String queri;
          queri = "SELECT id_nasabah FROM akun WHERE nomor_rekening = " + accNumReceiver + ";";
          rs = stmt.executeQuery(queri);
          if (rs.next()) {
            idAccReceiver = rs.getLong("id_nasabah");
          }
        } else {
          return "AccountNotValid";
        }
        stmt.close();
        con.close();
      } catch (ClassNotFoundException | SQLException e) {
        return "Exception";
      }
    }

    // Check is enough saldo
    try {
      /* connecting to database */
      Class.forName(dbClass);
      Connection con = DriverManager.getConnection(dbUrl, userName, password); 
      Statement stmt = con.createStatement();
      String query = "SELECT * from akun WHERE nomor_rekening = " + senderAccountNumber + ";";
      ResultSet rs = stmt.executeQuery(query);
      if (rs.next()) {
        balance = rs.getDouble("saldo");
        idAccSender = rs.getLong("id_nasabah");
      }
      if (balance < amount) {
        return "BalanceNotEnough";
      }

      // Can do transaction, get last id transaction to make a new id transaction
      query = "SELECT MAX(id_transaksi) from transaksibank;";
      rs = stmt.executeQuery(query);
      if (rs.next()) {
        long idTransactionOld = rs.getLong("MAX(id_transaksi)");
        idTransaction += idTransactionOld;
      }
      idTransaction += 1;           

      // Insert into sender as debit
      query = "INSERT INTO transaksibank VALUES (" + idTransaction + ", " + idAccSender 
          + ",  \"DEBIT\", " + amount + ", " + recipientsAccountNumber + ", now());";
      int insert = stmt.executeUpdate(query);
      if (insert <= 0) { 
        return "InsertFailed"; 
      }
      // Insert into receiver as kredit
      query = "INSERT INTO transaksibank VALUES (" + idTransaction + ", " + idAccReceiver 
          + ",  \"CREDIT\", " + amount + ", " + senderAccountNumber + ", now());";
      insert = stmt.executeUpdate(query);
      if (insert <= 0) {
        return "InsertFailed"; 
      }
        
      // UPDATE BALANCE
      // SENDER
      query = "UPDATE akun SET saldo = saldo - " + amount + "WHERE id_nasabah = " 
          + idAccSender + ";";
      int update = stmt.executeUpdate(query);
      if (update <= 0) {
        return "UpdateFailed";
      }

      // RECEIVER
      query = "UPDATE akun SET saldo = saldo + " + amount + "WHERE id_nasabah = " 
          + idAccReceiver + ";";
      update = stmt.executeUpdate(query);
      if (update <= 0) {
        return "UpdateFailed";
      }            
      stmt.close();
      con.close();
      return "OK";
    } catch (ClassNotFoundException | SQLException e) {
      return "Exception";
    }
  }

  /**
   * Web service createVirtualAccount.
   * @param accountNumber nomor rekening yang akan dibuat virtual akun nya
   * @return 
   */
  @WebMethod(operationName = "createVirtualAccount")
  public long createVirtualAccount(@WebParam(name = "accountNumber") long accountNumber) {
    String dbUrl = "jdbc:mariadb://localhost/bankdb?useUnicode=true&useJDBCCompliant" 
        + "TimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    String dbClass = "org.mariadb.jdbc.Driver";
    String query = "SELECT * FROM akun_virtual;";
    String userName = "root";
    String password = "";
    try {
      Class.forName(dbClass);
      Connection con = DriverManager.getConnection(dbUrl, userName, password); 
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      
      ArrayList<Long> virtAccounts = new ArrayList<>();
      while (rs.next()) { 
        virtAccounts.add(rs.getLong(2));
      }
      long virtAcc = 0L;
      Random rand = new Random();
      boolean used = true;
                            
      while (used) {
        // create 16 digits number
        virtAcc = 1 + rand.nextInt(9); // ensures that the 16th digit isn't 0
        for (int i = 0; i < 15; i++) {
          virtAcc *= 10L;
          virtAcc += rand.nextInt(10);
        }
        // check unique
        if (virtAccounts.indexOf(virtAcc) == -1) { 
          used = false;
        }
      }
      query = "INSERT INTO akun_virtual VALUES(" + accountNumber + ", " + virtAcc + ");";
      
      int insert = stmt.executeUpdate(query);
      con.close();
      if (insert > 0) {
        return virtAcc;  // inserted
      } else if (insert == 0) { 
        return 100; // no rows affected
      } else { 
        return 404; // if the operation is a mass delete on a segmented table space
      }             
    } catch (ClassNotFoundException e) { 
      return 500;
    } catch (SQLException e) { 
      return 501;
    }
  }

  /**
   * Web service checkCreditTransaction.
   * @param recipientsAccountNumber nomor rekening / virtual akun penerima
   * @param amount jumlah transaksi
   * @param startDate tanggal awal
   * @param endDate tanggal akhir
   * @return 
   */
  @WebMethod(operationName = "checkCreditTransaction")
  public int checkCreditTransaction(@WebParam(name = "recipientsAccountNumber") 
      long recipientsAccountNumber, @WebParam(name = "amount") double amount, 
      @WebParam(name = "startDate") String startDate, @WebParam(name = "endDate") String endDate) {
    String dbUrl = "jdbc:mariadb://localhost/bankdb?useUnicode=true&useJDBCCompliant" 
        + "TimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    String dbClass = "org.mariadb.jdbc.Driver";
    String query;
    query = "SELECT * FROM transaksibank WHERE jenis_transaksi = \"CREDIT\" AND nomor_akun = " 
        + recipientsAccountNumber + " AND jumlah_transaksi = " + amount + " AND waktu BETWEEN \"" 
        + startDate + "\" AND \"" + endDate + "\";";
    String userName = "root";
    String password = "";
    try {
      Class.forName(dbClass);
      Connection con = DriverManager.getConnection(dbUrl, userName, password); 
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      
      if (rs.next()) { 
        return 200; // OK
      } else { 
        return 404; // No Credit Transaction
      }
                                
    } catch (ClassNotFoundException e) { 
      return 500;
    } catch (SQLException e) {
      return 501;
    }      
  }
}