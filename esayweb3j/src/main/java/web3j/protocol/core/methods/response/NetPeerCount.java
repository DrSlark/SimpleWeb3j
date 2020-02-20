package web3j.protocol.core.methods.response;

import org.web3j.utils.Numeric;

import java.math.BigInteger;

import web3j.protocol.core.Response;

/**
 * net_peerCount.
 */
public class NetPeerCount extends Response<String> {

    public BigInteger getQuantity() {
        return Numeric.decodeQuantity(getResult());
    }
}
