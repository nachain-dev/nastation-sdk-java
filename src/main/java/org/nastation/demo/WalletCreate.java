package org.nastation.demo;

import org.nachain.core.crypto.bip39.Language;
import org.nachain.core.crypto.bip39.MnemonicGenerator;
import org.nachain.core.util.Hex;
import org.nachain.core.wallet.Keystore;
import org.nachain.core.wallet.WalletUtils;

import java.util.List;

/**
 * This example shows how to create and restore wallets via native methods
 *
 * Recommended Use:
 * NaStationRestApi - request_account_new()
 *
 * @author John | Nirvana Core
 * @since 12/06/2021
 */
public class WalletCreate {

    public static void main(String[] args) throws Exception {

        System.out.println("##### Get all language: ##### ");
        List<Language> languages = MnemonicGenerator.languages;
        for (Language l : languages) {
            System.out.println(l.name());
        }

        System.out.println("##### Create Wallet: #####");

        // lang
        Language language = Language.ENGLISH;

        // words
        String words = WalletUtils.generateWords(language);

        // salt
        // No need to set the salt unless there are higher security requirements
        String salt = null;//"123456"

        //generally speaking, index defaults to 0
        //if no salt value then input null
        Keystore keystore = WalletUtils.generate(language, words, salt, 0);
        System.out.println(keystore);

        System.out.println("##### Restore Wallet: #####");
        {
            // generate
            Keystore keystore1 = WalletUtils.generate(language, words);
            System.out.println(keystore1);
            byte[] privateKey = keystore1.getPrivateKey();

            // privateKey text
            String pkHex = Hex.encode0x(privateKey);
            System.out.println(pkHex);

            // restore
            byte[] restorePK = Hex.decode0x(pkHex);
            Keystore keystore2 = WalletUtils.generate(restorePK);
            System.out.println(keystore2.toString());
        }

    }

}
