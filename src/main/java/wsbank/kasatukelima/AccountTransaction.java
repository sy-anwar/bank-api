package wsbank.kasatukelima;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/** 
 * Class AccountTransaction.
 * @author K01-05
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "userName",
    "accountNumber",
    "balance",
    "type",
    "amount",
    "accNumTrx",
    "transactionTime"
})

/**
 * 
 */
public class AccountTransaction {
  @XmlElement(name = "userName", required = true)
  private String userName;
  @XmlElement(name = "accountNumber", required = true)
  private long accountNumber;
  @XmlElement(name = "balance", required = true)
  private double balance;
  @XmlElement(name = "type", required = true)
  private String type;
  @XmlElement(name = "amount", required = true)
  private double amount;
  @XmlElement(name = "accNumTrx", required = true)
  private long accNumTrx;
  @XmlElement(name = "transactionTime", required = true)
  private String transactionTime; 

  /**
   * Constructor Class AccountTransaction.
   */
  public AccountTransaction() {
    this.userName = "";
    this.accountNumber = 0;
    this.balance = 0;
    this.type = "";
    this.amount = 0;
    this.accNumTrx = 0;
    this.transactionTime = "";
  }

  /**
   * Constructor Class AccountTransaction.
   * @param userName represents owner name
   * @param accountNumber represents account bank number
   * @param balance represents balance of account
   * @param type represents type of transaction
   * @param amount represents value of transaction
   * @param accNumTrx represents target number (account nnumber / virtual account)
   * @param transactionTime represents time of the transaction
   */
  public AccountTransaction(String userName, long accountNumber, double balance, String type,
         double amount, long accNumTrx, String transactionTime) {
    this.userName = userName;
    this.accountNumber = accountNumber;
    this.balance = balance;
    this.type = type;
    this.amount = amount;
    this.accNumTrx = accNumTrx;
    this.transactionTime = transactionTime;
  }

  public void setTransactionTime(final String transactionTime) {
    this.transactionTime = transactionTime;
  }

  public void setUserName(final String userName) {
    this.userName = userName;
  }

  public void setAccountNumber(final long accountNumber) {
    this.accountNumber = accountNumber;
  }

  public void setBalance(final double balance) {
    this.balance = balance;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public void setAmount(final double amount) {
    this.amount = amount;
  }

  public void setAccNumTrx(final long accNumTrx) {
    this.accNumTrx = accNumTrx;
  }  
}
