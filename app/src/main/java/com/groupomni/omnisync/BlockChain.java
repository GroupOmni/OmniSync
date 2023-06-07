package com.groupomni.omnisync;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class BlockChain {
    private List<Block> chain;

    public BlockChain() {
        chain = new ArrayList<>();
        chain.add(createGenesisBlock());
    }

    private Block createGenesisBlock() {
        return new Block(0, "0");
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public Block addBlock() {
        Block latestBlock = getLatestBlock();
        Block newBlock = new Block(latestBlock.index + 1, latestBlock.hash);
        chain.add(newBlock);
        return newBlock;
    }

    public void addFileHashToLatestBlock(String fileHash) {
        Block latestBlock = getLatestBlock();
        latestBlock.addFileHash(fileHash);
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                return false;
            }

            if (!currentBlock.previousHash.equals(previousBlock.hash)) {
                return false;
            }
        }

        return true;
    }
}