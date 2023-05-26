package org.nastation.demo;

import com.fasterxml.jackson.databind.JsonNode;
import org.checkerframework.checker.guieffect.qual.UI;
import org.jsoup.Connection;
import org.nachain.core.base.Amount;
import org.nachain.core.base.Unit;
import org.nachain.core.chain.structure.instance.CoreInstanceEnum;
import org.nachain.core.token.CoreTokenEnum;
import org.nastation.demo.util.HttpUtil;
import org.nastation.demo.util.JsonUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

/**
 * The example shows how to request NaStation api
 * <p>
 * Before http requesting, please make sure you have launched the desktop version of NaStation
 * https://www.nachain.org/app (For Desktop)
 *
 * Or you are already running the linux version of the NaStation wallet node
 * <p>
 * If you are running on a server, please ensure that access is authorized through middleware such as nginx
 * NaStation default port : 20902
 *
 *
 * @author John | Nirvana Core
 * @since 12/06/2021
 */
public class NaStationRestApi {

    //NaStation default port : 20902
    static String URL = "http://localhost:20902/station/api";

    static long nacTokenId = CoreTokenEnum.NAC.id;

    static long nacInstanceId = CoreInstanceEnum.NAC.id;


    /**
     * Get gas fee
     * The gas fee of different instances is not the same
     * @throws Exception
     */
    private static void request_gas_fee() throws Exception {

        Connection.Response execute = HttpUtil.get(URL + "/gas/fee")
                .data("instanceId", String.valueOf(nacInstanceId))
                .execute();

        String json = execute.body();
        System.out.println(JsonUtil.prettyByGson(json));

    }

    /**
     * Tx broadcast
     *
     * @throws Exception
     */
    private static void request_tx_broadcast() throws Exception {

        String rawJson = "YOUR_TX_RAW_JSON";

        Connection.Response execute = HttpUtil.get(URL + "/tx/broadcastRaw")
                .data("txJson", rawJson)
                .data("instanceId", String.valueOf(nacInstanceId))
                .execute();

        String json = execute.body();
        System.out.println(JsonUtil.prettyByGson(json));

    }

    /**
     * Account send tx by api
     *
     * @throws Exception
     */
    private static void request_account_send() throws Exception {

        String fromAddress = "from address";
        String toAddress = "to address";
        String password = "password";
        double coinAmount = 10D;

        Connection.Response execute = HttpUtil.get(URL + "/account/send")
                .data("fromAddress", fromAddress)
                .data("toAddress", toAddress)
                .data("password", password)
                .data("value", String.valueOf(coinAmount))
                .data("instanceId", String.valueOf(nacInstanceId))
                .data("token", String.valueOf(nacTokenId))
                .data("remark", "thanks")
                .execute();

        String json = execute.body();
        System.out.println(JsonUtil.prettyByGson(json));

        /*
        HttpResult result = parseJson(json);
        if (result.getFlag()) {
            Map<String, String> map = (Map<String, String>) result.getData();

            String txHash = map.get("hash");
            String mail = map.get("mail");

            ###
            You can check the hash by "/tx/detail" and do your logic
            After the sending operation is performed, the status of the transaction needs to be trained according to the transaction hash.
            If the transaction can be retrieved and the status is correct, it proves that the transaction has taken effect.
            You can perform some business logic operations, such as increasing user account balance, inserting deposit records, etc.
            ##

        } else {
            System.out.println(result.getMsg());
        }
        */

    }

