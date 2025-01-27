package cn.tesseract.bettercaves.config;

public class BCSettings {
    public static final String MOD_ID = "bettercaves";
    public static final String NAME = "Better Caves";
    public static final int SUB_CHUNK_SIZE = 4;
    public static final float[] START_COEFFS = new float[SUB_CHUNK_SIZE];
    public static final float[] END_COEFFS = new float[SUB_CHUNK_SIZE];

    static {
        // Calculate coefficients used for bilinear interpolation during noise calculation.
        // These are initialized one time here to avoid redundant computation later on.
        for (int n = 0; n < SUB_CHUNK_SIZE; n++) {
            START_COEFFS[n] = (float)(SUB_CHUNK_SIZE - 1 - n) / (SUB_CHUNK_SIZE - 1);
            END_COEFFS[n] = (float)(n) / (SUB_CHUNK_SIZE - 1);
        }
    }

    private BCSettings() {} // private constructor prevents instantiation
}
