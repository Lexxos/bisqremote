package bisq.notification;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHelper {

    private IvParameterSpec ivspec;
    private SecretKeySpec keyspec;
    private Cipher cipher;

    public CryptoHelper(String key_) {
        updateKey(key_);

        try {
            cipher = Cipher.getInstance("AES/CBC/NOPadding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public byte[] newIV() {
        return cipher.getIV();
    }

    public void updateKey(String key_) {
        if (key_.length() != 32) {
            try {
                throw new Exception("key not 32 characters");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        keyspec = new SecretKeySpec(key_.getBytes(), "AES");
    }

    public String encrypt(String valueToEncrypt, String iv) throws Exception {
        while (valueToEncrypt.length() % 16 != 0) { valueToEncrypt = valueToEncrypt + " "; }

        if ( iv.length() != 16) { throw new Exception( "iv not 16 characters"); }
        ivspec = new IvParameterSpec(iv.getBytes());
        byte[] encryptedBytes = encryptInternal(valueToEncrypt, ivspec);
        String encryptedBase64 = Base64.encode(encryptedBytes);
        return encryptedBase64;
    }

    public String decrypt(String valueToDecrypt, String iv) throws Exception {
        if ( iv.length() != 16) { throw new Exception( "iv not 16 characters"); }
        ivspec = new IvParameterSpec(iv.getBytes());
        byte[] decryptedBytes = decryptInternal(valueToDecrypt, ivspec);
        String decryptedString = new String(decryptedBytes);
        return decryptedString;
    }

    private byte[] encryptInternal(String text, IvParameterSpec ivspec) throws Exception {
        if (text == null || text.length() == 0) {
            throw new Exception("Empty string");
        }

        byte[] encrypted = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            encrypted = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            throw new Exception("[encrypt] " + e.getMessage());
        }
        return encrypted;
    }

    private byte[] decryptInternal(String code, IvParameterSpec ivspec) throws Exception {
        if (code == null || code.length() == 0) {
            throw new Exception("Empty string");
        }

        byte[] decrypted = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            decrypted = cipher.doFinal(Base64.decode(code));
        } catch (Exception e) {
            throw new Exception("[decrypt] " + e.getMessage());
        }
        return decrypted;
    }
}
