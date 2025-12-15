package com.qxyz17.acq.ml;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;

/** 预训练模型离线打分，0-1 越接近 1 越像作弊 */
public class MLCore {

    private MultiLayerNetwork net;
    private boolean enabled = false;

    public MLCore() {
        File model = new File("plugins/ACQ/model.bin");
        if (model.exists()) {
            try {
                net = ModelSerializer.restoreMultiLayerNetwork(model);
                enabled = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isEnabled() { return enabled; }

    public double predict(float... features) {
        if (!enabled) return 0;
        INDArray input = Nd4j.create(features);
        return net.output(input).getDouble(0);
    }
}
