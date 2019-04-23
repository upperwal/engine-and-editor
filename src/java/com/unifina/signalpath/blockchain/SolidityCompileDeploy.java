package com.unifina.signalpath.blockchain;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.unifina.signalpath.*;
import com.unifina.utils.MapTraversal;
import org.apache.log4j.Logger;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SolidityCompileDeploy extends ModuleWithUI implements Pullable<EthereumContract>, Serializable {

	private static final Logger log = Logger.getLogger(SolidityCompileDeploy.class);

	private final EthereumAccountParameter ethereumAccount = new EthereumAccountParameter(this, "ethAccount");
	private Output<EthereumContract> contractOutput = null;

	private String code = null;
	private EthereumContract contract = null;
	private DoubleParameter sendEtherParam = new DoubleParameter(this, "initial ETH", 0.0);

	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();
	private Web3j web3j;

	@Override
	public void init() {
		addInput(ethereumAccount);

		// TODO: document why this is needed. Parameter changes shouldn't trigger onConfiguration, but code changes should
		ethereumAccount.setUpdateOnChange(true);
	}

	/**
	 * Override to provide contract template that will be compiled when module is added to canvas
	 */
	public String getCodeTemplate() {
		return null;
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions.writeTo(options);

		config.put("code", code);
		if (contract != null) {
			config.put("contract", contract.toMap());
		}

		return config;
	}

	protected SolidityCompiler.Option[] getSolcOptions() {
		SolidityCompiler.Option[] opts = {SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN};
		return opts;
	}

	protected JsonObject compile(String solidity_code) {
		String result = null;
		String errors = null;
		try {
			SolidityCompiler.Result res = SolidityCompiler.compile(solidity_code.getBytes(StandardCharsets.UTF_8), true, getSolcOptions());
			if (res.isFailed()) {
				errors = res.errors;
			} else {
				result = res.output;
			}
		} catch (IOException e) {
			errors = e.getLocalizedMessage();
		}
		if (errors != null) {
			throw new RuntimeException("Error compiling contract: " + errors);
		}

		// TODO: in case of many contracts, pick the one with the longest bytecode (or find better heuristic for the "main" conrtact)
		Set<Map.Entry<String, JsonElement>> entries = new JsonParser().parse(result).getAsJsonObject().get("contracts").getAsJsonObject().entrySet();
		Map.Entry<String, JsonElement> e = entries.iterator().next();
		return e.getValue().getAsJsonObject();
	}

	protected String deploy(String bytecode, List<Object> args, BigInteger sendWei) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		BigInteger gasPrice = BigDecimal.valueOf(ethereumOptions.getGasPriceWei()).toBigInteger();
		BigInteger nonce = web3j.ethGetTransactionCount(ethereumAccount.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
		Credentials credentials = Credentials.create(ethereumAccount.getPrivateKey());

		// convert constructor arguments into web3j "Types" that contain both a Java class and the argument's value
		EthereumABI.Function constructor = contract.getABI().getConstructor();
		int argCount = Math.min(constructor.inputs.size(), args.size());
		List<org.web3j.abi.datatypes.Type> argTypes = new ArrayList<>(argCount);
		for (int i = 0; i < argCount; i++) {
			String argType = constructor.inputs.get(i).type;
			argTypes.add(Web3jHelper.instantiateType(argType, args.get(i)));
		}
		String encodedArgs = org.web3j.abi.FunctionEncoder.encodeConstructor(argTypes);
		String txBytes = bytecode + encodedArgs;
		RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, getGasLimit(), null, sendWei, txBytes);
		byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
		String hexValue = Numeric.toHexString(signedMessage);

		/* TODO: async deploy not implemented. Requires ability to send the resulting address to the editor UI during edit time.
		CompletableFuture<EthSendTransaction> cf = web3j.ethSendRawTransaction(hexValue).sendAsync();
		cf.thenAccept(new Consumer<EthSendTransaction>() {
			@Override
			public void accept(EthSendTransaction tx) {
			}
		});
		*/
		// synchronous deploy:
		EthSendTransaction tx = web3j.ethSendRawTransaction(hexValue).send();
		String txhash = tx.getTransactionHash();
		log.debug("TX response: " + txhash);
		String address = web3j.ethGetTransactionReceipt(txhash).send().getResult().getContractAddress();
		return address;
	}

	protected BigInteger getGasLimit() {
		return BigInteger.valueOf(6000000l);
	}

	protected Web3j getWeb3j() {
		return Web3j.build(new HttpService(ethereumOptions.getRpcUrl()));
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		// the button to send "deploy" message shouldn't be visible after deployment
		// onConfiguration may still be triggered by parameter changes
		if (contract != null && contract.isDeployed()) {
			if (config.containsKey("compile")) {
				throw new RuntimeException("Already deployed, changes ignored. Please create a new module.");
			}
			return;
		}

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);
		web3j = getWeb3j();
		boolean compileRequested = config.containsKey("compile");
		boolean deployRequested = config.containsKey("deploy");

		// old contract, previously compiled
		if (config.containsKey("contract")) {
			contract = EthereumContract.fromMap(MapTraversal.getMap(config, "contract"));
		}

		JsonObject compilationResult = null;
		if (compileRequested || deployRequested) {
			Object codeFromEditor = config.get("code");
			if (codeFromEditor == null) {
				code = getCodeTemplate();            // initially, get template from inheriting class e.g. PayByUse
			} else {
				code = codeFromEditor.toString();
			}

			boolean hasCode = code != null && !code.trim().isEmpty();
			boolean hasDeployer = ethereumAccount.getAddress() != null;
			if (hasCode && hasDeployer) {
				compilationResult = compile(code);
				contract = new EthereumContract(null, new EthereumABI(new JsonParser().parse(compilationResult.get("abi").getAsString()).getAsJsonArray()));
			}
		}

		if (contract != null) {
			if (deployRequested && compilationResult != null) {
				// transform Streamr params into list of constructor arguments
				Stack<Object> args = new Stack<>();
				List<Map> params = (List) config.get("params");
				EthereumABI.Function constructor = contract.getABI().getConstructor();
				BigInteger sendWei = BigInteger.ZERO;
				if (constructor != null) {
					// skip first parameter (ethAccount, not constructor parameter)
					for (Map param : params.subList(1, params.size())) {
						args.push(param.get("value"));
					}
					// for payable constructors, sendEtherParam is added in params after the ordinary function arguments
					// value can't be read from sendEtherParam.getValue because it's not added to the module until createParameters is called (so it exists in config but not in module object)
					if (constructor.payable) {
						BigDecimal sendEtherParamValue = new BigDecimal(args.pop().toString());
						sendWei = sendEtherParamValue.multiply(BigDecimal.TEN.pow(18)).toBigInteger();
					}
				}
				String bytecode = compilationResult.get("bin").getAsString();
				try {
					String address = deploy(bytecode, args, sendWei);
					contract = new EthereumContract(address, contract.getABI());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			createContractOutput();
			createParameters(contract.getABI());
		}
	}

	private void createContractOutput() {
		contractOutput = new EthereumContractOutput(this, "contract");
		addOutput(contractOutput);
	}

	private void createParameters(EthereumABI abi) {
		EthereumABI.Function constructor = abi.getConstructor();
		if (constructor != null) {
			for (EthereumABI.Slot input : constructor.inputs) {
				String name = input.name.replace("_", " ");
				Parameter p = EthereumToStreamrTypes.asParameter(input.type, name, this);
				p.setCanConnect(false);
				p.setCanToggleDrivingInput(false);
				addInput(p);
			}
			if (constructor.payable) {
				addInput(sendEtherParam);
			}
		}
	}

	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {

	}

	@Override
	public EthereumContract pullValue(Output output) {
		return contract;
	}

	@Override
	public void connectionsReady() {
		super.connectionsReady();
		if (contract != null) {
			contractOutput.send(contract);
		}
	}

}