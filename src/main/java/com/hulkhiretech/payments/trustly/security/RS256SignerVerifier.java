package com.hulkhiretech.payments.trustly.security;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hulkhiretech.payments.trustly.req.DataNode;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;


import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class RS256SignerVerifier {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final Gson gson = new Gson();

    /* =========================================================
       PHP: $algorithm2prefix
       ========================================================= */
    private static final Map<String, String> ALGORITHM_TO_PREFIX = Map.of(
            "SHA1withRSA", "",
            "SHA256withRSA", "alg=RS256;",
            "SHA384withRSA", "alg=RS384;",
            "SHA512withRSA", "alg=RS512;"
    );

    /* =========================================================
       PHP: serialize_data($object)
       ========================================================= */
    public static String serializeData(Object object) {
        StringBuilder sb = new StringBuilder();
        serialize(object, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void serialize(Object object, StringBuilder sb) {

        if (object == null) return;

        if (object instanceof Map) {
            TreeMap<String, Object> sorted =
                    new TreeMap<>((Map<String, Object>) object);

            for (Map.Entry<String, Object> entry : sorted.entrySet()) {
                sb.append(entry.getKey());
                serialize(entry.getValue(), sb);
            }

        } else if (object instanceof List) {
            for (Object value : (List<?>) object) {
                serialize(value, sb);
            }

        } else if (object instanceof String
                || object instanceof Number
                || object instanceof Boolean) {

            sb.append(object.toString());

        } else {
            // POJO → Map → recurse
            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> map = gson.fromJson(gson.toJson(object), mapType);
            serialize(map, sb);
        }
    }

    /* =========================================================
       PHP: sign()
       ========================================================= */
    private static String sign(
            String method,
            String uuid,
            Object data,
            String privateKeyPath
    ) throws Exception {

        PrivateKey privateKey = loadPrivateKey(new FileReader(privateKeyPath));

        String plaintext = method + uuid + serializeData(data);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] signed = signature.sign();
        String base64 = Base64.getEncoder().encodeToString(signed);

        //return ALGORITHM_TO_PREFIX.get("SHA256withRSA") + base64;
        return base64;
    }

    /* =========================================================
       PHP: verify()
       ========================================================= */
    public static boolean verify(
            String method,
            String uuid,
            Object data,
            String signatureFromTrustly,
            String publicKeyPath
    ) throws Exception {

        String algorithm = "SHA1withRSA";

        if (signatureFromTrustly.startsWith("alg=RS")) {
            String prefix = signatureFromTrustly.substring(0, 10);
            signatureFromTrustly = signatureFromTrustly.substring(10);

            for (Map.Entry<String, String> e : ALGORITHM_TO_PREFIX.entrySet()) {
                if (e.getValue().equals(prefix)) {
                    algorithm = e.getKey();
                    break;
                }
            }
        }

        PublicKey publicKey = loadPublicKey(new FileReader(publicKeyPath));

        String plaintext = method + uuid + serializeData(data);

        Signature signature = Signature.getInstance(algorithm);
        signature.initVerify(publicKey);
        signature.update(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] decoded = Base64.getDecoder().decode(signatureFromTrustly);
        return signature.verify(decoded);
    }

    /* =========================================================
       FIXED PRIVATE KEY LOADER (PKCS#1 + PKCS#8)
       ========================================================= */
    private static PrivateKey loadPrivateKey(Reader reader) throws Exception {

        try (PEMParser parser = new PEMParser(reader)) {
            Object obj = parser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            if (obj instanceof PEMKeyPair) {
                // -----BEGIN RSA PRIVATE KEY-----
                return converter.getKeyPair((PEMKeyPair) obj).getPrivate();
            }

            if (obj instanceof PrivateKeyInfo) {
                // -----BEGIN PRIVATE KEY-----
                return converter.getPrivateKey((PrivateKeyInfo) obj);
            }

            throw new IllegalArgumentException("Unsupported private key format: " + obj);
        }
    }

    private static PublicKey loadPublicKey(Reader reader) throws Exception {

        try (PEMParser parser = new PEMParser(reader)) {
            SubjectPublicKeyInfo info =
                    (SubjectPublicKeyInfo) parser.readObject();

            return new JcaPEMKeyConverter().getPublicKey(info);
        }
    }

    /* =========================================================
       MAIN METHOD — YOU CAN RUN THIS
       ========================================================= */
    public static class TrustlySignatureTestRunner {

        public static void main(String[] args) throws Exception {

            Map<String, Object> data = Map.of(
                    "Amount", "103.00",
                    "Currency", "SEK",
                    "Country", "SE"
            );

            String signature = sign(
                    "Deposit",
                    "TXN123456789",
                    data,
                    "./src/main/resources/merchant-private.pem"
            );

            System.out.println("Signature:");
            System.out.println(signature);

            boolean isValid = verify(
                    "Deposit",
                    "TXN123456789",
                    data,
                    signature,
                    "./src/main/resources/merchant-public.pem"
            );

            System.out.println("Is Valid = " + isValid);
        }
    }

	public static String sign(String method, String uuid, DataNode data) {
		 try {
			PrivateKey privateKey = loadPrivateKey(new FileReader("./src/main/resources/merchant-private.pem"));
			//String plaintext = method + uuid + serializeData(data);

			String signature =sign(
		             method,
		             uuid,
		           data,
		           "./src/main/resources/merchant-private.pem");
			System.out.println(signature);
			return signature;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	        
		
		
	}
}
