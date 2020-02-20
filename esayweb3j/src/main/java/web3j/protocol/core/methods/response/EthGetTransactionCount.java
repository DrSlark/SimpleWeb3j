package web3j.protocol.core.methods.response;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

import web3j.protocol.core.Response;

/**
 * eth_getTransactionCount.
 */
public class EthGetTransactionCount extends Response<String> {
    public BigInteger getTransactionCount() {
        return Numeric.decodeQuantity(getResult());
    }
}
