package fi.iki.elonen.handler;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

public class StreamUrl extends RouterNanoHTTPD.DefaultStreamHandler {

    @Override
    public String getMimeType() {
        return "text/plain";
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {
        return NanoHTTPD.Response.Status.OK;
    }

    @Override
    public InputStream getData() {
        return new ByteArrayInputStream("a stream of data ;-)".getBytes());
    }

}

class CipherBenchmark {
    private static String TAG = "CipherBenchmark";

    public static void main(String[] args) {
        new CipherBenchmark().runtest();
    }


    void print(String log) {
        System.out.println(log);
    }

    public byte[] random_bytes(int len) {
        Random random = new Random();
        byte[] bytes = new byte[len];
        random.nextBytes(bytes);
        return bytes;
    }

    long start_time = 0;
    long end_time = 0;
    double time_diff = 0;

    public byte[] dofinal_(byte[] plain, Cipher cipher, int num) throws BadPaddingException, IllegalBlockSizeException {
        cipher.doFinal(plain);
        start_time = System.nanoTime();
        byte[] cipher_data = null;
        for (int i = 0; i < num; i++)
            cipher_data = cipher.doFinal(plain);
        end_time = System.nanoTime();
        time_diff = (end_time - start_time) / 1e6;
        return cipher_data;
    }


    public byte[] dofinal_dec(byte[] cipher_data, Cipher cipher, int num) throws BadPaddingException, IllegalBlockSizeException {
        cipher.doFinal(cipher_data);
        start_time = System.nanoTime();
        byte[] dec_plain = null;
        for (int i = 0; i < num; i++)
            dec_plain = cipher.doFinal(cipher_data);
        end_time = System.nanoTime();
        time_diff = (end_time - start_time) / 1e6;
        return dec_plain;
    }

