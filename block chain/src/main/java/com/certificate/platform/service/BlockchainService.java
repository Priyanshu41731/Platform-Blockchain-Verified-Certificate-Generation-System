package com.certificate.platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.utils.Numeric;
import jakarta.annotation.PostConstruct;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class BlockchainService {

    @Value("${blockchain.rpc-url}")
    private String rpcUrl;

    @Value("${blockchain.private-key}")
    private String privateKey;

    @Value("${blockchain.contract-address}")
    private String contractAddress;

    private Web3j web3j;
    private Credentials credentials;

    @PostConstruct
    public void init() {
        web3j = Web3j.build(new HttpService(rpcUrl));
        credentials = Credentials.create(privateKey);
    }

    public String registerCertificate(String certificateId, String hash) {
        try {
            Function function = new Function(
                    "registerCertificate",
                    Arrays.asList(new Utf8String(certificateId), new Utf8String(hash)),
                    Collections.emptyList()
            );

            String encodedFunction = FunctionEncoder.encode(function);

            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    DefaultGasProvider.GAS_PRICE,
                    DefaultGasProvider.GAS_LIMIT,
                    contractAddress,
                    encodedFunction
            );

            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, 11155111L, credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

            if (ethSendTransaction.hasError()) {
                throw new RuntimeException("Blockchain error: " + ethSendTransaction.getError().getMessage());
            }

            return ethSendTransaction.getTransactionHash();

        } catch (Exception e) {
            throw new RuntimeException("Failed to register on blockchain: " + e.getMessage());
        }
    }

    public boolean verifyCertificateOnChain(String hash) {
        try {
            Function function = new Function(
                    "verifyCertificate",
                    Arrays.asList(new Utf8String(hash)),
                    Arrays.asList(new TypeReference<Bool>() {})
            );

            String encodedFunction = FunctionEncoder.encode(function);

            EthCall response = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                            credentials.getAddress(),
                            contractAddress,
                            encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
            ).send();

            List<Type> result = FunctionReturnDecoder.decode(
                    response.getValue(),
                    function.getOutputParameters()
            );

            if (result.isEmpty()) return false;
            return (Boolean) result.get(0).getValue();

        } catch (Exception e) {
            return false;
        }
    }
}