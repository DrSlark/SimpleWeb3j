package web3j.protocol.core.methods.response;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

import web3j.protocol.core.Response;

/**
 * eth_getBalance.
 */
public class EthGetBalance extends Response<String> {
    public BigInteger getBalance() {
        return Numeric.decodeQuantity(getResult());
    }
}