    /**
     * Get tx details
     * rawTx：native data
     *
     * @throws Exception
     */
    private static void request_tx_detail() throws Exception {

        String hash = "0x0b26d17e121e14a64984de35d0815ccd8cfee28af2f8cead4d005ac6f31bfffb";

        Connection.Response execute = HttpUtil.get(URL + "/tx/detail")
                .data("hash", hash)
                .data("instanceId", String.valueOf(nacInstanceId))
                .execute();

        String json = execute.body();
        System.out.println(JsonUtil.prettyByGson(json));

        // TIPS:
        //"status": 1 : Indicates that the transaction has been successful and confirmed

        /*
        {
          "flag": true,
          "message": "",
          "data": {

            <---------------------
            ##Items for easy interface display

            "hashText": "0x0b26d17e121e14a64984de35d0815ccd8cfee28af2f8cead4d005ac6f31bfffb",
            "txHeightText": "1",
            "dateTimeText": "17/10/2021 23:00:46",
            "fromText": "COINBASE",
            "toText": "NacqYpEAwb6ojnE8KrHQjnP6J5LUq3KabT",
            "amountText": "1.0",
            "statusText": "COMPLETED",
            "status": 1,
            "feeText": "0.0",
            --------------------->

            "rawTx": {
              "instance": 1,
              "version": 2,
              "timestamp": 1634482846723,
              "token": 1,
              "from": "COINBASE",
              "to": "NacqYpEAwb6ojnE8KrHQjnP6J5LUq3KabT",
              "value": 1000000000,
              "gas": 0,
              "gasType": 1,
              "gasLimit": 0,
              "gasInputData": [],
              "txHeight": 1,
              "txType": 1,
              "context": "{\"instanceType\":\"Token\",\"eventType\":\"TOKEN_TRANSFER\",\"referrerInstance\":1,\"referrerTx\":\"\",\"crossToInstance\":1,\"data\":\"\",\"txMark\":{\"clientName\":\"\",\"osName\":\"\"}}",
              "remarks": "GENESIS",
              "blockCondition": 0,
              "hash": "0x0b26d17e121e14a64984de35d0815ccd8cfee28af2f8cead4d005ac6f31bfffb",
              "senderSign": "0x1faf1a0b469ce7fdf8ec9a18a92c97419da842032323a5efd311733ac7ad72b3148119d451c4b2cd805f0d17ee2d1a4f536f38ec95bae477d99d2b34ff063f0050b1a2d1e11444f9cda872a86ba4e47cf0e33ac9bd19ece421f3a94542bb96ba",
              "status": 1,
              "eventStatus": 1,
              "eventInfo": "",
              "eventStates": [
                {
                  "stateType": 1,
                  "tx": "0x0b26d17e121e14a64984de35d0815ccd8cfee28af2f8cead4d005ac6f31bfffb",
                  "address": "COINBASE",
                  "before": 0,
                  "after": -1000000000,
                  "stateDifference": 1000000000,
                  "hash": "0xdf9703570e418b5776407a8232866a845b46fc4c4706cd305a1b37968704df5f"
                },
                {
                  "stateType": 1,
                  "tx": "0x0b26d17e121e14a64984de35d0815ccd8cfee28af2f8cead4d005ac6f31bfffb",
                  "address": "NacqYpEAwb6ojnE8KrHQjnP6J5LUq3KabT",
                  "before": 0,
                  "after": 0,
                  "stateDifference": 0,
                  "hash": "0x0108b823677c0e9dcdab3dc081fe8e094abce00e78d11bd4fdb87ae96c89fd7d"
                }
              ],
              "bleedValue": 0,
              "minedSign": "0x433b767608c56a2be8b8c54848a23b354995c64d7356cf75de682c13f0bf0d94dc8a5aa55e6864aa584a3e5924dadba16cbbbd6631f209db2d90e277cc8bc20e50b1a2d1e11444f9cda872a86ba4e47cf0e33ac9bd19ece421f3a94542bb96ba",
              "inputData": [],
              "output": {
                "instance": 0,
                "targetTx": "",
                "amount": 0
              },
              "changeTx": "",
              "blockHeight": 1
            }
          }
        }

        */

    }

    /**
     * Get the block height of different instances
     * The block height of different instances is not the same
     *
     * @throws Exception
     */
    private static void request_block_height() throws Exception {

        Connection.Response execute = HttpUtil.get(URL + "/block/lastHeight")
                .data("instanceId", String.valueOf(nacInstanceId))
                .execute();

        String json = execute.body();
        System.out.println(JsonUtil.prettyByGson(json));

    }

