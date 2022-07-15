package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.cloud.dataproc.v1.Cluster;
import com.google.cloud.dataproc.v1.ClusterControllerClient;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.ClusterVH;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataProcInventoryCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;
    private static final Logger logger = LoggerFactory.getLogger(DataProcInventoryCollector.class);
    public List<ClusterVH> fetchDataProcInventory(String project)  {
        List<ClusterVH> clusterVHList=new ArrayList<>();
        String[] regions={"us-east1","us-central1"};
        try {
            for(String region:regions) {
                ClusterControllerClient clusterControllerClient = gcpCredentialsProvider.getDataProcClient(region);
                ClusterControllerClient.ListClustersPagedResponse clusters = clusterControllerClient.listClusters(project, region);
                logger.debug("List populated");
                for (Cluster cluster : clusters.iterateAll()) {
                    logger.debug("Iterating list for region {}",region);
                    ClusterVH clusterVH = new ClusterVH();
                    String kmsKeyName=cluster.getConfig().getEncryptionConfig().getGcePdKmsKeyName();
                    clusterVH.setKmsKeyName(kmsKeyName==""?null:kmsKeyName);
                    clusterVH.setProjectName(project);
                    clusterVH.setId(cluster.getClusterName());
                    clusterVH.setRegion(region);
                    clusterVHList.add(clusterVH);
                }
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }

        return clusterVHList;
    }
}