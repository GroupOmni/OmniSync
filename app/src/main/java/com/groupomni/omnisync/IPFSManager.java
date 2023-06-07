//package com.groupomni.omnisync;
//
//import io.ipfs.api.IPFS;
//import io.ipfs.api.MerkleNode;
//import io.ipfs.api.NamedStreamable;
//
//import io.ipfs.multihash.Multihash;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//
//public class IPFSManager {
//    private IPFS ipfs;
//
//    public IPFSManager(String ipfsAddress) {
//        ipfs = new IPFS(ipfsAddress);
//
//        IPFS.Config config = ipfs.getConfiguration();
//        config.getAddresses().getSwarm().clear();
//        config.getAddresses().getSwarm().add("/ip4/0.0.0.0/tcp/4001");
//        config.getAddresses().getSwarm().add("/ip6/::/tcp/4001");
//    }
//
//    public String syncFile(String filePath) {
//        NamedStreamable.FileWrapper file = new NamedStreamable.FileWrapper(new File(filePath));
//        try {
//            MerkleNode addedNode = ipfs.add(file).get(0);
//            String cid = addedNode.hash.toBase58();
//            return cid;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public void retrieveFile(Multihash cid, String outputPath) {
//        try {
//            byte[] fileContents = ipfs.cat(cid);
//
//            // Save the file contents to the specified output path
//            Path outputFilePath = Paths.get(outputPath);
//            Files.createDirectories(outputFilePath.getParent());
//            Files.write(outputFilePath, fileContents);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}