    /**
     * Get the block height of different instances by nascan
     * The block height of different instances is not the same
     *
     * @throws Exception
     */
    private static void request_block_height_by_nascan() throws Exception {

        Connection.Response execute = HttpUtil.get(URL + "/block/getLastHeightByScan")
                .data("instanceId", String.valueOf(nacInstanceId))
                .execute();

        String json = execute.body();
        System.out.println(JsonUtil.prettyByGson(json));
    }

    /**
     * Get block details
     * blocDataRow：mainly interface display
     * rawBlock：native data
     * txList：txs in the block
     *
     * @throws Exception
     */
    private static void request_block_detail() throws Exception {

        Connection.Response execute = HttpUtil.get(URL + "/block/detail")
                .data("height", String.valueOf(1))
                .data("instanceId", String.valueOf(nacInstanceId))
                .execute();

        String json = execute.body();
        System.out.println(JsonUtil.prettyByGson(json));

        /*

            {
              "flag": true,
              "message": "",
              "data": {
                "blocDataRow": {

                  <---------------------
                  ##Items for easy interface display

                  "heightText": "1",
                  "hashText": "0x8c8f88fde2e1cf7aba8f81d9fd423e8860b95675c6525d16bbf1562fda11c8a5",
                  "timeText": "17/10/2021 23:00:46",
                  "minerText": "MNc2bVLJp3iBHUosFbHTHQZjQZZeh6M6JM",
                  --------------------->

                  <---------------------
                  ## Raw block data

                  "rawBlock": {
                   "instance": 1,
                    "height": 1,
                    "timestamp": 1634482846985,
                    "miner": "MNc2bVLJp3iBHUosFbHTHQZjQZZeh6M6JM",
                    "blockReward": 1000000000,
                    "collectMined": 0,
                    "size": 275,
                    "parentHash": "",
                    "version": 2,
                    "transactionsRoot": "490285677655287c1e47ff19aae05f83a26b86b636bd881245b2bfcc46906135",
                    "txVolume": 4,
                    "gasUsed": 0,
                    "gasLimit": 0,
                    "gasMinimum": 0,
                    "gasMaximum": 0,
                    "extraData": {
                      "client": "Nirvana Chain(1.1.0 Beta)@Linux amd64 C24AM94164189184TM100797014016",
                      "dataCenter": "The Citadel",
                      "dappInvoking": "",
                      "gasDestroy": 0,
                      "uninstallAward": 0,
                      "gasAward": 0,
                      "bleedValue": 0
                    },
                    "hash": "0x8c8f88fde2e1cf7aba8f81d9fd423e8860b95675c6525d16bbf1562fda11c8a5",
                    "minedSign": "0xabde30bf5a123f04aafd2f2a7a162ae2802bebc1dfe0bd405c7c6c87781274099971429b30599d4a10408c4094c0a07eb2d610890c0a8ef20fd4d6231c880c0c50b1a2d1e11444f9cda872a86ba4e47cf0e33ac9bd19ece421f3a94542bb96ba"
                  }
                  --------------------->
                },

                <---------------------
                ## Transactions included in the block

                "txList": [
                  "0x0b26d17e121e14a64984de35d0815ccd8cfee28af2f8cead4d005ac6f31bfffb",
                  "0x7839ad1b59192c928bec654e8a53643953e4ed641b9794298ece9a7c37902350",
                  "0x49ba609d5f0b216a869a8deebcbcfd95fe1d9d7306b9b120867b9e3dff7d5c40",
                  "0x41bc45b37960fcb306f539411f7f816ec5415c85f99532caf439fa7fd7f20748"
                ]
                --------------------->
              }
            }

        */

    }

