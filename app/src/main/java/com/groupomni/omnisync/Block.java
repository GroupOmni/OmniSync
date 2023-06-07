package com.groupomni.omnisync;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Block {
    public int index;
    private long timestamp;
    public String hash;
    public String previousHash;
    private List<String> fileHashes;

    public Block(int index, String previousHash) {
        this.index = index;
        this.timestamp = new Date().getTime();
        this.previousHash = previousHash;
        this.fileHashes = new ArrayList<>();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        String data = index + timestamp + previousHash + fileHashes.toString();
        StringBuilder sb = new StringBuilder();

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = messageDigest.digest(data.getBytes());

            for (byte hashByte : hashBytes) {
                sb.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public void addFileHash(String fileHash) {
        fileHashes.add(fileHash);
        hash = calculateHash();
    }
}
