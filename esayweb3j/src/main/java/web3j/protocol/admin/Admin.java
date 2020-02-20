package web3j.protocol.admin;

import java.math.BigInteger;
import java.util.concurrent.ScheduledExecutorService;

import web3j.protocol.Web3j;
import web3j.protocol.Web3jService;
import web3j.protocol.admin.methods.response.NewAccountIdentifier;
import web3j.protocol.admin.methods.response.PersonalListAccounts;
import web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import web3j.protocol.core.Request;
import web3j.protocol.core.methods.request.Transaction;
import web3j.protocol.core.methods.response.EthSendTransaction;

/**
 * JSON-RPC Request object building factory for common Parity and Geth. 
 */
public interface Admin extends Web3j {

    static Admin build(Web3jService web3jService) {
        return new JsonRpc2_0Admin(web3jService);
    }
    
    static Admin build(
            Web3jService web3jService, long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        return new JsonRpc2_0Admin(web3jService, pollingInterval, scheduledExecutorService);
    }

    public Request<?, PersonalListAccounts> personalListAccounts();
    
    public Request<?, NewAccountIdentifier> personalNewAccount(String password);
    
    public Request<?, PersonalUnlockAccount> personalUnlockAccount(
            String address, String passphrase, BigInteger duration);
    
    public Request<?, PersonalUnlockAccount> personalUnlockAccount(
            String address, String passphrase);
    
    public Request<?, EthSendTransaction> personalSendTransaction(
            Transaction transaction, String password);

}   
