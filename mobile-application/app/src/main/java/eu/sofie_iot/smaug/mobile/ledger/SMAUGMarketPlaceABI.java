/*
 * Copyright 2020 Ericsson AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package eu.sofie_iot.smaug.mobile.ledger;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tuples.generated.Tuple6;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.5.16.
 */
@SuppressWarnings("rawtypes")
public class SMAUGMarketPlaceABI extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_ADDMANAGER = "addManager";

    public static final String FUNC_CHANGEOWNER = "changeOwner";

    public static final String FUNC_CLOSEREQUEST = "closeRequest";

    public static final String FUNC_DELETEREQUEST = "deleteRequest";

    public static final String FUNC_GETCLOSEDREQUESTIDENTIFIERS = "getClosedRequestIdentifiers";

    public static final String FUNC_GETMARKETINFORMATION = "getMarketInformation";

    public static final String FUNC_GETOFFER = "getOffer";

    public static final String FUNC_GETOPENREQUESTIDENTIFIERS = "getOpenRequestIdentifiers";

    public static final String FUNC_GETREQUEST = "getRequest";

    public static final String FUNC_GETREQUESTDECISION = "getRequestDecision";

    public static final String FUNC_GETREQUESTDECISIONTIME = "getRequestDecisionTime";

    public static final String FUNC_GETREQUESTOFFERIDS = "getRequestOfferIDs";

    public static final String FUNC_ISOFFERDEFINED = "isOfferDefined";

    public static final String FUNC_ISOWNER = "isOwner";

    public static final String FUNC_ISREQUESTDECIDED = "isRequestDecided";

    public static final String FUNC_ISREQUESTDEFINED = "isRequestDefined";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_RESETACCESSTOKENS = "resetAccessTokens";

    public static final String FUNC_REVOKEMANAGERCERT = "revokeManagerCert";

    public static final String FUNC_SETTLETRADE = "settleTrade";

    public static final String FUNC_SUBMITAUTHORISEDREQUEST = "submitAuthorisedRequest";

    public static final String FUNC_SUBMITREQUEST = "submitRequest";

    public static final String FUNC_SUPPORTSINTERFACE = "supportsInterface";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final String FUNC_SUBMITREQUESTARRAYEXTRA = "submitRequestArrayExtra";

    public static final String FUNC_GETREQUESTEXTRA = "getRequestExtra";

    public static final String FUNC_DECIDEREQUEST = "decideRequest";

    public static final String FUNC_SUBMITOFFER = "submitOffer";

    public static final String FUNC_SUBMITOFFERARRAYEXTRA = "submitOfferArrayExtra";

    public static final String FUNC_GETOFFEREXTRA = "getOfferExtra";

    public static final String FUNC_GETTYPE = "getType";

    public static final String FUNC_INTERLEDGERABORT = "interledgerAbort";

    public static final String FUNC_interledgerCommit = "interledgerCommit";

    public static final String FUNC_INTERLEDGERRECEIVE = "interledgerReceive";

    public static final String FUNC_WITHDRAW = "withdraw";

    public static final Event FUNCTIONSTATUS_EVENT = new Event("FunctionStatus", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
    ;

    public static final Event INTERLEDGEREVENTACCEPTED_EVENT = new Event("InterledgerEventAccepted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    public static final Event INTERLEDGEREVENTREJECTED_EVENT = new Event("InterledgerEventRejected", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    public static final Event INTERLEDGEREVENTSENDING_EVENT = new Event("InterledgerEventSending", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<DynamicBytes>() {}));
    ;

    public static final Event OFFERADDED_EVENT = new Event("OfferAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}));
    ;

    public static final Event OFFERCLAIMABLE_EVENT = new Event("OfferClaimable", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}));
    ;

    public static final Event OFFEREXTRAADDED_EVENT = new Event("OfferExtraAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    public static final Event OFFERFULFILLED_EVENT = new Event("OfferFulfilled", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<DynamicBytes>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event PAYMENTCASHEDOUT_EVENT = new Event("PaymentCashedOut", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event REQUESTADDED_EVENT = new Event("RequestAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event REQUESTCLAIMABLE_EVENT = new Event("RequestClaimable", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<DynamicArray<Uint256>>() {}));
    ;

    public static final Event REQUESTCLOSED_EVENT = new Event("RequestClosed", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    public static final Event REQUESTDECIDED_EVENT = new Event("RequestDecided", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<DynamicArray<Uint256>>() {}));
    ;

    public static final Event REQUESTEXTRAADDED_EVENT = new Event("RequestExtraAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    ;

    public static final Event TRADESETTLED_EVENT = new Event("TradeSettled", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected SMAUGMarketPlaceABI(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected SMAUGMarketPlaceABI(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected SMAUGMarketPlaceABI(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected SMAUGMarketPlaceABI(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<FunctionStatusEventResponse> getFunctionStatusEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(FUNCTIONSTATUS_EVENT, transactionReceipt);
        ArrayList<FunctionStatusEventResponse> responses = new ArrayList<FunctionStatusEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            FunctionStatusEventResponse typedResponse = new FunctionStatusEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.status = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<FunctionStatusEventResponse> functionStatusEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, FunctionStatusEventResponse>() {
            @Override
            public FunctionStatusEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(FUNCTIONSTATUS_EVENT, log);
                FunctionStatusEventResponse typedResponse = new FunctionStatusEventResponse();
                typedResponse.log = log;
                typedResponse.status = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<FunctionStatusEventResponse> functionStatusEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(FUNCTIONSTATUS_EVENT));
        return functionStatusEventFlowable(filter);
    }

    public List<InterledgerEventAcceptedEventResponse> getInterledgerEventAcceptedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(INTERLEDGEREVENTACCEPTED_EVENT, transactionReceipt);
        ArrayList<InterledgerEventAcceptedEventResponse> responses = new ArrayList<InterledgerEventAcceptedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            InterledgerEventAcceptedEventResponse typedResponse = new InterledgerEventAcceptedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.nonce = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<InterledgerEventAcceptedEventResponse> interledgerEventAcceptedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, InterledgerEventAcceptedEventResponse>() {
            @Override
            public InterledgerEventAcceptedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(INTERLEDGEREVENTACCEPTED_EVENT, log);
                InterledgerEventAcceptedEventResponse typedResponse = new InterledgerEventAcceptedEventResponse();
                typedResponse.log = log;
                typedResponse.nonce = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<InterledgerEventAcceptedEventResponse> interledgerEventAcceptedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(INTERLEDGEREVENTACCEPTED_EVENT));
        return interledgerEventAcceptedEventFlowable(filter);
    }

    public List<InterledgerEventRejectedEventResponse> getInterledgerEventRejectedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(INTERLEDGEREVENTREJECTED_EVENT, transactionReceipt);
        ArrayList<InterledgerEventRejectedEventResponse> responses = new ArrayList<InterledgerEventRejectedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            InterledgerEventRejectedEventResponse typedResponse = new InterledgerEventRejectedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.nonce = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<InterledgerEventRejectedEventResponse> interledgerEventRejectedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, InterledgerEventRejectedEventResponse>() {
            @Override
            public InterledgerEventRejectedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(INTERLEDGEREVENTREJECTED_EVENT, log);
                InterledgerEventRejectedEventResponse typedResponse = new InterledgerEventRejectedEventResponse();
                typedResponse.log = log;
                typedResponse.nonce = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<InterledgerEventRejectedEventResponse> interledgerEventRejectedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(INTERLEDGEREVENTREJECTED_EVENT));
        return interledgerEventRejectedEventFlowable(filter);
    }

    public List<InterledgerEventSendingEventResponse> getInterledgerEventSendingEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(INTERLEDGEREVENTSENDING_EVENT, transactionReceipt);
        ArrayList<InterledgerEventSendingEventResponse> responses = new ArrayList<InterledgerEventSendingEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            InterledgerEventSendingEventResponse typedResponse = new InterledgerEventSendingEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.data = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<InterledgerEventSendingEventResponse> interledgerEventSendingEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, InterledgerEventSendingEventResponse>() {
            @Override
            public InterledgerEventSendingEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(INTERLEDGEREVENTSENDING_EVENT, log);
                InterledgerEventSendingEventResponse typedResponse = new InterledgerEventSendingEventResponse();
                typedResponse.log = log;
                typedResponse.id = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.data = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<InterledgerEventSendingEventResponse> interledgerEventSendingEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(INTERLEDGEREVENTSENDING_EVENT));
        return interledgerEventSendingEventFlowable(filter);
    }

    public List<OfferAddedEventResponse> getOfferAddedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OFFERADDED_EVENT, transactionReceipt);
        ArrayList<OfferAddedEventResponse> responses = new ArrayList<OfferAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OfferAddedEventResponse typedResponse = new OfferAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.offerID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.requestID = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.offerMaker = (String) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<OfferAddedEventResponse> offerAddedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, OfferAddedEventResponse>() {
            @Override
            public OfferAddedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OFFERADDED_EVENT, log);
                OfferAddedEventResponse typedResponse = new OfferAddedEventResponse();
                typedResponse.log = log;
                typedResponse.offerID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.requestID = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.offerMaker = (String) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<OfferAddedEventResponse> offerAddedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OFFERADDED_EVENT));
        return offerAddedEventFlowable(filter);
    }

    public List<OfferClaimableEventResponse> getOfferClaimableEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OFFERCLAIMABLE_EVENT, transactionReceipt);
        ArrayList<OfferClaimableEventResponse> responses = new ArrayList<OfferClaimableEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OfferClaimableEventResponse typedResponse = new OfferClaimableEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.offerID = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<OfferClaimableEventResponse> offerClaimableEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, OfferClaimableEventResponse>() {
            @Override
            public OfferClaimableEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OFFERCLAIMABLE_EVENT, log);
                OfferClaimableEventResponse typedResponse = new OfferClaimableEventResponse();
                typedResponse.log = log;
                typedResponse.offerID = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<OfferClaimableEventResponse> offerClaimableEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OFFERCLAIMABLE_EVENT));
        return offerClaimableEventFlowable(filter);
    }

    public List<OfferExtraAddedEventResponse> getOfferExtraAddedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OFFEREXTRAADDED_EVENT, transactionReceipt);
        ArrayList<OfferExtraAddedEventResponse> responses = new ArrayList<OfferExtraAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OfferExtraAddedEventResponse typedResponse = new OfferExtraAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.offerID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<OfferExtraAddedEventResponse> offerExtraAddedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, OfferExtraAddedEventResponse>() {
            @Override
            public OfferExtraAddedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OFFEREXTRAADDED_EVENT, log);
                OfferExtraAddedEventResponse typedResponse = new OfferExtraAddedEventResponse();
                typedResponse.log = log;
                typedResponse.offerID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<OfferExtraAddedEventResponse> offerExtraAddedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OFFEREXTRAADDED_EVENT));
        return offerExtraAddedEventFlowable(filter);
    }

    public List<OfferFulfilledEventResponse> getOfferFulfilledEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OFFERFULFILLED_EVENT, transactionReceipt);
        ArrayList<OfferFulfilledEventResponse> responses = new ArrayList<OfferFulfilledEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OfferFulfilledEventResponse typedResponse = new OfferFulfilledEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.offerID = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.token = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<OfferFulfilledEventResponse> offerFulfilledEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, OfferFulfilledEventResponse>() {
            @Override
            public OfferFulfilledEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OFFERFULFILLED_EVENT, log);
                OfferFulfilledEventResponse typedResponse = new OfferFulfilledEventResponse();
                typedResponse.log = log;
                typedResponse.offerID = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.token = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<OfferFulfilledEventResponse> offerFulfilledEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OFFERFULFILLED_EVENT));
        return offerFulfilledEventFlowable(filter);
    }

    public List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, OwnershipTransferredEventResponse>() {
            @Override
            public OwnershipTransferredEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
                OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
                typedResponse.log = log;
                typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public List<PaymentCashedOutEventResponse> getPaymentCashedOutEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(PAYMENTCASHEDOUT_EVENT, transactionReceipt);
        ArrayList<PaymentCashedOutEventResponse> responses = new ArrayList<PaymentCashedOutEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PaymentCashedOutEventResponse typedResponse = new PaymentCashedOutEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestID = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.offerID = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<PaymentCashedOutEventResponse> paymentCashedOutEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, PaymentCashedOutEventResponse>() {
            @Override
            public PaymentCashedOutEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(PAYMENTCASHEDOUT_EVENT, log);
                PaymentCashedOutEventResponse typedResponse = new PaymentCashedOutEventResponse();
                typedResponse.log = log;
                typedResponse.requestID = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.offerID = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<PaymentCashedOutEventResponse> paymentCashedOutEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PAYMENTCASHEDOUT_EVENT));
        return paymentCashedOutEventFlowable(filter);
    }

    public List<RequestAddedEventResponse> getRequestAddedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(REQUESTADDED_EVENT, transactionReceipt);
        ArrayList<RequestAddedEventResponse> responses = new ArrayList<RequestAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RequestAddedEventResponse typedResponse = new RequestAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.deadline = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<RequestAddedEventResponse> requestAddedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, RequestAddedEventResponse>() {
            @Override
            public RequestAddedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(REQUESTADDED_EVENT, log);
                RequestAddedEventResponse typedResponse = new RequestAddedEventResponse();
                typedResponse.log = log;
                typedResponse.requestID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.deadline = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<RequestAddedEventResponse> requestAddedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(REQUESTADDED_EVENT));
        return requestAddedEventFlowable(filter);
    }

    public List<RequestClaimableEventResponse> getRequestClaimableEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(REQUESTCLAIMABLE_EVENT, transactionReceipt);
        ArrayList<RequestClaimableEventResponse> responses = new ArrayList<RequestClaimableEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RequestClaimableEventResponse typedResponse = new RequestClaimableEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestID = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.offerIDs = (List<BigInteger>) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<RequestClaimableEventResponse> requestClaimableEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, RequestClaimableEventResponse>() {
            @Override
            public RequestClaimableEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(REQUESTCLAIMABLE_EVENT, log);
                RequestClaimableEventResponse typedResponse = new RequestClaimableEventResponse();
                typedResponse.log = log;
                typedResponse.requestID = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.offerIDs = (List<BigInteger>) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<RequestClaimableEventResponse> requestClaimableEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(REQUESTCLAIMABLE_EVENT));
        return requestClaimableEventFlowable(filter);
    }

    public List<RequestClosedEventResponse> getRequestClosedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(REQUESTCLOSED_EVENT, transactionReceipt);
        ArrayList<RequestClosedEventResponse> responses = new ArrayList<RequestClosedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RequestClosedEventResponse typedResponse = new RequestClosedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<RequestClosedEventResponse> requestClosedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, RequestClosedEventResponse>() {
            @Override
            public RequestClosedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(REQUESTCLOSED_EVENT, log);
                RequestClosedEventResponse typedResponse = new RequestClosedEventResponse();
                typedResponse.log = log;
                typedResponse.requestID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<RequestClosedEventResponse> requestClosedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(REQUESTCLOSED_EVENT));
        return requestClosedEventFlowable(filter);
    }

    public List<RequestDecidedEventResponse> getRequestDecidedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(REQUESTDECIDED_EVENT, transactionReceipt);
        ArrayList<RequestDecidedEventResponse> responses = new ArrayList<RequestDecidedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RequestDecidedEventResponse typedResponse = new RequestDecidedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.winningOffersIDs = (List<BigInteger>) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<RequestDecidedEventResponse> requestDecidedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, RequestDecidedEventResponse>() {
            @Override
            public RequestDecidedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(REQUESTDECIDED_EVENT, log);
                RequestDecidedEventResponse typedResponse = new RequestDecidedEventResponse();
                typedResponse.log = log;
                typedResponse.requestID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.winningOffersIDs = (List<BigInteger>) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<RequestDecidedEventResponse> requestDecidedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(REQUESTDECIDED_EVENT));
        return requestDecidedEventFlowable(filter);
    }

    public List<RequestExtraAddedEventResponse> getRequestExtraAddedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(REQUESTEXTRAADDED_EVENT, transactionReceipt);
        ArrayList<RequestExtraAddedEventResponse> responses = new ArrayList<RequestExtraAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            RequestExtraAddedEventResponse typedResponse = new RequestExtraAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<RequestExtraAddedEventResponse> requestExtraAddedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, RequestExtraAddedEventResponse>() {
            @Override
            public RequestExtraAddedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(REQUESTEXTRAADDED_EVENT, log);
                RequestExtraAddedEventResponse typedResponse = new RequestExtraAddedEventResponse();
                typedResponse.log = log;
                typedResponse.requestID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<RequestExtraAddedEventResponse> requestExtraAddedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(REQUESTEXTRAADDED_EVENT));
        return requestExtraAddedEventFlowable(filter);
    }

    public List<TradeSettledEventResponse> getTradeSettledEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(TRADESETTLED_EVENT, transactionReceipt);
        ArrayList<TradeSettledEventResponse> responses = new ArrayList<TradeSettledEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TradeSettledEventResponse typedResponse = new TradeSettledEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.requestID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.offerID = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<TradeSettledEventResponse> tradeSettledEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, TradeSettledEventResponse>() {
            @Override
            public TradeSettledEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(TRADESETTLED_EVENT, log);
                TradeSettledEventResponse typedResponse = new TradeSettledEventResponse();
                typedResponse.log = log;
                typedResponse.requestID = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.offerID = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<TradeSettledEventResponse> tradeSettledEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRADESETTLED_EVENT));
        return tradeSettledEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> addManager(String managerAddress) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ADDMANAGER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, managerAddress)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> changeOwner(String addr) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_CHANGEOWNER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, addr)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> closeRequest(BigInteger requestIdentifier) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_CLOSEREQUEST, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(requestIdentifier)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> deleteRequest(BigInteger requestIdentifier) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_DELETEREQUEST, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(requestIdentifier)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple2<BigInteger, List<BigInteger>>> getClosedRequestIdentifiers() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETCLOSEDREQUESTIDENTIFIERS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteFunctionCall<Tuple2<BigInteger, List<BigInteger>>>(function,
                new Callable<Tuple2<BigInteger, List<BigInteger>>>() {
                    @Override
                    public Tuple2<BigInteger, List<BigInteger>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, List<BigInteger>>(
                                (BigInteger) results.get(0).getValue(), 
                                convertToNative((List<Uint256>) results.get(1).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<Tuple2<BigInteger, String>> getMarketInformation() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETMARKETINFORMATION, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Address>() {}));
        return new RemoteFunctionCall<Tuple2<BigInteger, String>>(function,
                new Callable<Tuple2<BigInteger, String>>() {
                    @Override
                    public Tuple2<BigInteger, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, String>(
                                (BigInteger) results.get(0).getValue(), 
                                (String) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Tuple4<BigInteger, BigInteger, String, BigInteger>> getOffer(BigInteger offerIdentifier) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETOFFER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(offerIdentifier)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple4<BigInteger, BigInteger, String, BigInteger>>(function,
                new Callable<Tuple4<BigInteger, BigInteger, String, BigInteger>>() {
                    @Override
                    public Tuple4<BigInteger, BigInteger, String, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<BigInteger, BigInteger, String, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Tuple2<BigInteger, List<BigInteger>>> getOpenRequestIdentifiers() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETOPENREQUESTIDENTIFIERS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteFunctionCall<Tuple2<BigInteger, List<BigInteger>>>(function,
                new Callable<Tuple2<BigInteger, List<BigInteger>>>() {
                    @Override
                    public Tuple2<BigInteger, List<BigInteger>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, List<BigInteger>>(
                                (BigInteger) results.get(0).getValue(), 
                                convertToNative((List<Uint256>) results.get(1).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<Tuple4<BigInteger, BigInteger, BigInteger, String>> getRequest(BigInteger requestIdentifier) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETREQUEST, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(requestIdentifier)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}));
        return new RemoteFunctionCall<Tuple4<BigInteger, BigInteger, BigInteger, String>>(function,
                new Callable<Tuple4<BigInteger, BigInteger, BigInteger, String>>() {
                    @Override
                    public Tuple4<BigInteger, BigInteger, BigInteger, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<BigInteger, BigInteger, BigInteger, String>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (String) results.get(3).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Tuple2<BigInteger, List<BigInteger>>> getRequestDecision(BigInteger requestIdentifier) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETREQUESTDECISION, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(requestIdentifier)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteFunctionCall<Tuple2<BigInteger, List<BigInteger>>>(function,
                new Callable<Tuple2<BigInteger, List<BigInteger>>>() {
                    @Override
                    public Tuple2<BigInteger, List<BigInteger>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, List<BigInteger>>(
                                (BigInteger) results.get(0).getValue(), 
                                convertToNative((List<Uint256>) results.get(1).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<Tuple2<BigInteger, BigInteger>> getRequestDecisionTime(BigInteger requestIdentifier) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETREQUESTDECISIONTIME, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(requestIdentifier)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple2<BigInteger, BigInteger>>(function,
                new Callable<Tuple2<BigInteger, BigInteger>>() {
                    @Override
                    public Tuple2<BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Tuple2<BigInteger, List<BigInteger>>> getRequestOfferIDs(BigInteger requestIdentifier) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETREQUESTOFFERIDS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(requestIdentifier)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<DynamicArray<Uint256>>() {}));
        return new RemoteFunctionCall<Tuple2<BigInteger, List<BigInteger>>>(function,
                new Callable<Tuple2<BigInteger, List<BigInteger>>>() {
                    @Override
                    public Tuple2<BigInteger, List<BigInteger>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, List<BigInteger>>(
                                (BigInteger) results.get(0).getValue(), 
                                convertToNative((List<Uint256>) results.get(1).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<Tuple2<BigInteger, Boolean>> isOfferDefined(BigInteger offerIdentifier) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISOFFERDEFINED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(offerIdentifier)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Bool>() {}));
        return new RemoteFunctionCall<Tuple2<BigInteger, Boolean>>(function,
                new Callable<Tuple2<BigInteger, Boolean>>() {
                    @Override
                    public Tuple2<BigInteger, Boolean> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, Boolean>(
                                (BigInteger) results.get(0).getValue(), 
                                (Boolean) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Boolean> isOwner() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISOWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<Tuple2<BigInteger, Boolean>> isRequestDecided(BigInteger requestIdentifier) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISREQUESTDECIDED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(requestIdentifier)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Bool>() {}));
        return new RemoteFunctionCall<Tuple2<BigInteger, Boolean>>(function,
                new Callable<Tuple2<BigInteger, Boolean>>() {
                    @Override
                    public Tuple2<BigInteger, Boolean> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, Boolean>(
                                (BigInteger) results.get(0).getValue(), 
                                (Boolean) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Tuple2<BigInteger, Boolean>> isRequestDefined(BigInteger requestIdentifier) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISREQUESTDEFINED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(requestIdentifier)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Bool>() {}));
        return new RemoteFunctionCall<Tuple2<BigInteger, Boolean>>(function,
                new Callable<Tuple2<BigInteger, Boolean>>() {
                    @Override
                    public Tuple2<BigInteger, Boolean> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, Boolean>(
                                (BigInteger) results.get(0).getValue(), 
                                (Boolean) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<String> owner() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> resetAccessTokens() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_RESETACCESSTOKENS, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> revokeManagerCert(String managerAddress) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_REVOKEMANAGERCERT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, managerAddress)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> settleTrade(BigInteger requestID, BigInteger offerID) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SETTLETRADE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(requestID), 
                new org.web3j.abi.datatypes.generated.Uint256(offerID)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> submitAuthorisedRequest(byte[] tokenDigest, byte[] signature, byte[] nonce, BigInteger deadline) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SUBMITAUTHORISEDREQUEST, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tokenDigest), 
                new org.web3j.abi.datatypes.DynamicBytes(signature), 
                new org.web3j.abi.datatypes.generated.Bytes32(nonce), 
                new org.web3j.abi.datatypes.generated.Uint256(deadline)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> submitRequest(BigInteger deadline) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SUBMITREQUEST, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(deadline)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> supportsInterface(byte[] interfaceId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_SUPPORTSINTERFACE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes4(interfaceId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> submitRequestArrayExtra(BigInteger requestID, List<BigInteger> extra) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SUBMITREQUESTARRAYEXTRA, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(requestID), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                        org.web3j.abi.datatypes.generated.Uint256.class,
                        org.web3j.abi.Utils.typeMap(extra, org.web3j.abi.datatypes.generated.Uint256.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple6<BigInteger, BigInteger, BigInteger, BigInteger, List<BigInteger>, BigInteger>> getRequestExtra(BigInteger requestIdentifier) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETREQUESTEXTRA, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(requestIdentifier)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<DynamicArray<Uint256>>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple6<BigInteger, BigInteger, BigInteger, BigInteger, List<BigInteger>, BigInteger>>(function,
                new Callable<Tuple6<BigInteger, BigInteger, BigInteger, BigInteger, List<BigInteger>, BigInteger>>() {
                    @Override
                    public Tuple6<BigInteger, BigInteger, BigInteger, BigInteger, List<BigInteger>, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple6<BigInteger, BigInteger, BigInteger, BigInteger, List<BigInteger>, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                convertToNative((List<Uint256>) results.get(4).getValue()), 
                                (BigInteger) results.get(5).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> decideRequest(BigInteger requestIdentifier, List<BigInteger> acceptedOfferIDs) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_DECIDEREQUEST, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(requestIdentifier), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                        org.web3j.abi.datatypes.generated.Uint256.class,
                        org.web3j.abi.Utils.typeMap(acceptedOfferIDs, org.web3j.abi.datatypes.generated.Uint256.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> submitOffer(BigInteger requestID) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SUBMITOFFER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(requestID)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> submitOfferArrayExtra(BigInteger offerID, List<BigInteger> extra, BigInteger weiValue) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_SUBMITOFFERARRAYEXTRA, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(offerID), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Uint256>(
                        org.web3j.abi.datatypes.generated.Uint256.class,
                        org.web3j.abi.Utils.typeMap(extra, org.web3j.abi.datatypes.generated.Uint256.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<Tuple7<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>> getOfferExtra(BigInteger offerIdentifier) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETOFFEREXTRA, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(offerIdentifier)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple7<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple7<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple7<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple7<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue(), 
                                (BigInteger) results.get(5).getValue(), 
                                (BigInteger) results.get(6).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Tuple2<BigInteger, String>> getType() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETTYPE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}, new TypeReference<Utf8String>() {}));
        return new RemoteFunctionCall<Tuple2<BigInteger, String>>(function,
                new Callable<Tuple2<BigInteger, String>>() {
                    @Override
                    public Tuple2<BigInteger, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, String>(
                                (BigInteger) results.get(0).getValue(), 
                                (String) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> interledgerAbort(BigInteger id, BigInteger reason) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_INTERLEDGERABORT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id), 
                new org.web3j.abi.datatypes.generated.Uint256(reason)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> interledgerCommit(BigInteger id) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_interledgerCommit, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> interledgerCommit(BigInteger id, byte[] data) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_interledgerCommit, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(id), 
                new org.web3j.abi.datatypes.DynamicBytes(data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> interledgerReceive(BigInteger nonce, byte[] data) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_INTERLEDGERRECEIVE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(nonce), 
                new org.web3j.abi.datatypes.DynamicBytes(data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> withdraw(BigInteger offerID) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_WITHDRAW, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(offerID)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static SMAUGMarketPlaceABI load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new SMAUGMarketPlaceABI(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static SMAUGMarketPlaceABI load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new SMAUGMarketPlaceABI(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static SMAUGMarketPlaceABI load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new SMAUGMarketPlaceABI(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static SMAUGMarketPlaceABI load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new SMAUGMarketPlaceABI(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class FunctionStatusEventResponse extends BaseEventResponse {
        public BigInteger status;
    }

    public static class InterledgerEventAcceptedEventResponse extends BaseEventResponse {
        public BigInteger nonce;
    }

    public static class InterledgerEventRejectedEventResponse extends BaseEventResponse {
        public BigInteger nonce;
    }

    public static class InterledgerEventSendingEventResponse extends BaseEventResponse {
        public BigInteger id;

        public byte[] data;
    }

    public static class OfferAddedEventResponse extends BaseEventResponse {
        public BigInteger offerID;

        public BigInteger requestID;

        public String offerMaker;
    }

    public static class OfferClaimableEventResponse extends BaseEventResponse {
        public BigInteger offerID;
    }

    public static class OfferExtraAddedEventResponse extends BaseEventResponse {
        public BigInteger offerID;
    }

    public static class OfferFulfilledEventResponse extends BaseEventResponse {
        public BigInteger offerID;

        public byte[] token;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class PaymentCashedOutEventResponse extends BaseEventResponse {
        public BigInteger requestID;

        public BigInteger offerID;

        public BigInteger amount;
    }

    public static class RequestAddedEventResponse extends BaseEventResponse {
        public BigInteger requestID;

        public BigInteger deadline;
    }

    public static class RequestClaimableEventResponse extends BaseEventResponse {
        public BigInteger requestID;

        public List<BigInteger> offerIDs;
    }

    public static class RequestClosedEventResponse extends BaseEventResponse {
        public BigInteger requestID;
    }

    public static class RequestDecidedEventResponse extends BaseEventResponse {
        public BigInteger requestID;

        public List<BigInteger> winningOffersIDs;
    }

    public static class RequestExtraAddedEventResponse extends BaseEventResponse {
        public BigInteger requestID;
    }

    public static class TradeSettledEventResponse extends BaseEventResponse {
        public BigInteger requestID;

        public BigInteger offerID;
    }
}