    public void runtest() {
        try {
            SecretKey aes_key_128 = new SecretKeySpec(random_bytes(16), "AES");
            SecretKey aes_key_256 = new SecretKeySpec(random_bytes(32), "AES_256");
            SecretKey des_key = new SecretKeySpec(random_bytes(8), "DES");
            SecretKey desede_key = new SecretKeySpec(random_bytes(24), "DESede");
            byte[] tea_key = random_bytes(16);
            IvParameterSpec aes_iv = new IvParameterSpec(random_bytes(16));
            IvParameterSpec des_iv = new IvParameterSpec(random_bytes(8));
            byte[] plain = random_bytes(10 * 1024 * 1024);
            byte[] cipher_data = null, dec_plain = null;
            //byte[] plain = "Test Cipher".getBytes();


            String transfomation = "";
            Cipher cipher;
/*
            print("# Blowfish:");
            transfomation = "Blowfish";
            cipher = Cipher.getInstance(transfomation);
            String Key = "Something";
            byte[] KeyData = Key.getBytes();
            SecretKeySpec KS = new SecretKeySpec(KeyData, "Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, KS);
            for(int i=1;i<=10;i++){
                cipher_data=dofinal_(plain,cipher,i);/////////
                print(i+"* [" + transfomation + "] ENC: " + String.format("%.1f", plain.length*i/time_diff/1024/1024*1000) + " MB/s");
            }
            cipher.init(Cipher.DECRYPT_MODE, KS);
            for(int i=1;i<=10;i++){
                dec_plain=dofinal_dec(cipher_data,cipher,i);/////////
            if (!Arrays.equals(dec_plain, plain)) {
                print(transfomation + " DECRYPT FAILED");
            } else {
                print(i+"* [" + transfomation + "] DEC: " + String.format("%.1f", cipher_data.length*i/time_diff/1024/1024*1000) + " MB/s");
            }}


            print("# AES: ");
            transfomation = "AES/CBC/PKCS5Padding";
            cipher = Cipher.getInstance(transfomation);
            cipher.init(Cipher.ENCRYPT_MODE, aes_key_128, aes_iv);
            for(int i=1;i<=10;i++){
                cipher_data=dofinal_(plain,cipher,i);/////////
                print(i+"* [" + transfomation + "] ENC: " + String.format("%.1f", plain.length*i/time_diff/1024/1024*1000) + " MB/s");
            }
            cipher.init(Cipher.DECRYPT_MODE, aes_key_128, aes_iv);
            for(int i=1;i<=10;i++){
                dec_plain=dofinal_dec(cipher_data,cipher,i);/////////
                if (!Arrays.equals(dec_plain, plain)) {
                    print(transfomation + " DECRYPT FAILED");
                } else {
                    print(i+"* [" + transfomation + "] DEC: " + String.format("%.1f", cipher_data.length*i/time_diff/1024/1024*1000) + " MB/s");
                }}



            print("# DES: ");
            // DES Require IV 8 bytes long
            transfomation = "DES/CBC/PKCS5Padding";
            cipher = Cipher.getInstance(transfomation);
            cipher.init(Cipher.ENCRYPT_MODE, des_key, des_iv);
            for(int i=1;i<=10;i++){
                cipher_data=dofinal_(plain,cipher,i);/////////
                print(i+"* [" + transfomation + "] ENC: " + String.format("%.1f", plain.length*i/time_diff/1024/1024*1000) + " MB/s");
            }
            cipher.init(Cipher.DECRYPT_MODE, des_key, des_iv);
            for(int i=1;i<=10;i++){
                dec_plain=dofinal_dec(cipher_data,cipher,i);/////////
                if (!Arrays.equals(dec_plain, plain)) {
                    print(transfomation + " DECRYPT FAILED");
                } else {
                    print(i+"* [" + transfomation + "] DEC: " + String.format("%.1f", cipher_data.length*i/time_diff/1024/1024*1000) + " MB/s");
                }}



           //des_iv = new IvParameterSpec(random_bytes(24));///////
            print("# 3DES: ");
            transfomation = "DESede/CBC/PKCS5Padding";
            cipher = Cipher.getInstance(transfomation);
            cipher.init(Cipher.ENCRYPT_MODE, desede_key, des_iv);
            for(int i=1;i<=10;i++){
                cipher_data=dofinal_(plain,cipher,i);/////////
                print(i+"* [" + transfomation + "] ENC: " + String.format("%.1f", plain.length*i/time_diff/1024/1024*1000) + " MB/s");
            }
            cipher.init(Cipher.DECRYPT_MODE, desede_key, des_iv);
            for(int i=1;i<=10;i++){
                dec_plain=dofinal_dec(cipher_data,cipher,i);/////////
                if (!Arrays.equals(dec_plain, plain)) {
                    print(transfomation + " DECRYPT FAILED");
                } else {
                    print(i+"* [" + transfomation + "] DEC: " + String.format("%.1f", cipher_data.length*i/time_diff/1024/1024*1000) + " MB/s");
                }}
*/


            print("# TEA: ");
            transfomation = "TEA";
            TEA tea = new TEA(tea_key);
            for (int i = 1; i <= 10; i++) {
                start_time = System.nanoTime();
                for (int n = 1; n <= i; n++)
                    cipher_data = tea.encrypt(plain);
                end_time = System.nanoTime();
                time_diff = (end_time - start_time) / 1e6;
                print(i + "* [" + transfomation + "] ENC: " + String.format("%.1f", plain.length * i / time_diff / 1024 / 1024 * 1000) + " MB/s");
            }

            for (int i = 1; i <= 10; i++) {
                start_time = System.nanoTime();
                for (int n = 1; n <= i; n++)
                    dec_plain = tea.decrypt(cipher_data);
                end_time = System.nanoTime();
                time_diff = (end_time - start_time) / 1e6;
                if (!Arrays.equals(dec_plain, plain)) {
                    print(transfomation + " DECRYPT FAILED");
                } else {
                    print(i + "* [" + transfomation + "] DEC: " + String.format("%.1f", cipher_data.length * i / time_diff / 1024 / 1024 * 1000) + " MB/s");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


class Combination {
    public static void dfs(int[] input
            , int[] output, int index, int start) {
        if (index == output.length)//产生一个组合序列
            ; // System.out.println(Arrays.toString(output));
        else {
            for (int j = start; j < input.length; j++) {
                output[index] = input[j];//记录选取的元素
                dfs(input, output, index + 1, j + 1);//选取下一个元素，可选下标区间为[j+1,input.length]
            }
        }
    }

    public static void main(String[] args) {
        int[] input = new int[22];
        for (int i = 0; i < 22; i++) {
            input[i] = i;
        }
        int N = 11;//组合长度
        int[] output = new int[N];
        dfs(input, output, 0, 0);
    }
}
