//package com.groupomni.omnisync;
//
//import java.io.File;
//import java.util.List;
//
//public class FileSyncManager {
//    private IPFSManager ipfsManager;
////    private NodeDiscoveryManager nodeDiscoveryManager;
//
//    public FileSyncManager(String ipfsAddress) {
//        ipfsManager = new IPFSManager(ipfsAddress);
////        nodeDiscoveryManager = new NodeDiscoveryManager();
//    }
//
//    public void syncFolder(String folderPath) {
//        List<String> nodes; // = nodeDiscoveryManager.discoverNodes();
//        for (String node : nodes) {
//            syncFilesInFolder(folderPath, node);
//        }
//    }
//
//    private void syncFilesInFolder(String folderPath, String node) {
//        File folder = new File(folderPath);
//        if (!folder.isDirectory()) {
//            throw new IllegalArgumentException("Specified path is not a directory");
//        }
//
//        File[] files = folder.listFiles();
//        if (files != null) {
//            for (File file : files) {
//                if (file.isDirectory()) {
//                    syncFilesInFolder(file.getAbsolutePath(), node); // Recursively sync files in subdirectories
//                } else {
//                    String cid = ipfsManager.syncFile(file.getAbsolutePath());
//                    if (cid != null) {
//                        sendFileToNode(node, cid);
//                    }
//                }
//            }
//        }
//    }
//
//    private void sendFileToNode(String node, String cid) {
//        // Implement logic to send the CID to the specified node
//    }
//}
