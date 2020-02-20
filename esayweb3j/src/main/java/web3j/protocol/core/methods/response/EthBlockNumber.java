package web3j.protocol.core.methods.response;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

import web3j.protocol.core.Response;

/**
 * eth_blockNumber.
 */
public class EthBlockNumber extends Response<String> {
    public BigInteger getBlockNumber() {
        return Numeric.decodeQuantity(getResult());
    }
}
