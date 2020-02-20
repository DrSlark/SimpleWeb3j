package web3j.protocol.core;

import java.math.BigInteger;

import web3j.protocol.core.methods.request.ShhFilter;
import web3j.protocol.core.methods.request.Transaction;
import web3j.protocol.core.methods.response.DbGetHex;
import web3j.protocol.core.methods.response.DbGetString;
import web3j.protocol.core.methods.response.DbPutHex;
import web3j.protocol.core.methods.response.DbPutString;
import web3j.protocol.core.methods.response.EthAccounts;
import web3j.protocol.core.methods.response.EthBlock;
import web3j.protocol.core.methods.response.EthBlockNumber;
import web3j.protocol.core.methods.response.EthCall;
import web3j.protocol.core.methods.response.EthCoinbase;
import web3j.protocol.core.methods.response.EthCompileLLL;
import web3j.protocol.core.methods.response.EthCompileSerpent;
import web3j.protocol.core.methods.response.EthCompileSolidity;
import web3j.protocol.core.methods.response.EthEstimateGas;
import web3j.protocol.core.methods.response.EthFilter;
import web3j.protocol.core.methods.response.EthGasPrice;
import web3j.protocol.core.methods.response.EthGetBalance;
import web3j.protocol.core.methods.response.EthGetBlockTransactionCountByHash;
import web3j.protocol.core.methods.response.EthGetBlockTransactionCountByNumber;
import web3j.protocol.core.methods.response.EthGetCode;
import web3j.protocol.core.methods.response.EthGetCompilers;
import web3j.protocol.core.methods.response.EthGetStorageAt;
import web3j.protocol.core.methods.response.EthGetTransactionCount;
import web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import web3j.protocol.core.methods.response.EthGetUncleCountByBlockHash;
import web3j.protocol.core.methods.response.EthGetUncleCountByBlockNumber;
import web3j.protocol.core.methods.response.EthGetWork;
import web3j.protocol.core.methods.response.EthHashrate;
import web3j.protocol.core.methods.response.EthLog;
import web3j.protocol.core.methods.response.EthMining;
import web3j.protocol.core.methods.response.EthProtocolVersion;
import web3j.protocol.core.methods.response.EthSendTransaction;
import web3j.protocol.core.methods.response.EthSign;
import web3j.protocol.core.methods.response.EthSubmitHashrate;
import web3j.protocol.core.methods.response.EthSubmitWork;
import web3j.protocol.core.methods.response.EthSyncing;
import web3j.protocol.core.methods.response.EthTransaction;
import web3j.protocol.core.methods.response.EthUninstallFilter;
import web3j.protocol.core.methods.response.NetListening;
import web3j.protocol.core.methods.response.NetPeerCount;
import web3j.protocol.core.methods.response.NetVersion;
import web3j.protocol.core.methods.response.ShhAddToGroup;
import web3j.protocol.core.methods.response.ShhHasIdentity;
import web3j.protocol.core.methods.response.ShhMessages;
import web3j.protocol.core.methods.response.ShhNewFilter;
import web3j.protocol.core.methods.response.ShhNewGroup;
import web3j.protocol.core.methods.response.ShhNewIdentity;
import web3j.protocol.core.methods.response.ShhPost;
import web3j.protocol.core.methods.response.ShhUninstallFilter;
import web3j.protocol.core.methods.response.ShhVersion;
import web3j.protocol.core.methods.response.Web3ClientVersion;
import web3j.protocol.core.methods.response.Web3Sha3;

/**
 * Core Ethereum JSON-RPC API.
 */
public interface Ethereum {
    Request<?, Web3ClientVersion> web3ClientVersion();

    Request<?, Web3Sha3> web3Sha3(String data);

    Request<?, NetVersion> netVersion();

    Request<?, NetListening> netListening();

    Request<?, NetPeerCount> netPeerCount();

    Request<?, EthProtocolVersion> ethProtocolVersion();

    Request<?, EthCoinbase> ethCoinbase();

    Request<?, EthSyncing> ethSyncing();

    Request<?, EthMining> ethMining();

    Request<?, EthHashrate> ethHashrate();

    Request<?, EthGasPrice> ethGasPrice();

    Request<?, EthAccounts> ethAccounts();

    Request<?, EthBlockNumber> ethBlockNumber();

    Request<?, EthGetBalance> ethGetBalance(
            String address, DefaultBlockParameter defaultBlockParameter);

    Request<?, EthGetStorageAt> ethGetStorageAt(
            String address, BigInteger position,
            DefaultBlockParameter defaultBlockParameter);

    Request<?, EthGetTransactionCount> ethGetTransactionCount(
            String address, DefaultBlockParameter defaultBlockParameter);

