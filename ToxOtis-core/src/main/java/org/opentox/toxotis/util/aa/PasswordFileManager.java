/*
 *
 * ToxOtis
 *
 * ToxOtis is the Greek word for Sagittarius, that actually means ‘archer’. ToxOtis
 * is a Java interface to the predictive toxicology services of OpenTox. ToxOtis is
 * being developed to help both those who need a painless way to consume OpenTox
 * services and for ambitious service providers that don’t want to spend half of
 * their time in RDF parsing and creation.
 *
 * Copyright (C) 2009-2010 Pantelis Sopasakis & Charalampos Chomenides
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact:
 * Pantelis Sopasakis
 * chvng@mail.ntua.gr
 * Address: Iroon Politechniou St. 9, Zografou, Athens Greece
 * tel. +30 210 7723236
 *
 */
package org.opentox.toxotis.util.aa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import javax.crypto.BadPaddingException;
import org.opentox.toxotis.exceptions.impl.ServiceInvocationException;
import org.opentox.toxotis.exceptions.impl.ToxOtisException;

/**
 * <p align=justify>
 * Create highly secure password files to store your credentials. Your username and
 * password are stored in an encrypted format using a master key that you have to
 * provide. The master key is used when you need to create a new username+password file
 * or when you need to use this password file to authenticate your self. This way,
 * you will never need to provide your credentials within your application. Firstly,
 * you have to create a password file that has the following structure:</p>
 * 
 * 
 * <pre>--- START MASTER KEY ---
 * m2gWQ1FRUVdSXXxhOVBnazQyKy8vUzRcPWFfJ2tmKpE
 * tZm1rcm5rZ2MNCaA/RPQtLjJfFjt/bHQ0S0Q/MzkxbU
 * qg|lztU51cRDRmCcczbmMjZ3ZiZlFRbGRlYWtmbnIoL
 * jAaYWZlLW5lbWbwT0IERXgHtUtubWQ+ZjJtQEBtZmlh
 * ZG5hamVuZmdyb18xOTQVIWD3A:FJMQYzN5c2hjM1vTk
 * ... [ More random characters go here ] ....
 * eq25nDVg4sfmaFp4g5taFghtSD2g5l09Ufn*adegHlo
 * --- END MASTER KEY ---</pre>
 * 
 * 
 * <p align=justify>
 * This can be done using the method {@link PasswordFileManager#createMasterPasswordFile(java.lang.String, java.lang.String, int, boolean)  }.
 * Store this file somewhere on your machine (We suggest that you made it hidden
 * and specify its permissions properly). Then you can use this file to create an
 * encrypted password file or authenticate yourself against the SSO server providing
 * the path of your password file and not your credentials! Here are two examples:
 * </p>
 * 
 * 
 * <pre> // Example 1: Create a password file
 * PasswordFileManager.CRYPTO.createMasterPasswordFile("john", "s3cret", "./secret/my.key");
 * </pre>
 * 
 * 
 * <pre> // Example 2: Authentication using the password file
 * // No credentials are provided
 * AuthenticationToken at = PasswordFileManager.CRYPTO.
 *    authFromFile("./secret/my.key");
 * </pre>
 * 
 * 
 * <b>It does not work:</b> The most common reasons for exceptional events are the
 * following:
 * <ol>
 * <li>The master password file is not found: Make sure you have created a master
 * password file and have stored it in a directory where the application has access
 * and read priviledges. You can set the path of the master key file using the method
 * {@link PasswordFileManager#setMasterPasswordFile(java.lang.String)
 * setMasterPasswordFile(String)}. The default destination for this file is
 * './secret/secret.key' on linux and MacOSX machines and '.\secret\secret.key'
 * on Windows.</li>
 * <li>The master key file has bad syntax: Make sure your master key file has the
 * header <code>--- START MASTER KEY ---</code> as the first line and the footer
 * <code>--- END MASTER KEY ---</code> at the end. Leave no empty lines.</li>
 * <li>The master key is found and is valid but you cannot authenticate yourself:
 * This might happen if your password file was created with less or more <code>salt</code>.
 * Check out the salting iterations using the method {@link PasswordFileManager#getCryptoIterations()
 * getCryptoIterations()} (The default value is 23).</li>
 * </ol>
 * 
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 *
 * @see PasswordFileManager#authFromFile(java.io.File)
 * @see PasswordFileManager#createPasswordFile(java.lang.String, java.lang.String, java.lang.String) 
 */