    /**
     * Get the balance of the account in NAC instance
     * data field will return a map containing the balance of each token
     *
     * @throws Exception
     */
    private static void request_account_balance() throws Exception {

        Connection.Response execute = HttpUtil.get(URL + "/account/balance")
                .data("address", "NTFuWBVDdDCJS3Uw24MbP4wJMybKwwsuJz")
                .data("instanceId", String.valueOf(nacInstanceId))
                .execute();

        String json = execute.body();
        System.out.println(JsonUtil.prettyByGson(json));

        /*
            {
              "flag": true,
              "message": "",
              "data": {
                "instance": 1,
                "tokenBalanceMap": {

                   // TokenID : Balance
                  "1": 829169755242,
                  "4": 49728877000000
                }
              }
            }
        */

        // parse json to object
        JsonNode dataByOm = JsonUtil.getDataByOm(json);
        JsonNode tokenBalanceMap = dataByOm.get("tokenBalanceMap");
        if (tokenBalanceMap != null) {

            JsonNode jsonNode = tokenBalanceMap.get(String.valueOf(CoreTokenEnum.NAC.id));
            if (jsonNode != null) {

                String nacBalanceText = jsonNode.asText();
                BigInteger nacBalanceBigInt = new BigInteger(nacBalanceText);

                BigDecimal bigDecimal = Amount.of(nacBalanceBigInt).toDecimal(Unit.NAC);
                double nacBalance = bigDecimal.doubleValue();

                System.out.println("Nac Balance:" + nacBalance);
            }

        }

    }

    /**
     * Get the detail of the wallet account in NAC instance
     *
     * @throws Exception
     */
    private static void request_account_detail() throws Exception {

        Connection.Response execute = HttpUtil.get(URL + "/account/detail")
                .data("address", "NiDeREkkMGPXtQspaGJzuH9DVzLKcjzVk6")
                .execute();

        String json = execute.body();
        System.out.println(JsonUtil.prettyByGson(json));

        /*
            {
              "flag": true,
              "message": "",
              "data": {
                "id": 5063,
                "version": 0,
                "addTime": [ 2022, 8, 01, 17, 49, 14 ],
                "UpdateTime": [ 2022, 8, 01, 17, 49, 14 ],
                "name": "wallet-1660643353907",
                "password": "",
                "mnemonic": "",
                "mnemonicEncrypt": "",
                "salt": "",
                "saltEncrypt": "",
                "address": "NiDeREkkMGPXtQspaGJzuH9DVzLKcjzVk6",
                "createType": 0,
                "fullNode": false,
                "defaultWallet": true,
                "hasBackup": false
              }
            }
        */
    }

    /**
     * Get paging data of account list
     *
     * @throws Exception
     */
    private static void request_account_list() throws Exception {

        // page number start from ZERO
        Connection.Response execute = HttpUtil.get(URL + "/account/list")
                .data("pageNum", "0")
                .data("pageSize", "20")
                .execute();

        String json = execute.body();
        System.out.println(JsonUtil.prettyByGson(json));

    }

    public static void main(String[] args) throws Exception {

        //request_account_new();
        //request_account_list();
        //request_account_balance();
        //request_account_detail();
        //request_block_height();
        //request_block_height_by_nascan();
        //request_block_detail();
        //request_tx_detail();
        //request_tx_broadcast();
        //request_gas_fee();
        //request_account_send();

    }

    /**
     * Create a new wallet and save it to the db of NaStation
     *
     * @throws Exception
     */
    private static void request_account_new() throws Exception {

        Connection.Response execute = HttpUtil.get(URL + "/account/new")
                .data("name", "wallet-" + new Date().getTime())
                .data("password", "change@123456")
                // Generally speaking, creating a wallet does not require setting a salt
                // unless you have higher security requirements
                //.data("salt", "hello")
                .execute();

        String json = execute.body();
        System.out.println(JsonUtil.prettyByGson(json));

        /*
        {
          "flag": true,
          "message": "",
          "data": {
            "privateKey": "0x302e02010030050603.....40dbc9c03d981ce18f1cdeb64f04b890acf1ab5",
            "password": "Abc@123456",
            "salt": "",
            "name": "wallet-xxxx",
            "mnemonic": "boat plug frog ...... vibrant envelope",
            "id": "47"
          }
        }
        */

    }

    /**
     * Ping test
     * @throws Exception
     */
    private static void request_ping() throws Exception {

        Connection.Response execute = HttpUtil.get(URL + "/ping")
                .execute();

        String json = execute.body();
        System.out.println(JsonUtil.prettyByGson(json));
    }

}