    Request<?, EthGetBlockTransactionCountByHash> ethGetBlockTransactionCountByHash(
            String blockHash);

    Request<?, EthGetBlockTransactionCountByNumber> ethGetBlockTransactionCountByNumber(
            DefaultBlockParameter defaultBlockParameter);

    Request<?, EthGetUncleCountByBlockHash> ethGetUncleCountByBlockHash(String blockHash);

    Request<?, EthGetUncleCountByBlockNumber> ethGetUncleCountByBlockNumber(
            DefaultBlockParameter defaultBlockParameter);

    Request<?, EthGetCode> ethGetCode(String address, DefaultBlockParameter defaultBlockParameter);

    Request<?, EthSign> ethSign(String address, String sha3HashOfDataToSign);

    Request<?, EthSendTransaction> ethSendTransaction(
            Transaction transaction);

    Request<?, EthSendTransaction> ethSendRawTransaction(
            String signedTransactionData);

    Request<?, EthCall> ethCall(
            Transaction transaction,
            DefaultBlockParameter defaultBlockParameter);

    Request<?, EthEstimateGas> ethEstimateGas(
            Transaction transaction);

    Request<?, EthBlock> ethGetBlockByHash(String blockHash, boolean returnFullTransactionObjects);

    Request<?, EthBlock> ethGetBlockByNumber(
            DefaultBlockParameter defaultBlockParameter,
            boolean returnFullTransactionObjects);

    Request<?, EthTransaction> ethGetTransactionByHash(String transactionHash);

    Request<?, EthTransaction> ethGetTransactionByBlockHashAndIndex(
            String blockHash, BigInteger transactionIndex);

    Request<?, EthTransaction> ethGetTransactionByBlockNumberAndIndex(
            DefaultBlockParameter defaultBlockParameter, BigInteger transactionIndex);

    Request<?, EthGetTransactionReceipt> ethGetTransactionReceipt(String transactionHash);

    Request<?, EthBlock> ethGetUncleByBlockHashAndIndex(
            String blockHash, BigInteger transactionIndex);

    Request<?, EthBlock> ethGetUncleByBlockNumberAndIndex(
            DefaultBlockParameter defaultBlockParameter, BigInteger transactionIndex);

    Request<?, EthGetCompilers> ethGetCompilers();

    Request<?, EthCompileLLL> ethCompileLLL(String sourceCode);

    Request<?, EthCompileSolidity> ethCompileSolidity(String sourceCode);

    Request<?, EthCompileSerpent> ethCompileSerpent(String sourceCode);

    Request<?, EthFilter> ethNewFilter(web3j.protocol.core.methods.request.EthFilter ethFilter);

    Request<?, EthFilter> ethNewBlockFilter();

    Request<?, EthFilter> ethNewPendingTransactionFilter();

    Request<?, EthUninstallFilter> ethUninstallFilter(BigInteger filterId);

    Request<?, EthLog> ethGetFilterChanges(BigInteger filterId);

    Request<?, EthLog> ethGetFilterLogs(BigInteger filterId);

    Request<?, EthLog> ethGetLogs(web3j.protocol.core.methods.request.EthFilter ethFilter);

    Request<?, EthGetWork> ethGetWork();

    Request<?, EthSubmitWork> ethSubmitWork(String nonce, String headerPowHash, String mixDigest);

    Request<?, EthSubmitHashrate> ethSubmitHashrate(String hashrate, String clientId);

    Request<?, DbPutString> dbPutString(String databaseName, String keyName, String stringToStore);

    Request<?, DbGetString> dbGetString(String databaseName, String keyName);

    Request<?, DbPutHex> dbPutHex(String databaseName, String keyName, String dataToStore);

    Request<?, DbGetHex> dbGetHex(String databaseName, String keyName);

    Request<?, ShhPost> shhPost(
            web3j.protocol.core.methods.request.ShhPost shhPost);

    Request<?, ShhVersion> shhVersion();

    Request<?, ShhNewIdentity> shhNewIdentity();

    Request<?, ShhHasIdentity> shhHasIdentity(String identityAddress);

    Request<?, ShhNewGroup> shhNewGroup();

    Request<?, ShhAddToGroup> shhAddToGroup(String identityAddress);

    Request<?, ShhNewFilter> shhNewFilter(ShhFilter shhFilter);

    Request<?, ShhUninstallFilter> shhUninstallFilter(BigInteger filterId);

    Request<?, ShhMessages> shhGetFilterChanges(BigInteger filterId);

    Request<?, ShhMessages> shhGetMessages(BigInteger filterId);
}