public final class PasswordFileManager extends Observable {

    /**
     * Cryptographic Salt Iterations
     */
    private static final int DEFAULT_CRYPTO_ITERATIONS = 23;
    /**
     * Maximum number of comment lines allowed
     * in the master file before the data comes.
     */
    private static final int MAX_COMMENT_LINES = 200;
    private static final int MIN_CHAR_INDEX = 33, MAX_CHAR_INDEX = 126, LINE_SIZE = 50;
    private static final int PWD_STRENGTH1 = 50, PWD_STRENGTH2 = 100, PWD_STRENGTH3 = 500;
    private int cryptoIterations = DEFAULT_CRYPTO_ITERATIONS;
    private javax.crypto.Cipher eCipher;
    private javax.crypto.Cipher dCipher;
    private static final String PATH_SEP = System.getProperty("file.separator");
    private static final String MASTER_START = "--- START MASTER KEY ---",
            MASTER_END = "--- END MASTER KEY ---", ENCODING_STD = "UTF-8";
    private static final String DEFAULT_MASTER_PASSWORD_FILE =
            System.getProperty("user.home") + PATH_SEP + "toxotisKeys" + PATH_SEP + "master.key";
    private static final double MAX_PERCENTAGE_COMPLETED = 100;
    private static final byte[] SALT = {
        (byte) 0xaf, (byte) 0x35, (byte) 0x11, (byte) 0x0c,
        (byte) 0xd6, (byte) 0xdd, (byte) 0x02, (byte) 0x1a};
    private String masterPasswordFile;
    private static char[] masterPassword;
    /** This class is a singleton and this is the access point for it. Being final users
    don't need to worry about synchronization*/
    public static final PasswordFileManager CRYPTO = getInstance();
    /** A dummy variable holding the instance of the singleton. */
    private static PasswordFileManager instanceOfThis = null;
    private double fileCreationProgress = 0;
    private static final Random RNG = new Random();
    private PrintStream printStream = System.out;
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PasswordFileManager.class);

    /**
     * Where to stream messages. This is meaningful only in 
     * verbose mode.
     * @param printStream
     */
    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }

    private static PasswordFileManager getInstance() {
        if (instanceOfThis == null) {
            instanceOfThis = new PasswordFileManager();
        }
        return instanceOfThis;
    }

    private PasswordFileManager() {
        super();
        addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                PasswordFileManager.CRYPTO.fileCreationProgress = Double.parseDouble(arg.toString());
            }
        });
    }

    /**
     * Returns a double between 0 and 100 that monitors the progress of the
     * password file creation.
     * @return
     *      Password generation progress
     */
    public double getPasswordGenerationProgress() {
        return fileCreationProgress;
    }

    private String getPassStrength(final int size) {
        String passStrength;
        if (size < PWD_STRENGTH1) {
            passStrength = "POOR";
        } else if (size >= PWD_STRENGTH1 && size < PWD_STRENGTH2) {
            passStrength = "ACCEPTABLE";
        } else if (size >= PWD_STRENGTH2 && size < PWD_STRENGTH3) {
            passStrength = "GOOD";
        } else {
            passStrength = "EXCELECT";
        }
        return passStrength;
    }

    private StringBuilder readRandomGenerator(String randomGenerator, int size)
            throws IOException {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(randomGenerator);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            char[] characters;
            int readInt = -1;
            StringBuilder passBuilder = new StringBuilder();
            int charCounter = 0;
            super.setChanged();
            while ((readInt = br.read()) != -1 && charCounter < size) {
                if (readInt < MAX_CHAR_INDEX && readInt > MIN_CHAR_INDEX) {
                    characters = Character.toChars(readInt);
                    passBuilder.append(characters);
                    charCounter++;
                    notifyObservers(MAX_PERCENTAGE_COMPLETED * (double) charCounter / ((double) size));
                    super.setChanged();
                }
            }
            return passBuilder;
        } finally {
            IOException ioexception = null;
            if (br != null) {
                try {
                    br.close();
                } catch (final IOException e) {
                    logger.error("Cannot close buffered reader", e);
                    ioexception = e;
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (final IOException e) {
                    logger.error("Cannot close ISR", e);
                    ioexception = e;
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (final IOException e) {
                    logger.error("Cannot close FIS", e);
                    ioexception = e;
                }
            }
            if (ioexception != null) {
                throw ioexception;
            }
        }

    }

    /**
     * Create a master password file for managing you password files. You credentials
     * will be stored in a password file
     * @param randomGenerator
     *      Path to random numbers generator device (for example on a Linux machine
     *      you can choose <code>/dev/random</code> or <code>/dev/urandom</code>).
     *      Setting it to <code>null</code>,a pseudorandom generator that depends
     *      on <code>SecureRandom</code> will be used.
     * @param destination
     *      Path where the master key will be stored
     * @param size
     *      Size of your password
     * @param verbose
     *      Whether information about the procedure should be output to the console
     * @throws IOException
     *      In case either the random generator or the destination for the master
     *      password are unreachable or a read/write exception occurs.
     */
    public void createMasterPasswordFile(final String randomGenerator,
            final String destination, final int size, boolean verbose) throws IOException {
        if (verbose) {
            printStream.println("----- ToxOtis Pasword Generator -----");
        }
        String passStrength = getPassStrength(size);

        String rng = randomGenerator != null ? randomGenerator : "Secure RNG (java.security.SecureRandom)";
        if (verbose) {
            printStream.println("Random number generator : " + rng);
            printStream.println("Password file           : " + destination);
            printStream.println("Password Stength        : " + passStrength + " (" + size + ")");
        }

        if (randomGenerator != null) {
            if (randomGenerator.contains("/random") && verbose) {
                printStream.println("\nMore Entropy needed... Move your mouse around the screen to generate some random bits.");
                printStream.println("This procedure might take a few minutes... You may use /dev/urandom to create a pseudo random key faster");
            }
            StringBuilder passBuilder = readRandomGenerator(randomGenerator, size);

            FileWriter fw = new FileWriter(destination);
            BufferedWriter bufferedWriter = new BufferedWriter(fw);
            bufferedWriter.write(MASTER_START);
            bufferedWriter.newLine();
            String mPass = Base64.encodeString(passBuilder.toString());
            int lineCharsCounter = 0;
            for (int k = 0; k < mPass.length(); k++) {
                bufferedWriter.write(mPass.charAt(k));
                lineCharsCounter++;
                if (lineCharsCounter == LINE_SIZE) {
                    bufferedWriter.newLine();
                    lineCharsCounter = 0;
                }
            }
            bufferedWriter.newLine();
            bufferedWriter.write(MASTER_END);
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        } else {// randomGenerator == null
            try {
                SecureRandom secureRand = SecureRandom.getInstance("SHA1PRNG");
                secureRand.setSeed(RNG.nextLong());
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < size; i++) {
                    builder.append(secureRand.nextDouble());
                }
                FileWriter fw = new FileWriter(destination);
                BufferedWriter bufferedWriter = new BufferedWriter(fw);
                bufferedWriter.write(MASTER_START);
                bufferedWriter.newLine();
                String mPass = Base64.encodeString(builder.toString());
                int lineCharsCounter = 0;
                for (int k = 0; k < mPass.length(); k++) {
                    bufferedWriter.write(mPass.charAt(k));
                    lineCharsCounter++;
                    if (lineCharsCounter == LINE_SIZE) {
                        bufferedWriter.newLine();
                        lineCharsCounter = 0;
                    }
                }
                bufferedWriter.newLine();
                bufferedWriter.write(MASTER_END);
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }

            } catch (NoSuchAlgorithmException ex) {
                logger.error(null, ex);
            }
        }
    }

    private void initializeMasterPassword() {
        if (masterPassword == null || (masterPassword != null && masterPassword.length == 0)) {// Initialize the master password (if not already initialized)
            if (masterPasswordFile == null) {//Use the default password file...
                masterPasswordFile = DEFAULT_MASTER_PASSWORD_FILE;
            }
            FileReader fr = null;
            BufferedReader br = null;
            try {
                File secretFile = new File(masterPasswordFile);
                if (!secretFile.exists()) {
                    throw new IllegalArgumentException(
                            String.format("File containing the master password was not found at : '%s'",
                            secretFile.getAbsolutePath()));
                }
                fr = new FileReader(secretFile);
                br = new BufferedReader(fr);
                String line = null;
                StringBuilder buffer = new StringBuilder();
                int count = 0;
                while ((line = br.readLine()) != null && !line.equals(MASTER_START)) {
                    count++;
                    if (count > MAX_COMMENT_LINES) {
                        throw new IllegalArgumentException("Invalid Master-Key file");
                    }
                }
                while ((line = br.readLine()) != null && !line.equals(MASTER_END)) {
                    line = line.trim();
                    buffer.append(line);
                }
                masterPassword = Base64.decodeString(buffer.toString()).toCharArray();
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            } finally {
                if (fr != null) {
                    try {
                        fr.close();
                    } catch (IOException ex) {
                        String message = "Unexpected error while trying to "
                                + "close a File Reader used to read the master password of JAQPOT.";
                        throw new RuntimeException(message, ex);
                    }
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ex) {
                        String message = "Unexpected error while trying to "
                                + "close a Buffered Reader used to read the master password of JAQPOT.";
                        throw new RuntimeException(message, ex);
                    }
                }
            }
            java.security.Security.addProvider(new com.sun.crypto.provider.SunJCE());
            int iterations = cryptoIterations;
            createChipher(masterPassword, SALT, iterations);
        }
    }

    private void createChipher(char[] pass, byte[] salt, int iterations) {
        try {
            javax.crypto.spec.PBEParameterSpec paramSpec =
                    new javax.crypto.spec.PBEParameterSpec(salt, iterations);
            javax.crypto.SecretKeyFactory secretKeyFactory =
                    javax.crypto.SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            javax.crypto.SecretKey secretKey =
                    secretKeyFactory.generateSecret(new javax.crypto.spec.PBEKeySpec(pass));
            String chipherTransformation = "PBEWithMD5AndDES/CBC/PKCS5Padding";
            eCipher = javax.crypto.Cipher.getInstance(chipherTransformation);
            eCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, paramSpec);
            dCipher = javax.crypto.Cipher.getInstance(chipherTransformation);
            dCipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, paramSpec);
        } catch (Exception exc) {
            throw new SecurityException(exc);
        }
    }

    protected synchronized String encrypt(final String message) {
        initializeMasterPassword();
        try {
            byte[] stringBytes = message.getBytes(ENCODING_STD);
            byte[] enc = eCipher.doFinal(stringBytes);
            return new String(Base64.encode(enc));
        } catch (final Exception exc) {
            logger.error("Encryption Error", exc);
            throw new SecurityException(exc);
        }
    }

    protected synchronized String decrypt(final String encrypted) {
        initializeMasterPassword();
        try {
            byte[] dec = Base64.decode(encrypted);
            byte[] utf8 = dCipher.doFinal(dec);
            return new String(utf8, ENCODING_STD);
        } catch (BadPaddingException ex) {
            String msg = "The master key at " + masterPasswordFile + " might has been corrupted!";
            logger.error(msg, ex);
            throw new SecurityException(msg, ex);
        } catch (final Exception exc) {
            logger.error("Deciphering Error", exc);
            throw new SecurityException(exc);
        }
    }

    /**
     * Create an encrypted file containing the credentials provided in this method.
     * You need the master password file that was used to encrypt these credentials
     * to decrypt them and use them to acquire an authentication token.
     * @param username
     *      You username
     * @param password
     *      Your password
     * @param filePath
     *      The file where the credentials should be stored.
     * @throws IOException
     *      In case the destination is not reachable or you do not have access and
     *      write priviledges to that file or other I/O event inhibits the data
     *      transaction.
     */
    public synchronized void createPasswordFile(String username, String password, String filePath)
            throws IOException {
        initializeMasterPassword();
        FileWriter fstream = null;
        BufferedWriter out = null;
        try {
            fstream = new FileWriter(filePath);
            out = new BufferedWriter(fstream);
            out.write("--- START PRIVATE KEY ---\n");
            out.write(encrypt(username));
            out.write("\n");
            out.write(encrypt(password));
            out.write("\n--- END PRIVATE KEY ---\n");
            out.write("#Master Key: " + masterPasswordFile);
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
            if (fstream != null) {
                fstream.close();
            }
        }
    }

    /**
     * Acquire an authentication token using the credentials in an encrypted file.
     * These are decrypted using the master key (the destination to the master
     * key file should have been set properly) and POSTed to the SSO server which
     * returns a token for that user or an error response if the credentials are
     * not valid.
     *
     * @param filePath
     *      Path to the file containing the credentials
     * @return
     *      An Authentication Token upon successful authentication against the
     *      SSO server.
     * @throws IOException
     *      In case the file path you provided cannot be found (an instance of
     *      <code>java.io.FileNotFoundException</code> or if you do not have
     *      sufficient priviledges to read from that file.
     * @throws ToxOtisException
     *      In case the remote SSO server responds with an error code (e.g. 401 -
     *      Unauthorized or 403 - Forbidden), other HTTP related events inhibit the
     *      creation of am Authentication Token or if the provided password file
     *      is not valid.
     * @throws org.opentox.toxotis.exceptions.impl.ServiceInvocationException
     * @see PasswordFileManager#authFromFile(java.io.File) authFromFile(File)
     */
    public synchronized AuthenticationToken authFromFile(String filePath)
            throws IOException, ToxOtisException, ServiceInvocationException {
        initializeMasterPassword();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("No file found at : '" + filePath + "'");
        }
        return authFromFile(file);
    }

    /**
     * Acquire an authentication token using the credentials in an encrypted file.
     * These are decrypted using the master key (the destination to the master
     * key file should have been set properly) and POSTed to the SSO server which
     * returns a token for that user or an error response if the credentials are
     * not valid.
     *
     * @param file
     *      File containing the credentials (an instance of <code>java.io.File</code>)
     * @return
     *      An Authentication Token upon successful authentication against the
     *      SSO server.
     * @throws IOException
     *      In case the file path you provided cannot be found (an instance of
     *      <code>java.io.FileNotFoundException</code> or if you do not have
     *      sufficient priviledges to read from that file.
     * @throws ToxOtisException
     *      In case the remote SSO server responds with an error code (e.g. 401 -
     *      Unauthorized or 403 - Forbidden), other HTTP related events inhibit the
     *      creation of am Authentication Token or if the provided password file
     *      is not valid.
     * @throws org.opentox.toxotis.exceptions.impl.ServiceInvocationException
     * @see PasswordFileManager#authFromFile(java.lang.String) authFromFile(String)
     */
    public synchronized AuthenticationToken authFromFile(File file)
            throws IOException, ToxOtisException, ServiceInvocationException {
        initializeMasterPassword();
        FileReader fr = new FileReader(file);
        BufferedReader br = null;
        String username = null;
        String password = null;
        try {
            br = new BufferedReader(fr);
            String line;
            line = br.readLine();
            if (line == null || (line != null && !line.equals("--- START PRIVATE KEY ---"))) {
                throw new ToxOtisException("Invalid password file: Header not found!");
            }
            line = br.readLine();
            if (line != null) {
                username = decrypt(line);
            }
            line = br.readLine();
            if (line != null) {
                password = decrypt(line);
            }
            line = br.readLine();
            if (line == null || (line != null && !line.equals("--- END PRIVATE KEY ---"))) {
                throw new ToxOtisException("Invalid password file: Footer not found!");
            }
            return new AuthenticationToken(username, password);
        } finally {
            username = null;
            password = null;
            if (fr != null) {
                fr.close();
            }
            if (br != null) {
                br.close();
            }
        }
    }

    /**
     * Set the location of the master password file. By default this is set
     * to './secret/secret.key'
     * @param masterPasswordFile
     *      Path to the master password file
     * @see PasswordFileManager Documentation
     */
    public void setMasterPasswordFile(String masterPasswordFile) {
        this.masterPasswordFile = masterPasswordFile;
    }

    /**
     * Number of salting iterations that is used by this encryption/decryption
     * algorithm.
     * @return
     *      Returns the number of salting for the username and password.
     */
    public int getCryptoIterations() {
        return cryptoIterations;
    }

    /**
     * Set the salting iterations to be used in all encryption/decryption
     * operations of the Password Manager. A large number of salting iterations
     * offers greater security but might cause slower responses.
     * @param cryptoIterations
     *      Number of salting iteration.
     */
    public void setCryptoIterations(int cryptoIterations) {
        this.cryptoIterations = cryptoIterations;
    }
}
