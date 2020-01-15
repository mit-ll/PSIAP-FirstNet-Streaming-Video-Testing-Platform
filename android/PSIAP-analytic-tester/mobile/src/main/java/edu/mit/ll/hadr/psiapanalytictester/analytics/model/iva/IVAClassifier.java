package edu.mit.ll.hadr.psiapanalytictester.analytics.model.iva;

import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Arrays;
import java.util.Vector;

import edu.mit.ll.hadr.psiapanalytictester.analytics.model.FramePattern;

public class IVAClassifier
{
    private static final String TAG = "PSIAPClassifier";
    private static final String DAYNIGHT_MODEL_FILE = "file:///android_asset/day_night.pb";
    private static final String DAYNIGHT_LABELS_FILE = "";

    private static final String IVA_DAY_MODEL_FILE = "file:///android_asset/iva_day.pb";
    private static final String IVA_NIGHT_MODEL_FILE = "file:///android_asset/iva_night.pb";
    private static final String IVA_LABELS_FILE = "";

    private static final String DN_INPUT_NAME = "layer_1_input";
    private static final String DN_OUTPUT_NAME = "dense_1/Softmax"; // dense_1

    private static final String IVA_INPUT_NAME = "layer_1_input";
    private static final String IVA_OUTPUT_NAME = "dense_1/Softmax"; // dense_1

    //  w  x  h x d
    // 128 x 72 x 3
    private static final int INPUT_SIZE_W = 128;
    private static final int INPUT_SIZE_H = 72;

    private Vector<String> dn_labels = new Vector<String>(Arrays.asList("day", "night"));
    private Vector<String> iva_labels = new Vector<String>(Arrays.asList("In vehicle", "Not in vehicle"));

    private float[] dn_outputs;
    private float[] iva_outputs;
    private String[] dn_onames;
    private String[] iva_onames;


    private TensorFlowInferenceInterface dnInterface;
    private TensorFlowInferenceInterface ivadInterface;
    private TensorFlowInferenceInterface ivanInterface;

    public static IVAClassifier create(AssetManager assetManager)
    {
        IVAClassifier iva = new IVAClassifier();

        String filename = "";
        iva.dnInterface = new TensorFlowInferenceInterface(assetManager, DAYNIGHT_MODEL_FILE);
        iva.ivadInterface = new TensorFlowInferenceInterface(assetManager, IVA_DAY_MODEL_FILE);
        iva.ivanInterface = new TensorFlowInferenceInterface(assetManager, IVA_NIGHT_MODEL_FILE);

        int dnNumC = (int) iva.dnInterface.graph().operation(DN_OUTPUT_NAME).output(0).shape().size(1);
        Log.i(TAG, "Output layer size: " + dnNumC);
        int ivaNumC = (int) iva.ivanInterface.graph().operation(IVA_OUTPUT_NAME).output(0).shape().size(1);
        Log.i(TAG, "IVA Output layer size: " + ivaNumC);

        iva.dn_onames = new String[]{DN_OUTPUT_NAME};
        iva.dn_outputs = new float[dnNumC];
        iva.iva_onames = new String[]{IVA_OUTPUT_NAME};
        iva.iva_outputs = new float[ivaNumC];

        return iva;
    }

    public IVAModel classify(final float[] pixels) {





        return new IVAModel(true, true, "TEST", FramePattern.CAMERA_PATTERN, "DN-1.pb", "IVA-Day.pb");

    }

//    public List<Recognition> recognizeImage(final float[] pixels) {
//        // Log this method so that it can be analyzed with systrace.
//        TraceCompat.beginSection("recognizeImage");
//
//        // Copy the input data into TensorFlow.
//        TraceCompat.beginSection("feed");
//        inferenceInterface.feed(inputName, pixels, new long[]{inputSize * inputSize});
//        TraceCompat.endSection();
//
//        // Run the inference call.
//        TraceCompat.beginSection("run");
//        inferenceInterface.run(outputNames, runStats);
//        TraceCompat.endSection();
//
//        // Copy the output Tensor back into the output array.
//        TraceCompat.beginSection("fetch");
//        inferenceInterface.fetch(outputName, outputs);
//        TraceCompat.endSection();
//
//        // Find the best classifications.
//        PriorityQueue<Recognition> pq =
//                new PriorityQueue<Recognition>(
//                        3,
//                        new Comparator<Recognition>() {
//                            @Override
//                            public int compare(Recognition lhs, Recognition rhs) {
//                                // Intentionally reversed to put high confidence at the head of the queue.
//                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
//                            }
//                        });
//        for (int i = 0; i < outputs.length; ++i) {
//            if (outputs[i] > THRESHOLD) {
//                pq.add(
//                        new Recognition(
//                                "" + i, labels.size() > i ? labels.get(i) : "unknown", outputs[i], null));
//            }
//        }
//        final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
//        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
//        for (int i = 0; i < recognitionsSize; ++i) {
//            recognitions.add(pq.poll());
//        }
//        TraceCompat.endSection(); // "recognizeImage"
//        return recognitions;
//    }

    public String getStatString() {
        return dnInterface.getStatString();
    }

    public String getIvaDStatString() {
        return ivadInterface.getStatString();
    }

    public String getIvaNStatString() {
        return ivanInterface.getStatString();
    }

    public void close() {
        dnInterface.close();
        ivadInterface.close();
        ivanInterface.close();
    }
}
