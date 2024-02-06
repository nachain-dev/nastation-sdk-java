package org.nastation.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.nachain.core.base.Amount;
import org.nachain.core.base.Unit;
import org.nachain.core.chain.structure.instance.CoreInstanceEnum;
import org.nachain.core.chain.transaction.Tx;
import org.nachain.core.chain.transaction.TxGasType;
import org.nachain.core.chain.transaction.TxService;
import org.nachain.core.chain.transaction.TxType;
import org.nachain.core.chain.transaction.context.TxContextService;
import org.nachain.core.crypto.Key;
import org.nachain.core.crypto.bip39.Language;
import org.nachain.core.mailbox.Mail;
import org.nachain.core.mailbox.MailType;
import org.nachain.core.token.CoreTokenEnum;
import org.nachain.core.wallet.Keystore;
import org.nachain.core.wallet.WalletUtils;
import org.nachain.core.wallet.walletskill.NirvanaWalletSkill;
import org.nastation.demo.util.HttpUtil;
import org.nastation.demo.util.JsonUtil;
import org.nastation.demo.util.NumberUtil;
import org.nastation.demo.vo.HttpResult;
import org.nastation.demo.vo.UsedTokenBalanceDetail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class GatRawTxBroadcast {

    /* node cluster gateway */
    public static String nodeClusterUrl = "https://node.nachain.org";

    public static long nacTokenId = CoreTokenEnum.NAC.id;
    public static long nacInstanceId = CoreInstanceEnum.NAC.id;

    public static long gatTokenId = 16;
    public static long gatInstanceId = 20;

    public static String fromWalletWords = ""; // <your_mnemonic_words: apple embrace pretty..... >
    public static String toAddress = "";// <your_target_address>

    public static void main(String[] args) throws Exception {

        /*
            Steps description:
            1. Restore the wallet of the sender
            2. Confirm the target address and transfer amount
            3. Check in advance whether the sum of the transfer amount and the gas fee of the transfer party is sufficient
            4. Construct the transaction
            5. Broadcast tx
        * */

        /* 1. Restore from wallet */
        Keystore keystore = WalletUtils.generate(Language.ENGLISH, fromWalletWords, null, 0);
        byte[] privateKey = keystore.getPrivateKey();

        Key fromKey = null;
        try {
            fromKey = new Key(privateKey);
            fromKey.init(new NirvanaWalletSkill());
        } catch (InvalidKeySpecException e) {
            log.error("Wallet restore error");
        }
        if (fromKey == null) {
            return;
        }

        /* 2. Target address receive amount */
        double toAmount = 10;
        BigInteger amountBigInt = Amount.of(BigDecimal.valueOf(toAmount), Unit.NAC).toBigInteger();

        // from wallet address
        String fromAddress = fromKey.toWalletAddress();
        log.info("fromAddress: "+fromAddress);

        BigInteger gasFee = get_gasFee(gatInstanceId);
        if (gasFee.longValue() <= 0) {
            log.error("Gas fee error");
            return;
        }

        /* 3. Check: if (only gas: $NAC) enough? */
        double nacGasFeeDouble = NumberUtil.bigIntToNacDouble(gasFee);
        double total = nacGasFeeDouble;
        HttpResult httpResult = checkTokenAmountEnough(fromAddress, gatInstanceId, nacTokenId, total);
        if (!httpResult.getFlag()) {
            log.error("Gas not enough: "+httpResult.getMessage());
            //return;
        }

        //tx height of from wallet account
        long txHeight = getAccountTxHeight(gatInstanceId, fromAddress, gatTokenId) + 1;

        /* 4. Build tx object */

        String remark = "";
        Tx sendTx = TxService.newTx(
                TxType.TRANSFER,
                gatInstanceId, gatTokenId,
                fromAddress, toAddress, amountBigInt,
                gasFee,
                TxGasType.NAC.value,
                txHeight,
                TxContextService.newTransferContext(gatInstanceId),
                remark, 0, fromKey);

        Mail mail = Mail.newMail(MailType.MSG_SEND_TX, sendTx.toJson());
        String txHash = sendTx.getHash();
        String json = mail.toJson();

        /*5. Broadcast tx*/

        //broadcast tx, if flag=false then broadcast fail
        boolean flag = broadcastTx(json, gatInstanceId);

        StringBuilder builder = new StringBuilder();
        builder.append("\n\r");
        builder.append("Tx info: ").append("\n\r");
        builder.append("from = ").append(fromAddress).append("\n\r");
        builder.append("to = ").append(toAddress).append("\n\r");
        builder.append("amount = ").append(toAmount).append("\n\r");
        builder.append("txHash = ").append(txHash).append("\n\r");
        builder.append("json = ").append(json).append("\n\r");
        builder.append("flag = ").append(flag).append("\n\r");

        log.info(builder.toString());

        //TIPS:
        //You can use the timer to check whether the transaction hash is confirmed or successful
    }



    /*----------------
    * get gas fee
    * ----------------*/

    public static String buildGetGasUrlByNodeCluster(long instanceId) {
        return nodeClusterUrl + "/api/v2/mergeApi?module=tx&method=getGas&instance=" + instanceId;
    }

    public static BigInteger get_gasFee(long instanceId) {
        String url = buildGetGasUrlByNodeCluster(instanceId);
        try {
            Connection.Response executeResult = HttpUtil.get(url).execute();
            String json = executeResult.body();

            HttpResult result = JsonUtil.parseObjectByOm(json, HttpResult.class);
            return new BigInteger(String.valueOf(result.getData()));

        } catch (Exception e) {
            log.error("get_gasFee error : " + e.getMessage(), e);
        }

        return BigInteger.ZERO;
    }


    /*----------------
     * get account tx height
     * ----------------*/

    public static String buildGetAccountTxHeightUrlByNodeCluster() {
        return nodeClusterUrl + "/api/v2/mergeApi?module=account&method=getTxHeight";
    }

    public static long getAccountTxHeight(long instance, String address, long token) {
        String url = buildGetAccountTxHeightUrlByNodeCluster();
        try {
            Connection.Response executeResult = HttpUtil.get(url)
                    .data("address", address)
                    .data("token", String.valueOf(token))
                    .data("instance", String.valueOf(instance))
                    .execute();

            String resultJson = executeResult.body();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readValue(resultJson, JsonNode.class);

            JsonNode dataJsonNode = jsonNode.get("data");
            JsonNode flagJsonNode = jsonNode.get("flag");
            boolean flag = flagJsonNode.asBoolean();
            if (flag) {
                return dataJsonNode.asLong();
            }
        } catch (Exception e) {
            log.error("getAccountTxHeight error:", e);
        }

        return 0L;
    }

    /*----------------
     * Check if an address is greater than a conditional value on a specific instance and a token
     * ----------------*/

    public static HttpResult checkTokenAmountEnough(String address, long instanceId, long token, double condition) {

        Set<Map.Entry<Long, BigInteger>> entries = Sets.newHashSet();

        UsedTokenBalanceDetail usedTokenBalanceDetail = request_account_getUsedTokenBalanceDetail(address, instanceId);

        if (usedTokenBalanceDetail != null && usedTokenBalanceDetail.getTokenBalanceMap() != null) {
            entries = usedTokenBalanceDetail.getTokenBalanceMap().entrySet();
        }

        Optional<Map.Entry<Long, BigInteger>> hasTokenEnough = entries.stream().filter(k -> k.getKey().longValue() == token).findFirst();

        String msgTmpl = "Please make sure that the current wallet has enough %s under the %s instance.";
        String tokenSymbol = Arrays.stream(CoreTokenEnum.values()).filter(e -> e.id == token).findFirst().get().name;
        String instanceSymbol = Arrays.stream(CoreInstanceEnum.values()).filter(e -> e.id == instanceId).findFirst().get().name;
        HttpResult result = HttpResult.me().asFalse();

        if (!hasTokenEnough.isPresent()) {
            return result.msg(String.format(msgTmpl, tokenSymbol, instanceSymbol));
        }

        Map.Entry<Long, BigInteger> longBigIntegerEntry = hasTokenEnough.get();

        BigInteger value = longBigIntegerEntry.getValue();

        BigInteger conditionBigInt = NumberUtil.nacDoubleToBigInt(condition);

        boolean flag = value.longValue() >= conditionBigInt.longValue();

        if (!flag) {
            return result.msg(String.format(msgTmpl, tokenSymbol, instanceSymbol));
        }

        return result.setFlag(flag);

    }

    public static UsedTokenBalanceDetail request_account_getUsedTokenBalanceDetail(String address, long instanceId) {

        String url = nodeClusterUrl + "/api/v2/mergeApi?module=account&method=getUsedTokenBalanceDetail&walletAddress=" + address + "&instance=" + instanceId;
        UsedTokenBalanceDetail usedTokenBalanceDetail = null;
        try {
            Connection.Response executeResult = HttpUtil.get(url).execute();
            String json = executeResult.body();

            ObjectMapper objectMapper = JsonUtil.om();
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.get("data");
            usedTokenBalanceDetail = objectMapper.treeToValue(data, UsedTokenBalanceDetail.class);

        } catch (Exception e) {
            log.error("request_account_getUsedTokenBalanceDetail error : " + e.getMessage() + ", URL = " + url, e);
        }

        return usedTokenBalanceDetail;
    }


    public static String buildGetBroadcastTxUrlByNodeCluster() {
        return nodeClusterUrl + "/broadcast/mail/tx/v2";
    }

    public static boolean broadcastTx(String mailJson, long instanceId) {

        boolean flag = false;
        String url = buildGetBroadcastTxUrlByNodeCluster();
        try {
            Connection.Response executeResult = HttpUtil.post(url)
                    .data("mail", mailJson)
                    .data("instance", String.valueOf(instanceId))
                    .execute();

            String resultJson = executeResult.body();
            flag = resultJson.contains("true");

        } catch (Exception e) {
            log.error("broadcast error:", e);
        }

        return flag;
    }

}
